package com.example.attendanceTracker.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);
}