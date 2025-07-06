package com.example.attendanceTracker.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.attendanceTracker.DTO.CreateUserDto;
import com.example.attendanceTracker.DTO.UpdateUserDTO;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.service.UserService;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    
    @GetMapping("/me")
    public ResponseEntity<User> getMe(Authentication authentication) {    
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = jwtAuth.getToken();
            try {
                User user = userService.getUserByJwt(jwt);
                return ResponseEntity.ok(user);
            } catch (Exception e) {
                throw e;
            }
        }
        
        throw new RuntimeException("Authentication is not JWT type");
    }

    @PatchMapping("/me")
    public ResponseEntity<User> updateMyProfile(
        JwtAuthenticationToken auth,
        @RequestBody UpdateUserDTO updated) {
        Jwt jwt = auth.getToken();
        String email = jwt.getClaimAsString("email");
        User user = userService.findByEmailExcludeDeleted(email);
        User savedUser = userService.updateUser(user.getId(), updated);
    return ResponseEntity.ok(savedUser);
}

    @GetMapping("/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<User>> allUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    //example: users?page=0&size=10&sort=name,asc&searchkey=abc
    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Page<User>> allUsers(Pageable pageable, @RequestParam(value = "searchkey", required = false) String search) {
        Page<User> page = userService.listUsers(pageable, search);
        return ResponseEntity.ok(page);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody UpdateUserDTO updated ) {
        User savedUser = userService.updateUser(id, updated);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('admin')")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> createUser(@RequestBody CreateUserDto dto) {
        User savedUser = userService.createUser(dto);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}