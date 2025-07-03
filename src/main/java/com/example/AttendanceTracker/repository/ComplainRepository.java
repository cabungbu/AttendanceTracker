package com.example.AttendanceTracker.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.AttendanceTracker.model.Attendance;
import com.example.AttendanceTracker.model.Complain;
import com.example.AttendanceTracker.model.StatusComplain;
import com.example.AttendanceTracker.model.User;

@Repository
public interface ComplainRepository extends JpaRepository<Complain, UUID> {
    List<Complain> findByAttendance(Attendance attendance);
    List<Complain> findByStatus(StatusComplain status);
    List<Complain> findByAttendance_User(User user);
    List<Complain> findByAttendance_UserAndStatus(User user, StatusComplain status);
}
