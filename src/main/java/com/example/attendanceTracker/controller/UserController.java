package com.example.attendanceTracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

     @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        System.out.println("✅ /me endpoint called");
        System.out.println("➡️  JWT sub: " + jwt.getSubject());
        System.out.println("➡️  JWT email: " + jwt.getClaim("email"));
        return userService.getOrCreateUserFromToken(jwt);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updated ) {
        User user = userService.findById(id);
    user.setAvatarUrl(updated.getAvatarUrl());
    user.setDateOfBirth(updated.getDateOfBirth());
    user.setRole(updated.getRole());
    return ResponseEntity.ok(userService.update(user));
    }
}