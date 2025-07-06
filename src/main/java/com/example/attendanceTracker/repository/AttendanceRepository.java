package com.example.attendanceTracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    
    // Pagination methods
    Page<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Attendance> findByCheckInBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Attendance> findByCheckInAfter(LocalDateTime from, Pageable pageable);
    Page<Attendance> findByCheckInBefore(LocalDateTime to, Pageable pageable);
    Page<Attendance> findByUser(User user, Pageable pageable);
    Page<Attendance> findByUserAndCheckInAfter(User user, LocalDateTime from, Pageable pageable);
    Page<Attendance> findByUserAndCheckInBefore(User user, LocalDateTime to, Pageable pageable);
    
    // Alternative method to find attendance for a specific date
    @Query("SELECT a FROM Attendance a WHERE a.user = :user AND a.checkIn >= :startOfDay AND a.checkIn < :endOfDay")
    Optional<Attendance> findByUserAndDate(@Param("user") User user, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // Methods excluding deleted users
    List<Attendance> findByCheckInBetweenAndUserIsDeletedFalseOrUserIsDeletedIsNull(LocalDateTime start, LocalDateTime end);
    List<Attendance> findByCheckInAfterAndUserIsDeletedFalseOrUserIsDeletedIsNull(LocalDateTime from);
    List<Attendance> findByCheckInBeforeAndUserIsDeletedFalseOrUserIsDeletedIsNull(LocalDateTime to);
    List<Attendance> findByUserIsDeletedFalseOrUserIsDeletedIsNull();
    
    // Pagination methods excluding deleted users
    Page<Attendance> findByCheckInBetweenAndUserIsDeletedFalseOrUserIsDeletedIsNull(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Attendance> findByCheckInAfterAndUserIsDeletedFalseOrUserIsDeletedIsNull(LocalDateTime from, Pageable pageable);
    Page<Attendance> findByCheckInBeforeAndUserIsDeletedFalseOrUserIsDeletedIsNull(LocalDateTime to, Pageable pageable);
    Page<Attendance> findByUserIsDeletedFalseOrUserIsDeletedIsNull(Pageable pageable);
}