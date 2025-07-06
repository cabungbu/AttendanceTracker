package com.example.attendanceTracker.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    
    @OneToMany(mappedBy = "attendance", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Complain> complains;

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
    
    public List<Complain> getComplains() {
        return complains;
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
    
    public void setComplains(List<Complain> complains) {
        this.complains = complains;
    }
}
