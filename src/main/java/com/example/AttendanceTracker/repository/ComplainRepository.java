package com.example.attendanceTracker.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.Complain;
import com.example.attendanceTracker.model.StatusComplain;
import com.example.attendanceTracker.model.User;

@Repository
public interface ComplainRepository extends JpaRepository<Complain, UUID> {
    List<Complain> findByAttendance(Attendance attendance);
    List<Complain> findByStatus(StatusComplain status);
    List<Complain> findByAttendance_User(User user);
    List<Complain> findByAttendance_UserAndStatus(User user, StatusComplain status);
    Page<Complain> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Methods excluding deleted users
    List<Complain> findByAttendance_UserIsDeletedFalseOrAttendance_UserIsDeletedIsNull();
    List<Complain> findByStatusAndAttendance_UserIsDeletedFalseOrAttendance_UserIsDeletedIsNull(StatusComplain status);
    Page<Complain> findByCreatedAtBetweenAndAttendance_UserIsDeletedFalseOrAttendance_UserIsDeletedIsNull(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
