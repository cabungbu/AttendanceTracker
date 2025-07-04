package com.example.attendanceTracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);
    List<Attendance> findByCheckInBetween(LocalDateTime start, LocalDateTime end);
    List<Attendance> findByCheckInAfter(LocalDateTime from);
    List<Attendance> findByCheckInBefore(LocalDateTime to);
    List<Attendance> findByUser(User user);
}