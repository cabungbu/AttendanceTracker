package com.example.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.User;
import com.example.service.UserService;

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
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public ResponseEntity<User> me(Authentication auth) {
        User user = userService.getOrCreateUserFromToken(auth);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updated ) {
        User user = userService.findById(id); // ✅ Lấy user theo id, không cần dùng auth
    user.setAvatarUrl(updated.getAvatarUrl());
    user.setDateOfBirth(updated.getDateOfBirth());
    user.setRole(updated.getRole());
    return ResponseEntity.ok(userService.update(user));
    }
}