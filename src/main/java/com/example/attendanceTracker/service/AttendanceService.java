package com.example.attendanceTracker.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendanceTracker.DTO.AttendanceReportDTO;
import com.example.attendanceTracker.DTO.CheckInDTO;
import com.example.attendanceTracker.DTO.CheckOutDTO;
import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.AttendanceRepository;
import com.example.attendanceTracker.repository.ComplainRepository;
import com.example.attendanceTracker.repository.UserRepository;
import com.example.attendanceTracker.util.FileStorageService;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final ComplainRepository complainRepository;
    private final FileStorageService fileStorageService;

    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;
    
    @Value("${supabase.bucket}")
    private String supabaseBucket;

    public AttendanceService(
            AttendanceRepository attendanceRepository, 
            UserRepository userRepository,
            ComplainRepository complainRepository,
            FileStorageService fileStorageService) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.complainRepository = complainRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Attendance checkIn(User user, CheckInDTO checkInDto) {
        // Check if user has already checked in today
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        Optional<Attendance> existingRecord = attendanceRepository.findByUserAndDate(user, startOfDay, endOfDay);
        
        if (existingRecord.isPresent()) {
            throw new RuntimeException("Bạn đã điểm danh hôm nay rồi. Thời gian điểm danh: " + 
                existingRecord.get().getCheckIn().toLocalTime());
        }
        
        Attendance record = new Attendance();
        record.setUser(user);
        record.setCheckIn(LocalDateTime.now());
        
        // Xử lý tải lên ảnh checkin nếu có
        if (checkInDto.getCheckInImage() != null && !checkInDto.getCheckInImage().isEmpty()) {
            String imageUrl = fileStorageService.storeFile(checkInDto.getCheckInImage(), 
                    "checkin_" + user.getId() + "_" + System.currentTimeMillis());
            record.setCheckInImageUrl(imageUrl);
        }
        
        return attendanceRepository.save(record);
    }

    // New checkout method that automatically finds today's record
    @Transactional
    public Attendance checkOut(User user, CheckOutDTO checkOutDto) {
        // Find today's attendance record for this user
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        Optional<Attendance> todayRecord = attendanceRepository.findByUserAndDate(user, startOfDay, endOfDay);
        
        if (todayRecord.isEmpty()) {
            throw new RuntimeException("Không tìm thấy bản ghi điểm danh cho hôm nay. Vui lòng điểm danh trước.");
        }
        
        Attendance record = todayRecord.get();
        
        record.setCheckOut(LocalDateTime.now());
        
        // Xử lý tải lên ảnh checkout nếu có
        if (checkOutDto.getCheckOutImage() != null && !checkOutDto.getCheckOutImage().isEmpty()) {
            String imageUrl = fileStorageService.storeFile(checkOutDto.getCheckOutImage(), 
                    "checkout_" + record.getUser().getId() + "_" + System.currentTimeMillis());
            record.setCheckOutImageUrl(imageUrl);
        }
        
        return attendanceRepository.save(record);
    }

    // Get today's attendance status for a user
    public Optional<Attendance> getTodayAttendance(User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        return attendanceRepository.findByUserAndDate(user, startOfDay, endOfDay);
    }
    
    // Check if user can check in today
    public boolean canCheckInToday(User user) {
        return getTodayAttendance(user).isEmpty();
    }
    
    // Check if user can check out today
    public boolean canCheckOutToday(User user) {
        Optional<Attendance> todayRecord = getTodayAttendance(user);
        return todayRecord.isPresent() && todayRecord.get().getCheckOut() == null;
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
    
    // Lấy danh sách attendance của user với filter theo ngày (with pagination)
    public Page<Attendance> getMyAttendance(User user, Pageable pageable, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByUserAndCheckInBetween(user, fromTime, toTime, pageable);
        } else if (from != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            return attendanceRepository.findByUserAndCheckInAfter(user, fromTime, pageable);
        } else if (to != null) {
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByUserAndCheckInBefore(user, toTime, pageable);
        } else {
            // Nếu không có filter, lấy tất cả attendance của user
            return attendanceRepository.findByUser(user, pageable);
        }
    }

    // Lấy danh sách attendance của user với filter theo ngày (without pagination - kept for backward compatibility)
    public List<Attendance> getMyAttendance(User user, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByUserAndCheckInBetween(user, fromTime, toTime);
        } else if (from != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            return attendanceRepository.findByUserAndCheckInAfter(user, fromTime);
        } else if (to != null) {
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByUserAndCheckInBefore(user, toTime);
        } else {
            // Nếu không có filter, lấy tất cả attendance của user
            return attendanceRepository.findByUser(user);
        }
    }
    
    public Attendance findById(UUID id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi điểm danh"));
    }
    
    public List<Attendance> getAttendanceBetween(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByCheckInBetween(fromTime, toTime);
        } else if (from != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            return attendanceRepository.findByCheckInAfter(fromTime);
        } else if (to != null) {
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByCheckInBefore(toTime);
        } else {
            return attendanceRepository.findAll();
        }
    }

    public Page<Attendance> getAllAttendance(Pageable pageable, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByCheckInBetween(fromTime, toTime, pageable);
        } else if (from != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            return attendanceRepository.findByCheckInAfter(fromTime, pageable);
        } else if (to != null) {
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByCheckInBefore(toTime, pageable);
        } else {
            return attendanceRepository.findAll(pageable);
        }
    }

    public List<Attendance> getAllAttendance(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByCheckInBetween(fromTime, toTime);
        } else if (from != null) {
            LocalDateTime fromTime = from.atStartOfDay();
            return attendanceRepository.findByCheckInAfter(fromTime);
        } else if (to != null) {
            LocalDateTime toTime = to.atTime(LocalTime.MAX);
            return attendanceRepository.findByCheckInBefore(toTime);
        } else {
            return attendanceRepository.findAll();
        }
    }
    
    public List<Attendance> filterAttendance(Boolean checkedIn, Boolean checkedOut, UUID userId) {
        List<Attendance> result = new ArrayList<>();
        
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            result = attendanceRepository.findByUser(user);
        } else {
            result = attendanceRepository.findAll();
        }
        
        // Lọc theo trạng thái checkedIn/checkedOut
        if (checkedIn != null || checkedOut != null) {
            return result.stream()
                    .filter(a -> {
                        boolean match = true;
                        if (checkedIn != null) {
                            match = match && (a.getCheckIn() != null) == checkedIn;
                        }
                        if (checkedOut != null) {
                            match = match && (a.getCheckOut() != null) == checkedOut;
                        }
                        return match;
                    })
                    .collect(Collectors.toList());
        }
        
        return result;
    }
    
    public AttendanceReportDTO getMonthlySummary(UUID userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        List<Attendance> attendances = getMonthlyReport(user, year, month);
        
        AttendanceReportDTO report = new AttendanceReportDTO();
        report.setUserId(userId);
        report.setUserName(user.getName());
        report.setUserEmail(user.getEmail());
        report.setYear(year);
        report.setMonth(month);
        
        LocalDate date = LocalDate.of(year, month, 1);
        int daysInMonth = date.lengthOfMonth();
        report.setTotalDays(daysInMonth);
        
        report.setPresentDays(attendances.size());
        
        report.setAbsentDays(daysInMonth - report.getPresentDays());

        double totalHours = 0;
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                double hours = ChronoUnit.MINUTES.between(attendance.getCheckIn(), attendance.getCheckOut()) / 60.0;
                totalHours += hours;
            }
        }
        report.setTotalHours(totalHours);
        
        report.setAttendanceRecords(attendances);
        
        return report;
    }

    public AttendanceReportDTO getSummaryByPeriod(UUID userId, LocalDate from, LocalDate to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.atTime(LocalTime.MAX);
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetween(user, fromTime, toTime);

        AttendanceReportDTO report = new AttendanceReportDTO();
        report.setUserId(userId);
        report.setUserName(user.getName());
        report.setUserEmail(user.getEmail());
        report.setTotalDays((int) ChronoUnit.DAYS.between(from, to) + 1);
        report.setAttendanceRecords(attendances);
        report.setPresentDays(attendances.size());
        report.setAbsentDays(report.getTotalDays() - report.getPresentDays());
        double totalHours = 0;
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                double hours = ChronoUnit.MINUTES.between(attendance.getCheckIn(), attendance.getCheckOut()) / 60.0;
                totalHours += hours;
            }
        }
        report.setTotalHours(totalHours);
        return report;
    }

    public AttendanceReportDTO getYearlySummary(UUID userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        List<Attendance> attendances = getYearlyReport(user, year);
        
        AttendanceReportDTO report = new AttendanceReportDTO();
        report.setUserId(userId);
        report.setUserName(user.getName());
        report.setUserEmail(user.getEmail());
        report.setYear(year);
        
        // Tính tổng số ngày trong năm
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        int totalDays = (int) ChronoUnit.DAYS.between(startOfYear, endOfYear) + 1;
        report.setTotalDays(totalDays);
        
        report.setPresentDays(attendances.size());
        
        report.setAbsentDays(totalDays - report.getPresentDays());

        double totalHours = 0;
        for (Attendance attendance : attendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() != null) {
                double hours = ChronoUnit.MINUTES.between(attendance.getCheckIn(), attendance.getCheckOut()) / 60.0;
                totalHours += hours;
            }
        }
        report.setTotalHours(totalHours);
        
        report.setAttendanceRecords(attendances);
        
        return report;
    }
    
    public List<Attendance> getAttendanceWithComplaints() {
        return complainRepository.findAll().stream()
                .map(complain -> complain.getAttendance())
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Transactional
    public Attendance updateAttendance(UUID id, com.example.attendanceTracker.DTO.UpdateAttendanceDTO dto) {
        Attendance existingAttendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi điểm danh"));
        
        // Cập nhật thời gian check-in nếu có
        if (dto.getCheckIn() != null) {
            existingAttendance.setCheckIn(dto.getCheckIn());
        }
        
        // Cập nhật thời gian check-out nếu có
        if (dto.getCheckOut() != null) {
            existingAttendance.setCheckOut(dto.getCheckOut());
        }
        
        // Cập nhật hình ảnh check-in nếu có
        if (dto.getCheckInImage() != null && !dto.getCheckInImage().isEmpty()) {
            String imageUrl = fileStorageService.storeFile(dto.getCheckInImage(), 
                    "checkin_update_" + existingAttendance.getUser().getId() + "_" + System.currentTimeMillis());
            existingAttendance.setCheckInImageUrl(imageUrl);
        }
        
        // Cập nhật hình ảnh check-out nếu có
        if (dto.getCheckOutImage() != null && !dto.getCheckOutImage().isEmpty()) {
            String imageUrl = fileStorageService.storeFile(dto.getCheckOutImage(), 
                    "checkout_update_" + existingAttendance.getUser().getId() + "_" + System.currentTimeMillis());
            existingAttendance.setCheckOutImageUrl(imageUrl);
        }
        
        return attendanceRepository.save(existingAttendance);
    }
    
    // Các phương thức hỗ trợ cho export báo cáo (hiện đã vô hiệu hóa)
    /**
     * @deprecated This method is no longer used as the Excel export functionality has been removed
     */
    @Deprecated
    public List<Attendance> getMonthlyAttendanceForUser(UUID userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return getMonthlyReport(user, year, month);
    }
    
    /**
     * @deprecated This method is no longer used as the Excel export functionality has been removed
     */
    @Deprecated
    public List<Attendance> getYearlyAttendanceForUser(UUID userId, int year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return getYearlyReport(user, year);
    }
    
    /**
     * @deprecated This method is no longer used as the Excel export functionality has been removed
     */
    @Deprecated
    public List<Attendance> getMonthlyAttendanceForAllUsers(int year, int month) {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        return attendanceRepository.findByCheckInBetween(start, end);
    }
    
    /**
     * @deprecated This method is no longer used as the Excel export functionality has been removed
     */
    @Deprecated
    public List<Attendance> getYearlyAttendanceForAllUsers(int year) {
        LocalDateTime start = LocalDateTime.of(LocalDate.of(year, 1, 1), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.of(year, 12, 31), LocalTime.MAX);
        return attendanceRepository.findByCheckInBetween(start, end);
    }

    // Test Supabase configuration
    public ResponseEntity<Map<String, String>> testSupabaseConfig() {
        try {
            String url = supabaseUrl != null ? supabaseUrl : "Not configured";
            String bucket = supabaseBucket != null ? supabaseBucket : "Not configured";
            
            String maskedKey = "Not configured";
            if (supabaseKey != null && supabaseKey.length() > 15) {
                maskedKey = supabaseKey.substring(0, 10) + "..." + 
                            supabaseKey.substring(supabaseKey.length() - 5);
            }
            
            Map<String, String> config = new HashMap<>();
            config.put("supabaseUrl", url);
            config.put("supabaseBucket", bucket);
            config.put("supabaseKey", maskedKey);
            config.put("configSource", "From application.properties using .env values");
            
            try {
                String listEndpoint = supabaseUrl + "/storage/v1/object/list/" + supabaseBucket;
                
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("apikey", supabaseKey);
                headers.set("Authorization", "Bearer " + supabaseKey);
                headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                
                // Fix: Add required 'prefix' property to request body
                String requestBody = "{\"prefix\": \"\", \"limit\": 10}";
                org.springframework.http.HttpEntity<String> requestEntity = 
                    new org.springframework.http.HttpEntity<>(requestBody, headers);
                
                org.springframework.web.client.RestTemplate restTemplate = 
                    new org.springframework.web.client.RestTemplate();
                
                org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    listEndpoint,
                    org.springframework.http.HttpMethod.POST,
                    requestEntity,
                    String.class);
                
                config.put("bucketTest", "SUCCESS - Status: " + response.getStatusCode());
                config.put("bucketResponse", response.getBody());
                
            } catch (Exception bucketError) {
                config.put("bucketTest", "FAILED");
                config.put("bucketError", bucketError.getMessage());
                
                // More specific error handling
                if (bucketError.getMessage().contains("404")) {
                    config.put("bucketErrorType", "BUCKET_NOT_FOUND - Tạo bucket 'attendencetracker' trong Supabase Dashboard");
                } else if (bucketError.getMessage().contains("401") || bucketError.getMessage().contains("403")) {
                    config.put("bucketErrorType", "PERMISSION_DENIED - Sử dụng service_role key hoặc thiết lập RLS policies");
                } else {
                    config.put("bucketErrorType", "UNKNOWN_ERROR");
                }
            }
            
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            Map<String, String> errorConfig = new HashMap<>();
            errorConfig.put("error", "Lỗi khi lấy cấu hình Supabase: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorConfig);
        }
    }

    // Get today's attendance status for a user
    public ResponseEntity<Map<String, Object>> getTodayAttendanceStatus(User user) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Attendance> todayAttendance = getTodayAttendance(user);
        
        if (todayAttendance.isPresent()) {
            Attendance attendance = todayAttendance.get();
            response.put("hasCheckedIn", true);
            response.put("checkInTime", attendance.getCheckIn());
            response.put("hasCheckedOut", attendance.getCheckOut() != null);
            if (attendance.getCheckOut() != null) {
                response.put("checkOutTime", attendance.getCheckOut());
            }
            response.put("canCheckOut", canCheckOutToday(user));
            response.put("attendance", attendance);
        } else {
            response.put("hasCheckedIn", false);
            response.put("hasCheckedOut", false);
            response.put("canCheckIn", true);
            response.put("canCheckOut", false);
        }
        
        response.put("canCheckIn", canCheckInToday(user));
        
        return ResponseEntity.ok(response);
    }
    
    public Page<Attendance> getAttendanceByUserId(UUID userId, Pageable pageable, LocalDate from, LocalDate to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return getMyAttendance(user, pageable, from, to);
    }

    public List<Attendance> getAttendanceByUserId(UUID userId, LocalDate from, LocalDate to) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        return getMyAttendance(user, from, to);
    }
}
