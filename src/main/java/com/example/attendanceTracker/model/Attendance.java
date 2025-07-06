package com.example.attendanceTracker.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.AttendanceTracker.model.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance_records")
public class Attendance {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    
    private String checkInImageUrl;
    private String checkOutImageUrl;
    
    @OneToOne(mappedBy = "attendance", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private Complain complain;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;
    // === GETTERS ===

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getCheckIn() {
        return checkIn;
    }

    public LocalDateTime getCheckOut() {
        return checkOut;
    }
    
    public String getCheckInImageUrl() {
        return checkInImageUrl;
    }
    
    public String getCheckOutImageUrl() {
        return checkOutImageUrl;
    }
    
    public Complain getComplain() {
        return complain;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    // === SETTERS ===

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }
    
    public void setCheckInImageUrl(String checkInImageUrl) {
        this.checkInImageUrl = checkInImageUrl;
    }
    
    public void setCheckOutImageUrl(String checkOutImageUrl) {
        this.checkOutImageUrl = checkOutImageUrl;
    }
    
    public void setComplain(Complain complain) {
        this.complain = complain;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
