package com.example.AttendanceTracker.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
}
