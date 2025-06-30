package com.example.repository;

import com.example.model.Attendance;
import com.example.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);
}