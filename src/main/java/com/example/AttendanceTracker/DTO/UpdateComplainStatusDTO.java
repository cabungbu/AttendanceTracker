package com.example.attendanceTracker.DTO;

import jakarta.validation.constraints.NotNull;

import com.example.attendanceTracker.model.StatusComplain;

public class UpdateComplainStatusDTO {
    
    @NotNull(message = "Status is required")
    private StatusComplain status;

    public StatusComplain getStatus() {
        return status;
    }

    public void setStatus(StatusComplain status) {
        this.status = status;
    }
}
