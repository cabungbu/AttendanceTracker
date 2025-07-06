package com.example.attendanceTracker.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.attendanceTracker.model.User;

public class AttendanceDTO {
    private UUID id;
    private User user;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String checkInImageUrl;
    private String checkOutImageUrl;
    
    // Constructors
    public AttendanceDTO() {}
    
    public AttendanceDTO(UUID id, User user, LocalDateTime checkIn, LocalDateTime checkOut, 
                        String checkInImageUrl, String checkOutImageUrl) {
        this.id = id;
        this.user = user;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.checkInImageUrl = checkInImageUrl;
        this.checkOutImageUrl = checkOutImageUrl;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDateTime getCheckIn() {
        return checkIn;
    }
    
    public void setCheckIn(LocalDateTime checkIn) {
        this.checkIn = checkIn;
    }
    
    public LocalDateTime getCheckOut() {
        return checkOut;
    }
    
    public void setCheckOut(LocalDateTime checkOut) {
        this.checkOut = checkOut;
    }
    
    public String getCheckInImageUrl() {
        return checkInImageUrl;
    }
    
    public void setCheckInImageUrl(String checkInImageUrl) {
        this.checkInImageUrl = checkInImageUrl;
    }
    
    public String getCheckOutImageUrl() {
        return checkOutImageUrl;
    }
    
    public void setCheckOutImageUrl(String checkOutImageUrl) {
        this.checkOutImageUrl = checkOutImageUrl;
    }
}
