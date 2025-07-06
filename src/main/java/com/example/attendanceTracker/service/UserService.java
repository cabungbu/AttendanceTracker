package com.example.attendanceTracker.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.attendanceTracker.DTO.CreateUserDto;
import com.example.attendanceTracker.DTO.UpdateUserDTO;
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
        return userRepository.findByIsDeletedFalseOrIsDeletedIsNull();
    }

    public Page<User> listUsers(Pageable pageable, String search) {
        if (search == null || search.isBlank()) {
            return userRepository.findByIsDeletedFalseOrIsDeletedIsNull(pageable);
        }
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search.trim(), search.trim(), pageable);
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
        user.setPosition(dto.getPosition());
        user.setRole(Role.valueOf(dto.getRole().toLowerCase()));
        user.setIsDeleted(dto.isDeleted());
        user.setDeletedDate(dto.getDeletedDate());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setGender(dto.getGender());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(UUID id, UpdateUserDTO updated) {
        User user = findById(id);
        if (updated.getName() != null) {
            user.setName(updated.getName());
        }
        if (updated.getEmail() != null) {
            user.setEmail(updated.getEmail());
        }
        if (updated.getAvatarUrl() != null) {
            user.setAvatarUrl(updated.getAvatarUrl());
        }
        if (updated.getDateOfBirth() != null) {
            user.setDateOfBirth(updated.getDateOfBirth());
        }
        if (updated.getPosition() != null) {
            user.setPosition(updated.getPosition());
        }
        if (updated.getRole() != null) {
            user.setRole(updated.getRole());
        }
        if (updated.getIsDeleted()) {
            user.setIsDeleted(true);
            if (updated.getDeletedDate() != null) {
                user.setDeletedDate(updated.getDeletedDate());
            }
        }
        if (updated.getPhoneNumber() != null) {
            user.setPhoneNumber(updated.getPhoneNumber());
        }
        if (updated.getAddress() != null) {
            user.setAddress(updated.getAddress());
        }
        if (updated.getGender() != null) {
            user.setGender(updated.getGender());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = findById(id);  
        user.setIsDeleted(true);
        user.setDeletedDate(LocalDate.now());
        userRepository.save(user);
    }
}
