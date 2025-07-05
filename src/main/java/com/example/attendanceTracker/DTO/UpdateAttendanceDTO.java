package com.example.attendanceTracker.DTO;

import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

public class UpdateAttendanceDTO {
    
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private MultipartFile checkInImage;
    private MultipartFile checkOutImage;

    // Constructors
    public UpdateAttendanceDTO() {}

    // Getters and Setters
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

    public MultipartFile getCheckInImage() {
        return checkInImage;
    }

    public void setCheckInImage(MultipartFile checkInImage) {
        this.checkInImage = checkInImage;
    }

    public MultipartFile getCheckOutImage() {
        return checkOutImage;
    }

    public void setCheckOutImage(MultipartFile checkOutImage) {
        this.checkOutImage = checkOutImage;
    }
}
