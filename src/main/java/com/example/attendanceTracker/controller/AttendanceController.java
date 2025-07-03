package com.example.attendanceTracker.controller;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.service.AttendanceService;
import com.example.attendanceTracker.service.UserService;
import com.example.attendanceTracker.DTO.AttendanceReportDTO;
import com.example.attendanceTracker.DTO.CheckInDTO;
import com.example.attendanceTracker.DTO.CheckOutDTO;
import com.example.attendanceTracker.utils.ExportUtil;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    private final UserService userService;
    private final ExportUtil exportUtil;

    public AttendanceController(AttendanceService attendanceService, UserService userService, ExportUtil exportUtil) {
        this.attendanceService = attendanceService;
        this.userService = userService;
        this.exportUtil = exportUtil;
    }

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Attendance> checkIn(Authentication auth, @RequestBody CheckInDTO checkInDto) {
        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(attendanceService.checkIn(user, checkInDto));
    }

    @PostMapping("/checkout/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Attendance> checkOut(@PathVariable UUID id, @RequestBody CheckOutDTO checkOutDto) {
        return ResponseEntity.ok(attendanceService.checkOut(id, checkOutDto));
    }

    @GetMapping("/report/monthly")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<Attendance>> monthlyReport(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String email,
            Authentication auth) {
        User user = userService.findByEmail(
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                ? (email != null ? email : auth.getName())
                : auth.getName());
        return ResponseEntity.ok(attendanceService.getMonthlyReport(user, year, month));
    }

    @GetMapping("/report/yearly")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<Attendance>> yearlyReport(
            @RequestParam int year,
            @RequestParam(required = false) String email,
            Authentication auth) {
        User user = userService.findByEmail(
            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                ? (email != null ? email : auth.getName())
                : auth.getName());
        return ResponseEntity.ok(attendanceService.getYearlyReport(user, year));
    }
    
    // Lấy chi tiết một bản ghi attendance
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.findById(id));
    }
    
    // Lấy danh sách attendance của tất cả user (cho admin)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Attendance>> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getAllAttendance(from, to));
    }
    
    // Lọc attendance theo trạng thái
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<Attendance>> filterAttendance(
            @RequestParam(required = false) Boolean checkedIn,
            @RequestParam(required = false) Boolean checkedOut,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(attendanceService.filterAttendance(checkedIn, checkedOut, userId));
    }
    
    // Báo cáo tổng hợp theo tháng
    @GetMapping("/summary/monthly")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
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
            User user = userService.findByEmail(auth.getName());
            userIdToUse = user.getId();
        }
        
        return ResponseEntity.ok(attendanceService.getMonthlySummary(userIdToUse, year, month));
    }
    
    // Export dữ liệu attendance ra Excel
    @GetMapping("/export/excel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam int year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) UUID userId) {
        
        byte[] excelContent = exportUtil.exportAttendanceToExcel(year, month, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String filename = "attendance_report_" + year;
        if (month != null) filename += "_" + month;
        if (userId != null) filename += "_user_" + userId;
        filename += ".xlsx";
        headers.setContentDispositionFormData("attachment", filename);
        
        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }
    
    // Lấy danh sách attendance có khiếu nại
    @GetMapping("/with-complaints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Attendance>> getAttendanceWithComplaints() {
        return ResponseEntity.ok(attendanceService.getAttendanceWithComplaints());
    }
    
    // Sửa một bản ghi attendance (quyền admin)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Attendance> updateAttendance(
            @PathVariable UUID id,
            @RequestBody Attendance attendance) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, attendance));
    }
}
