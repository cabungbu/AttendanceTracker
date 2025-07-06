package com.example.attendanceTracker.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.attendanceTracker.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIsDeletedFalseOrIsDeletedIsNull(String email);
    Optional<User> findByIdAndIsDeletedFalseOrIsDeletedIsNull(UUID id);
    Page<User> findAll(Pageable pageable);
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name, String email, Pageable pageable
    );
    Page<User> findByIsDeletedFalseOrIsDeletedIsNull(Pageable pageable);
    List<User> findByIsDeletedFalseOrIsDeletedIsNull();
}