package com.example.attendanceTracker.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.AttendanceRepository;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Attendance checkIn(User user) {
        Attendance record = new Attendance();
        record.setUser(user);
        record.setCheckIn(LocalDateTime.now());
        return attendanceRepository.save(record);
    }

    public Attendance checkOut(UUID id) {
        Attendance record = attendanceRepository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));
        record.setCheckOut(LocalDateTime.now());
        return attendanceRepository.save(record);
    }

    public List<Attendance> getMonthlyReport(User user, int year, int month) {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        return attendanceRepository.findByUserAndCheckInBetween(user, start, end);
    }

    public List<Attendance> getYearlyReport(User user, int year) {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, 1, 1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.of(year, 12, 31), LocalTime.MAX);
        return attendanceRepository.findByUserAndCheckInBetween(user, start, end);
    }
}
