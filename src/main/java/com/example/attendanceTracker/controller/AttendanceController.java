package com.example.attendanceTracker.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.attendanceTracker.DTO.AttendanceReportDTO;
import com.example.attendanceTracker.DTO.CheckInDTO;
import com.example.attendanceTracker.DTO.CheckOutDTO;
import com.example.attendanceTracker.DTO.UpdateAttendanceDTO;
import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.service.AttendanceService;
import com.example.attendanceTracker.service.UserService;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final UserService userService;

    public AttendanceController(AttendanceService attendanceService, UserService userService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
    }

    private String extractEmailFromAuth(Authentication auth) {
        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }
        return auth.getName();
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<?> checkIn(
            Authentication auth, 
            @RequestParam(value = "checkInImage", required = false) MultipartFile checkInImage) {
        try {
            String email = extractEmailFromAuth(auth);
            User user = userService.findByEmail(email);
            CheckInDTO checkInDto = new CheckInDTO();
            checkInDto.setCheckInImage(checkInImage);
            
            Attendance attendance = attendanceService.checkIn(user, checkInDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Điểm danh thành công");
            response.put("attendance", attendance);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<?> checkOut(
            Authentication auth,
            @RequestParam(value = "checkOutImage", required = false) MultipartFile checkOutImage) {
        try {
            String email = extractEmailFromAuth(auth);
            User user = userService.findByEmail(email);
            CheckOutDTO checkOutDto = new CheckOutDTO();
            checkOutDto.setCheckOutImage(checkOutImage);
            
            Attendance attendance = attendanceService.checkOut(user, checkOutDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Kết thúc ca làm việc thành công");
            response.put("attendance", attendance);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/report/monthly")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<List<Attendance>> monthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String email,
            Authentication auth) {
        String currentUserEmail = extractEmailFromAuth(auth);
        
        User user = userService.findByEmail(
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"))
                ? (email != null ? email : currentUserEmail)
                : currentUserEmail);
        return ResponseEntity.ok(attendanceService.getMonthlyReport(user, year, month));
    }

    @GetMapping("/report/yearly")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<List<Attendance>> yearlyReport(
            @RequestParam int year,
            @RequestParam(required = false) String email,
            Authentication auth) {
        String currentUserEmail = extractEmailFromAuth(auth);
        
        User user = userService.findByEmail(
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"))
                ? (email != null ? email : currentUserEmail)
                : currentUserEmail);
        return ResponseEntity.ok(attendanceService.getYearlyReport(user, year));
    }
    
    // Lấy chi tiết một bản ghi attendance
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.findById(id));
    }
    
    // Lấy danh sách attendance của tất cả user theo khoảng thời gian (cho admin)
    @GetMapping("/getBetween")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Attendance>> getAttendanceBetween(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAttendanceBetween(from, to));
    }

    // Lấy tất cả attendance (có thể filter theo thời gian)
    @GetMapping("/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Page<Attendance>> getAllAttendance(
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAllAttendance(pageable, from, to));
    }
    
    // Lấy attendance theo userId cụ thể (cho admin)
    @GetMapping("/getByUser/{userId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Page<Attendance>> getAttendanceByUserId(
            @PathVariable UUID userId,
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAttendanceByUserId(userId, pageable, from, to));
    }
    
    // Lấy danh sách attendance của tài khoản hiện tại
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<Page<Attendance>> getMyAttendance(
            Authentication auth,
            Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        String email = extractEmailFromAuth(auth);
        User user = userService.findByEmail(email);
        
        return ResponseEntity.ok(attendanceService.getMyAttendance(user, pageable, from, to));
    }
    
    // Lọc attendance theo trạng thái
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<List<Attendance>> filterAttendance(
            @RequestParam(required = false) Boolean checkedIn,
            @RequestParam(required = false) Boolean checkedOut,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(attendanceService.filterAttendance(checkedIn, checkedOut, userId));
    }
    
    // Báo cáo tổng hợp theo tháng
    @GetMapping("/summary/monthly")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<AttendanceReportDTO> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) UUID userId,
            Authentication auth) {
        
        UUID userIdToUse;
        
        if (userId != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"))) {
            userIdToUse = userId; // Admin có thể xem báo cáo của bất kỳ user nào
        } else {
            // Nếu không phải admin hoặc admin không chỉ định user, lấy thông tin user từ token
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmail(currentUserEmail);
            userIdToUse = user.getId();
        }
        
        return ResponseEntity.ok(attendanceService.getMonthlySummary(userIdToUse, year, month));
    }

    // Báo cáo tổng hợp theo năm
    @GetMapping("/summary/yearly")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<AttendanceReportDTO> getYearlySummary(
            @RequestParam int year,
            @RequestParam(required = false) UUID userId,
            Authentication auth) {
        
        UUID userIdToUse;
        
        if (userId != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"))) {
            userIdToUse = userId; // Admin có thể xem báo cáo của bất kỳ user nào
        } else {
            // Nếu không phải admin hoặc admin không chỉ định user, lấy thông tin user từ token
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmail(currentUserEmail);
            userIdToUse = user.getId();
        }
        
        return ResponseEntity.ok(attendanceService.getYearlySummary(userIdToUse, year));
    }

    // Báo cáo tổng hợp theo khoảng thời gian (from - to)
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<AttendanceReportDTO> getSummaryByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID userId,
            Authentication auth) {
        UUID userIdToUse;
        if (userId != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_admin"))) {
            userIdToUse = userId;
        } else {
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmail(currentUserEmail);
            userIdToUse = user.getId();
        }
        return ResponseEntity.ok(attendanceService.getSummaryByPeriod(userIdToUse, from, to));
    }
    
    // Lấy danh sách attendance có khiếu nại
    @GetMapping("/with-complaints")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Attendance>> getAttendanceWithComplaints() {
        return ResponseEntity.ok(attendanceService.getAttendanceWithComplaints());
    }
    
    // Sửa một bản ghi attendance (quyền admin)
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> updateAttendance(
            @PathVariable UUID id,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(value = "checkInImage", required = false) MultipartFile checkInImage,
            @RequestParam(value = "checkOutImage", required = false) MultipartFile checkOutImage) {
        try {
            UpdateAttendanceDTO dto = new UpdateAttendanceDTO();
            
            // Parse thời gian nếu có
            if (checkIn != null && !checkIn.trim().isEmpty()) {
                dto.setCheckIn(java.time.LocalDateTime.parse(checkIn));
            }
            
            if (checkOut != null && !checkOut.trim().isEmpty()) {
                dto.setCheckOut(java.time.LocalDateTime.parse(checkOut));
            }
            
            // Set hình ảnh nếu có
            dto.setCheckInImage(checkInImage);
            dto.setCheckOutImage(checkOutImage);
            
            Attendance updatedAttendance = attendanceService.updateAttendance(id, dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Cập nhật điểm danh thành công");
            response.put("attendance", updatedAttendance);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    // Endpoint để kiểm tra cấu hình Supabase
    @GetMapping("/test-supabase-config")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, String>> testSupabaseConfig() {
        return attendanceService.testSupabaseConfig();
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<Map<String, Object>> getTodayStatus(Authentication auth) {
        String email = extractEmailFromAuth(auth);
        User user = userService.findByEmail(email);
        
        return attendanceService.getTodayAttendanceStatus(user);
    }
}
