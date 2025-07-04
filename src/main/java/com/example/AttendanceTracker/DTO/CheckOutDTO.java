package com.example.AttendanceTracker.DTO;

import org.springframework.web.multipart.MultipartFile;

public class CheckOutDTO {
    private MultipartFile checkOutImage;

    public MultipartFile getCheckOutImage() {
        return checkOutImage;
    }

    public void setCheckOutImage(MultipartFile checkOutImage) {
        this.checkOutImage = checkOutImage;
    }
}
