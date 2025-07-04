package com.example.attendanceTracker.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendanceTracker.DTO.CreateComplainDTO;
import com.example.attendanceTracker.DTO.UpdateComplainStatusDTO;
import com.example.attendanceTracker.model.Complain;
import com.example.attendanceTracker.model.StatusComplain;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.service.ComplainService;
import com.example.attendanceTracker.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/complains")
public class ComplainController {
    
    private final ComplainService complainService;
    private final UserService userService;
    
    public ComplainController(ComplainService complainService, UserService userService) {
        this.complainService = complainService;
        this.userService = userService;
    }
    
    // Gửi khiếu nại
    @PostMapping
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Complain> createComplain(
            @Valid @RequestBody CreateComplainDTO dto,
            Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        Complain complain = complainService.createComplain(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(complain);
    }
    
    // Lấy danh sách khiếu nại của user đang đăng nhập
    @GetMapping("/my-complains")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<List<Complain>> getMyComplaints(Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        return ResponseEntity.ok(complainService.getComplainsByUser(user));
    }
    
    // Lấy danh sách tất cả khiếu nại (cho admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Complain>> getAllComplaints(
            @RequestParam(required = false) StatusComplain status) {
        if (status != null) {
            return ResponseEntity.ok(complainService.getComplainsByStatus(status));
        }
        return ResponseEntity.ok(complainService.getAllComplaints());
    }
    
    // Lấy chi tiết 1 khiếu nại
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Complain> getComplaintById(@PathVariable UUID id, Authentication auth) {
        Complain complain = complainService.getComplainById(id);
        
        // Kiểm tra quyền truy cập
        User user = userService.findByEmail(auth.getName());
        boolean isAdmin = user.getRole().toString().equalsIgnoreCase("ADMIN");
        boolean isOwner = complain.getAttendance().getUser().getId().equals(user.getId());
        
        if (!isAdmin && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(complain);
    }
    
    // Duyệt hoặc từ chối khiếu nại
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Complain> updateComplaintStatus(
            @PathVariable UUID id, 
            @Valid @RequestBody UpdateComplainStatusDTO dto) {
        Complain complain = complainService.updateComplainStatus(id, dto);
        return ResponseEntity.ok(complain);
    }
    
    // Xóa khiếu nại (chỉ xóa được khiếu nại của chính mình và đang ở trạng thái PENDING)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Void> deleteComplaint(@PathVariable UUID id, Authentication auth) {
        User user = userService.findByEmail(auth.getName());
        complainService.deleteComplain(id, user);
        return ResponseEntity.noContent().build();
    }
    
    // Lấy tất cả khiếu nại liên quan đến 1 bản ghi attendance
    @GetMapping("/by-attendance/{attendanceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Complain>> getComplaintsByAttendance(@PathVariable UUID attendanceId) {
        return ResponseEntity.ok(complainService.getComplainsByAttendance(attendanceId));
    }
    
    // Thống kê số lượng khiếu nại theo trạng thái
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<StatusComplain, Long>> getComplaintStats() {
        List<Complain> allComplaints = complainService.getAllComplaints();
        
        java.util.Map<StatusComplain, Long> stats = new java.util.HashMap<>();
        stats.put(StatusComplain.PENDING, allComplaints.stream()
                .filter(c -> c.getStatus() == StatusComplain.PENDING).count());
        stats.put(StatusComplain.APPROVED, allComplaints.stream()
                .filter(c -> c.getStatus() == StatusComplain.APPROVED).count());
        stats.put(StatusComplain.REJECTED, allComplaints.stream()
                .filter(c -> c.getStatus() == StatusComplain.REJECTED).count());
        
        return ResponseEntity.ok(stats);
    }
}
