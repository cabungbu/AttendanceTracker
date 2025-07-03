package com.example.AttendanceTracker.DTO;

import org.springframework.web.multipart.MultipartFile;

public class CheckInDTO {
    private MultipartFile checkInImage;
    private String location;
    private String notes;

    public MultipartFile getCheckInImage() {
        return checkInImage;
    }

    public void setCheckInImage(MultipartFile checkInImage) {
        this.checkInImage = checkInImage;
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
