package com.example.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;


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

   
   public User getOrCreateUserFromToken(Authentication authentication) {
        JwtAuthenticationToken jwt = (JwtAuthenticationToken) authentication;
        String sub = jwt.getToken().getSubject();
        String email = (String) jwt.getToken().getClaims().get("email");
        String name = (String) jwt.getToken().getClaims().get("name");
        String picture = (String) jwt.getToken().getClaims().get("picture");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) jwt.getToken().getClaims().get("roles");

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
