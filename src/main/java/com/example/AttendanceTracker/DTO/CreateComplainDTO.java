package com.example.attendanceTracker.DTO;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateComplainDTO {
    
    @NotNull(message = "Attendance ID is required")
    private UUID attendanceId;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private MultipartFile complainImage;

    public UUID getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(UUID attendanceId) {
        this.attendanceId = attendanceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MultipartFile getComplainImage() {
        return complainImage;
    }

    public void setComplainImage(MultipartFile complainImage) {
        this.complainImage = complainImage;
    }
}
