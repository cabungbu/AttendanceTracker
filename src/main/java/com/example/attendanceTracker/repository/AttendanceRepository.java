package com.example.attendanceTracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);
}