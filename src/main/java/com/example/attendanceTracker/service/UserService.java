package com.example.attendanceTracker.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.example.attendanceTracker.model.Role;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.UserRepository;


@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User saveProfile(User user) {
        return userRepository.save(user);
    }


    public User getOrCreateUserFromToken(Jwt jwt) {
        String sub = jwt.getSubject();
        String email = jwt.getClaim("email");
        String name = jwt.getClaim("name");
        String picture = jwt.getClaim("picture");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwt.getClaims().get("https://attendance.com/roles");

        return userRepository.findBySub(sub)
            .orElseGet(() -> {
                Role role = Role.STAFF; // mặc định
                if (roles != null && roles.contains("ADMIN")) {
                    role = Role.ADMIN;
                }

                User user = new User();
                user.setSub(sub);
                user.setEmail(email);
                user.setName(name);
                user.setAvatarUrl(picture);
                user.setRole(role);
                return userRepository.save(user);
            });
    }

}
