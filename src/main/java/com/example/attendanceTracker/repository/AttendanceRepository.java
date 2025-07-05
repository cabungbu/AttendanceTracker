package com.example.attendanceTracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Methods for user-specific queries with date filtering
    List<Attendance> findByUserAndCheckInAfter(User user, LocalDateTime from);
    List<Attendance> findByUserAndCheckInBefore(User user, LocalDateTime to);
    
    // // Find today's attendance record for a user
    // @Query("SELECT a FROM Attendance a WHERE a.user = :user AND DATE(a.checkIn) = CURRENT_DATE")
    // Optional<Attendance> findTodayAttendanceByUser(@Param("user") User user);
    
    // Alternative method to find attendance for a specific date
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.checkIn >= :startOfDay AND a.checkIn < :endOfDay")
    Optional<Attendance> findByUserAndDate(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}