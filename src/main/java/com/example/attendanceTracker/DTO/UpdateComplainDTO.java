package com.example.attendanceTracker.DTO;

import org.springframework.web.multipart.MultipartFile;

import com.example.attendanceTracker.model.StatusComplain;

public class UpdateComplainDTO {
    
    private String content;
    private StatusComplain status;
    private MultipartFile complainImage;

    // Default constructor
    public UpdateComplainDTO() {}

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

    public MultipartFile getComplainImage() {
        return complainImage;
    }

    public void setComplainImage(MultipartFile complainImage) {
        this.complainImage = complainImage;
    }
}
