package com.example.attendanceTracker.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.attendanceTracker.DTO.AttendanceReportDTO;
import com.example.attendanceTracker.DTO.CheckInDTO;
import com.example.attendanceTracker.DTO.CheckOutDTO;
import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.service.AttendanceService;
import com.example.attendanceTracker.service.UserService;
import com.example.attendanceTracker.util.FileStorageService;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;
    
    @Value("${supabase.bucket}")
    private String supabaseBucket;

    public AttendanceController(AttendanceService attendanceService, UserService userService, FileStorageService fileStorageService) {
        this.attendanceService = attendanceService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
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
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
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
    
    // Lấy danh sách attendance của tất cả user (cho admin)
    @GetMapping("/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Attendance>> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAllAttendance(from, to));
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
        
        if (userId != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            userIdToUse = userId; // Admin có thể xem báo cáo của bất kỳ user nào
        } else {
            // Nếu không phải admin hoặc admin không chỉ định user, lấy thông tin user từ token
            String currentUserEmail = extractEmailFromAuth(auth);
            User user = userService.findByEmail(currentUserEmail);
            userIdToUse = user.getId();
        }
        
        return ResponseEntity.ok(attendanceService.getMonthlySummary(userIdToUse, year, month));
    }
    
    // Lấy danh sách attendance có khiếu nại
    @GetMapping("/with-complaints")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<Attendance>> getAttendanceWithComplaints() {
        return ResponseEntity.ok(attendanceService.getAttendanceWithComplaints());
    }
    
    // Sửa một bản ghi attendance (quyền admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable UUID id,
            @RequestBody Attendance attendance) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, attendance));
    }
    
    // Endpoint để kiểm tra cấu hình Supabase
    @GetMapping("/test-supabase-config")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Map<String, String>> testSupabaseConfig() {
        try {
            // Lấy thông tin từ application.properties
            String url = supabaseUrl != null ? supabaseUrl : "Not configured";
            String bucket = supabaseBucket != null ? supabaseBucket : "Not configured";
            
            // Che giấu key vì lý do bảo mật
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
            
            // Test bucket accessibility
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

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<Map<String, Object>> getTodayStatus(Authentication auth) {
        String email = extractEmailFromAuth(auth);
        User user = userService.findByEmail(email);
        
        Map<String, Object> response = new HashMap<>();
        
        Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(user);
        
        if (todayAttendance.isPresent()) {
            Attendance attendance = todayAttendance.get();
            response.put("hasCheckedIn", true);
            response.put("checkInTime", attendance.getCheckIn());
            response.put("hasCheckedOut", attendance.getCheckOut() != null);
            if (attendance.getCheckOut() != null) {
                response.put("checkOutTime", attendance.getCheckOut());
            }
            response.put("canCheckOut", attendanceService.canCheckOutToday(user));
            response.put("attendance", attendance);
        } else {
            response.put("hasCheckedIn", false);
            response.put("hasCheckedOut", false);
            response.put("canCheckIn", true);
            response.put("canCheckOut", false);
        }
        
        response.put("canCheckIn", attendanceService.canCheckInToday(user));
        
        return ResponseEntity.ok(response);
    }
}
