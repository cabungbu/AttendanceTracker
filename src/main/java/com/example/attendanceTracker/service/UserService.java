package com.example.attendanceTracker.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.attendanceTracker.DTO.CreateUserDto;
import com.example.attendanceTracker.model.Role;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.UserRepository;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserByJwt(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return userRepository.findByEmail(email)
            .orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy user với email: " + email
            )
        );
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByEmail(String email) {
         return userRepository.findByEmail(email)
            .orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy user với email: " + email
            )
        );
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() ->  new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy user với id: " + id)
            );
    }

    public User saveProfile(User user) {
        return userRepository.save(user);
    }


    @Transactional
    public User createUser(CreateUserDto dto) {
        userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
            throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Đã tồn tại người dùng với email: " + dto.getEmail()
            );
        });
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setRole(Role.valueOf(dto.getRole().toLowerCase()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, User updated) {
        User user = findById(id); // method đã có sẵn hoặc bạn thêm vào
        if (updated.getName() != null) {
            user.setName(updated.getName());
        }

        if (updated.getAvatarUrl() != null) {
            user.setAvatarUrl(updated.getAvatarUrl());
        }

        if (updated.getDateOfBirth() != null) {
            user.setDateOfBirth(updated.getDateOfBirth());
        }

        if (updated.getRole() != null) {
            user.setRole(updated.getRole());
        }
        return userRepository.save(user);
    }
}
