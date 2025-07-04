package com.example.attendanceTracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.attendanceTracker.DTO.CreateComplainDTO;
import com.example.attendanceTracker.DTO.UpdateComplainStatusDTO;
import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.Complain;
import com.example.attendanceTracker.model.StatusComplain;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.AttendanceRepository;
import com.example.attendanceTracker.repository.ComplainRepository;

@Service
public class ComplainService {
    
    private final ComplainRepository complainRepository;
    private final AttendanceRepository attendanceRepository;
    
    public ComplainService(ComplainRepository complainRepository, AttendanceRepository attendanceRepository) {
        this.complainRepository = complainRepository;
        this.attendanceRepository = attendanceRepository;
    }
    
    @Transactional
    public Complain createComplain(User user, CreateComplainDTO dto) {
        Attendance attendance = attendanceRepository.findById(dto.getAttendanceId())
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        
        // Check if the attendance belongs to the user
        if (!attendance.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only create complaints for your own attendance records");
        }
        
        Complain complain = new Complain();
        complain.setAttendance(attendance);
        complain.setContent(dto.getContent());
        complain.setStatus(StatusComplain.PENDING);
        complain.setCreatedAt(LocalDateTime.now());
        
        return complainRepository.save(complain);
    }
    
    public List<Complain> getComplainsByUser(User user) {
        return complainRepository.findByAttendance_User(user);
    }
    
    public List<Complain> getComplainsByStatus(StatusComplain status) {
        return complainRepository.findByStatus(status);
    }
    
    public List<Complain> getComplainsByAttendance(UUID attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        return complainRepository.findByAttendance(attendance);
    }
    
    @Transactional
    public Complain updateComplainStatus(UUID complainId, UpdateComplainStatusDTO dto) {
        Complain complain = complainRepository.findById(complainId)
                .orElseThrow(() -> new RuntimeException("Complain not found"));
        
        complain.setStatus(dto.getStatus());
        
        return complainRepository.save(complain);
    }
    
    public Complain getComplainById(UUID id) {
        return complainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complain not found"));
    }
    
    @Transactional
    public void deleteComplain(UUID id, User user) {
        Complain complain = complainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Complain not found"));
        
        // Only allow deletion if it's the user's own complain and it's still pending
        if (!complain.getAttendance().getUser().getId().equals(user.getId()) && 
            !complain.getStatus().equals(StatusComplain.PENDING)) {
            throw new RuntimeException("You can only delete your own pending complaints");
        }
        
        complainRepository.delete(complain);
    }
    
    public List<Complain> getAllComplaints() {
        return complainRepository.findAll();
    }
}
