package com.example.attendanceTracker.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.attendanceTracker.model.StatusComplain;

public class ComplainResponseDTO {
    private UUID id;
    private String content;
    private StatusComplain status;
    private String complainImageUrl;
    private LocalDateTime createdAt;
    
    // Attendance object
    private AttendanceDTO attendance;
    
    // Constructors
    public ComplainResponseDTO() {}
    
    public ComplainResponseDTO(UUID id, String content, StatusComplain status, String complainImageUrl, 
                              LocalDateTime createdAt, AttendanceDTO attendance) {
        this.id = id;
        this.content = content;
        this.status = status;
        this.complainImageUrl = complainImageUrl;
        this.createdAt = createdAt;
        this.attendance = attendance;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public StatusComplain getStatus() {
        return status;
    }
    
    public void setStatus(StatusComplain status) {
        this.status = status;
    }
    
    public String getComplainImageUrl() {
        return complainImageUrl;
    }
    
    public void setComplainImageUrl(String complainImageUrl) {
        this.complainImageUrl = complainImageUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public AttendanceDTO getAttendance() {
        return attendance;
    }
    
    public void setAttendance(AttendanceDTO attendance) {
        this.attendance = attendance;
    }
}
