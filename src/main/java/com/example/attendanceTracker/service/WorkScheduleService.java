package com.example.attendanceTracker.service;

import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.attendanceTracker.model.Attendance;

@Service
public class WorkScheduleService {
    
    // Ca làm việc cố định từ 8:30 - 17:30
    private static final LocalTime WORK_START_TIME = LocalTime.of(8, 30);
    private static final LocalTime WORK_END_TIME = LocalTime.of(17, 30);
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(8, 30); // Đi trễ nếu sau 8:30
    
    public LocalTime getWorkStartTime() {
        return WORK_START_TIME;
    }
    
    public LocalTime getWorkEndTime() {
        return WORK_END_TIME;
    }
    
    public LocalTime getLateThreshold() {
        return LATE_THRESHOLD;
    }
    
    /**
     * Kiểm tra xem có đi trễ không
     */
    public boolean isLate(Attendance attendance) {
        if (attendance.getCheckIn() == null) {
            return false; // Không có check-in thì không tính đi trễ
        }
        
        LocalTime checkInTime = attendance.getCheckIn().toLocalTime();
        return checkInTime.isAfter(LATE_THRESHOLD);
    }
    
    /**
     * Tính số phút đi trễ
     */
    public long getLateMinutes(Attendance attendance) {
        if (attendance.getCheckIn() == null || !isLate(attendance)) {
            return 0;
        }
        
        LocalTime checkInTime = attendance.getCheckIn().toLocalTime();
        return java.time.Duration.between(LATE_THRESHOLD, checkInTime).toMinutes();
    }
    
    /**
     * Đếm số buổi đi trễ trong danh sách attendance
     */
    public long countLateDays(List<Attendance> attendances) {
        return attendances.stream()
                .filter(this::isLate)
                .count();
    }
    
    /**
     * Tính tổng số phút đi trễ trong danh sách attendance
     */
    public long getTotalLateMinutes(List<Attendance> attendances) {
        return attendances.stream()
                .mapToLong(this::getLateMinutes)
                .sum();
    }
    
    /**
     * Kiểm tra có check-out sớm không (trước 17:30)
     */
    public boolean isEarlyCheckout(Attendance attendance) {
        if (attendance.getCheckOut() == null) {
            return false;
        }
        
        LocalTime checkOutTime = attendance.getCheckOut().toLocalTime();
        return checkOutTime.isBefore(WORK_END_TIME);
    }
    
    /**
     * Tính số phút về sớm
     */
    public long getEarlyCheckoutMinutes(Attendance attendance) {
        if (attendance.getCheckOut() == null || !isEarlyCheckout(attendance)) {
            return 0;
        }
        
        LocalTime checkOutTime = attendance.getCheckOut().toLocalTime();
        return java.time.Duration.between(checkOutTime, WORK_END_TIME).toMinutes();
    }
}
