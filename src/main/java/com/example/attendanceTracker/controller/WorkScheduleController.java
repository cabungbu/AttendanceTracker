package com.example.attendanceTracker.controller;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendanceTracker.service.WorkScheduleService;

@RestController
@RequestMapping("/work-schedule")
public class WorkScheduleController {
    
    private final WorkScheduleService workScheduleService;
    
    public WorkScheduleController(WorkScheduleService workScheduleService) {
        this.workScheduleService = workScheduleService;
    }
    
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('staff','admin')")
    public ResponseEntity<Map<String, Object>> getWorkScheduleInfo() {
        Map<String, Object> schedule = new HashMap<>();
        
        LocalTime startTime = workScheduleService.getWorkStartTime();
        LocalTime endTime = workScheduleService.getWorkEndTime();
        LocalTime lateThreshold = workScheduleService.getLateThreshold();
        
        schedule.put("workStartTime", startTime.toString()); // "08:30"
        schedule.put("workEndTime", endTime.toString()); // "17:30"
        schedule.put("lateThreshold", lateThreshold.toString()); // "08:30"
        schedule.put("workHours", 9); // 9 tiếng làm việc
        schedule.put("description", "Ca làm việc từ 8:30 - 17:30, đi trễ nếu sau 8:30");
        
        return ResponseEntity.ok(schedule);
    }
}
