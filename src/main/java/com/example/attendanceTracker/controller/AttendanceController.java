package com.example.attendanceTracker.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/checkin")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Attendance> checkIn(@PathVariable UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(attendanceService.checkIn(user));
    }

    @PostMapping("/checkout/{id}")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<Attendance> checkOut(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.checkOut(id));
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
}
