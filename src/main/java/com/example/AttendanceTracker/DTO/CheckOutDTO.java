package com.example.AttendanceTracker.DTO;

import org.springframework.web.multipart.MultipartFile;

public class CheckOutDTO {
    private MultipartFile checkOutImage;
    private String location;
    private String notes;

    public MultipartFile getCheckOutImage() {
        return checkOutImage;
    }

    public void setCheckOutImage(MultipartFile checkOutImage) {
        this.checkOutImage = checkOutImage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
