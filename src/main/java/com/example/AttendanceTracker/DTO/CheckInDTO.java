package com.example.AttendanceTracker.DTO;

import org.springframework.web.multipart.MultipartFile;

public class CheckInDTO {
    private MultipartFile checkInImage;

    public MultipartFile getCheckInImage() {
        return checkInImage;
    }

    public void setCheckInImage(MultipartFile checkInImage) {
        this.checkInImage = checkInImage;
    }
}
