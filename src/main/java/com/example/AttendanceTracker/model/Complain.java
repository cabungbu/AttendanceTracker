package com.example.attendanceTracker.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "complains")
public class Complain {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "attendance_id", nullable = false)
    @JsonBackReference
    private Attendance attendance;
    
    @Column(nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    private StatusComplain status = StatusComplain.PENDING;
    
    private String complainImageUrl;
    
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getComplainImageUrl() {
        return complainImageUrl;
    }

    public void setComplainImageUrl(String complainImageUrl) {
        this.complainImageUrl = complainImageUrl;
    }
}
