package com.example.AttendanceTracker.DTO;

import com.example.AttendanceTracker.model.User;

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
