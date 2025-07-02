package com.example.attendanceTracker.DTO;

import com.example.attendanceTracker.model.User;

public class CreateUserResponse {
    private final User user;
    private final String temporaryPassword;

    public CreateUserResponse(User user, String temporaryPassword) {
        this.user = user;
        this.temporaryPassword = temporaryPassword;
    }

    public User getUser() {
        return user;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }
}
