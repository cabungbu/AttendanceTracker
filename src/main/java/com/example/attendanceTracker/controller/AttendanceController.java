package com.example.attendanceTracker.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.attendanceTracker.DTO.AttendanceReportDTO;
import com.example.attendanceTracker.DTO.AttendanceWithStatsDTO;
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
            User user = userService.findByEmailExcludeDeleted(email);
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
            User user = userService.findByEmailExcludeDeleted(email);
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
            @RequestParam(required = false) UUID userId,
            Authentication auth) {

        User user;

        if (userId != null) {
            // Chỉ admin được truy cập userId khác
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            user = userService.findByIdExcludeDeleted(userId);
        } else {
            // Nếu không có userId, lấy từ token (staff tự xem mình)
            String email = extractEmailFromAuth(auth);
            user = userService.findByEmailExcludeDeleted(email);
        }

        return ResponseEntity.ok(attendanceService.getMonthlyReport(user, year, month));
    }

    @GetMapping("/report/yearly")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<List<Attendance>> yearlyReport(
            @RequestParam int year,
            @RequestParam(required = false) UUID userId,
            Authentication auth) {

        User user;

        if (userId != null) {
            // Chỉ admin mới được dùng userId
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            user = userService.findByIdExcludeDeleted(userId);
        } else {
            // Lấy từ token
            String email = extractEmailFromAuth(auth);
            user = userService.findByEmailExcludeDeleted(email);
        }

        List<Attendance> result = attendanceService.getYearlyReport(user, year);
        return ResponseEntity.ok(result);
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        // Ưu tiên xử lý theo năm/tháng nếu được truyền
        if (year != null && month != null) {
            from = LocalDate.of(year, month, 1);
            to = from.plusMonths(1).minusDays(1);
        } else if (year != null) {
            from = LocalDate.of(year, 1, 1);
            to = LocalDate.of(year, 12, 31);
        }

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
        // Kiểm tra user có bị xóa không
        userService.findByIdExcludeDeleted(userId);
        return ResponseEntity.ok(attendanceService.getAttendanceByUserId(userId, pageable, from, to));
    }
    
    // Lấy danh sách attendance của tài khoản hiện tại
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<AttendanceWithStatsDTO> getMyAttendance(
            Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        String email = extractEmailFromAuth(auth);
        User user = userService.findByEmailExcludeDeleted(email);

        // Gọi service mới trả về records + stats
        AttendanceWithStatsDTO response = attendanceService.getMyAttendanceWithStats(user, from, to);

        return ResponseEntity.ok(response);
    }
    
    // Lọc attendance theo trạng thái
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<List<Attendance>> filterAttendance(
            @RequestParam(required = false) Boolean checkedIn,
            @RequestParam(required = false) Boolean checkedOut,
            @RequestParam(required = false) UUID userId) {
        // Kiểm tra user có bị xóa không nếu userId được cung cấp
        if (userId != null) {
            userService.findByIdExcludeDeleted(userId);
        }
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
            // Kiểm tra user có bị xóa không
            userService.findByIdExcludeDeleted(userId);
            userIdToUse = userId; // Admin có thể xem báo cáo của bất kỳ user nào
        } else {
            // Nếu không phải admin hoặc admin không chỉ định user, lấy thông tin user từ token
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmailExcludeDeleted(currentUserEmail);
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
            // Kiểm tra user có bị xóa không
            userService.findByIdExcludeDeleted(userId);
            userIdToUse = userId; // Admin có thể xem báo cáo của bất kỳ user nào
        } else {
            // Nếu không phải admin hoặc admin không chỉ định user, lấy thông tin user từ token
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmailExcludeDeleted(currentUserEmail);
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
            // Kiểm tra user có bị xóa không
            userService.findByIdExcludeDeleted(userId);
            userIdToUse = userId;
        } else {
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmailExcludeDeleted(currentUserEmail);
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
    
    @GetMapping("/test-supabase-config")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, String>> testSupabaseConfig() {
        return attendanceService.testSupabaseConfig();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteAttendance(@PathVariable UUID id) {
        try {
            attendanceService.deleteAttendance(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Xóa bản ghi điểm danh thành công");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<Map<String, Object>> getTodayStatus(Authentication auth) {
        String email = extractEmailFromAuth(auth);
        User user = userService.findByEmailExcludeDeleted(email);
        
        return attendanceService.getTodayAttendanceStatus(user);
    }
}
