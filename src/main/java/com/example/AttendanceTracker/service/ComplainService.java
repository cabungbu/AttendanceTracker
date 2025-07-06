package com.example.attendanceTracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.attendanceTracker.DTO.AttendanceDTO;
import com.example.attendanceTracker.DTO.ComplainResponseDTO;
import com.example.attendanceTracker.DTO.CreateComplainDTO;
import com.example.attendanceTracker.DTO.UpdateComplainDTO;
import com.example.attendanceTracker.DTO.UpdateComplainStatusDTO;
import com.example.attendanceTracker.model.Attendance;
import com.example.attendanceTracker.model.Complain;
import com.example.attendanceTracker.model.StatusComplain;
import com.example.attendanceTracker.model.User;
import com.example.attendanceTracker.repository.AttendanceRepository;
import com.example.attendanceTracker.repository.ComplainRepository;
import com.example.attendanceTracker.util.FileStorageService;

@Service
public class ComplainService {
    
    private final ComplainRepository complainRepository;
    private final AttendanceRepository attendanceRepository;
    private final FileStorageService fileStorageService;
    
    public ComplainService(ComplainRepository complainRepository, AttendanceRepository attendanceRepository, 
                          FileStorageService fileStorageService) {
        this.complainRepository = complainRepository;
        this.attendanceRepository = attendanceRepository;
        this.fileStorageService = fileStorageService;
    }
    
    @Transactional
    public Complain createComplain(User user, CreateComplainDTO dto) {
        Attendance attendance = attendanceRepository.findById(dto.getAttendanceId())
                .orElseThrow(() -> new RuntimeException("Attendance record not found"));
        
        if (attendance.getComplain() != null) {
            throw new RuntimeException("Bạn đã gửi khiếu nại cho bản ghi này rồi");
        }

        // Check if the attendance belongs to the user
        if (!attendance.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only create complaints for your own attendance records");
        }
        
        Complain complain = new Complain();
        complain.setAttendance(attendance);
        complain.setContent(dto.getContent());
        complain.setStatus(StatusComplain.PENDING);
        complain.setCreatedAt(LocalDateTime.now());
        
        // Xử lý upload hình ảnh khiếu nại nếu có
        if (dto.getComplainImage() != null && !dto.getComplainImage().isEmpty()) {
            String imageUrl = fileStorageService.storeFile(dto.getComplainImage(), 
                    "complain_" + user.getId() + "_" + System.currentTimeMillis());
            complain.setComplainImageUrl(imageUrl);
        }
        attendance.setComplain(complain);
        return complainRepository.save(complain);
    }
    
    public List<Complain> getComplainsByUser(User user) {
        return complainRepository.findByAttendance_User(user);
    }
    
    public List<Complain> getComplainsByStatus(StatusComplain status) {
        return complainRepository.findByStatusAndAttendance_UserIsDeletedFalseOrAttendance_UserIsDeletedIsNull(status);
    }
    
    public List<Complain> getComplainsByAttendance(UUID attendanceId) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi điểm danh"));
        return complainRepository.findByAttendance(attendance);
    }

    public List<ComplainResponseDTO> getComplainsByAttendanceAsDTO(UUID attendanceId) {
        return getComplainsByAttendance(attendanceId).stream()
                .filter(complain -> !complain.getAttendance().getUser().getIsDeleted()) // Filter deleted users
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
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
    
    public ComplainResponseDTO getComplainByIdAsDTO(UUID id) {
        Complain complain = getComplainById(id);
        return convertToResponseDTO(complain);
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
        return complainRepository.findByAttendance_UserIsDeletedFalseOrAttendance_UserIsDeletedIsNull();
    }
    
    @Transactional
    public Complain updateComplainImage(UUID complainId, MultipartFile complainImage, User user) {
        Complain complain = complainRepository.findById(complainId)
                .orElseThrow(() -> new RuntimeException("Complain not found"));
        
        // Kiểm tra quyền: chỉ cho phép chủ sở hữu hoặc admin cập nhật
        boolean isAdmin = user.getRole().toString().equalsIgnoreCase("admin");
        boolean isOwner = complain.getAttendance().getUser().getId().equals(user.getId());
        
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You can only update your own complaints");
        }
        
        // Chỉ cho phép cập nhật nếu khiếu nại đang ở trạng thái PENDING
        if (!complain.getStatus().equals(StatusComplain.PENDING)) {
            throw new RuntimeException("You can only update pending complaints");
        }
        
        // Xử lý upload hình ảnh mới
        if (complainImage != null && !complainImage.isEmpty()) {
            String imageUrl = fileStorageService.storeFile(complainImage, 
                    "complain_update_" + user.getId() + "_" + System.currentTimeMillis());
            complain.setComplainImageUrl(imageUrl);
        }
        
        return complainRepository.save(complain);
    }
    
    @Transactional
    public Complain updateComplain(UUID complainId, UpdateComplainDTO dto, User user) {
        Complain complain = complainRepository.findById(complainId)
                .orElseThrow(() -> new RuntimeException("Complain not found"));
        
        // Kiểm tra quyền: chỉ cho phép chủ sở hữu hoặc admin cập nhật
        boolean isAdmin = user.getRole().toString().equalsIgnoreCase("admin");
        boolean isOwner = complain.getAttendance().getUser().getId().equals(user.getId());
        
        if (!isAdmin && !isOwner) {
            throw new RuntimeException("You can only update your own complaints");
        }
        
        // Cập nhật nội dung khiếu nại
        if (dto.getContent() != null && !dto.getContent().trim().isEmpty()) {
            // Chỉ cho phép cập nhật content nếu đang ở trạng thái PENDING
            if (!complain.getStatus().equals(StatusComplain.PENDING) && !isAdmin) {
                throw new RuntimeException("You can only update content for pending complaints");
            }
            complain.setContent(dto.getContent().trim());
        }
        
        // Cập nhật trạng thái (chỉ admin mới được phép)
        if (dto.getStatus() != null) {
            if (!isAdmin) {
                throw new RuntimeException("Only admin can update complaint status");
            }
            complain.setStatus(dto.getStatus());
        }
        
        // Cập nhật hình ảnh khiếu nại
        if (dto.getComplainImage() != null && !dto.getComplainImage().isEmpty()) {
            // Chỉ cho phép cập nhật hình ảnh nếu đang ở trạng thái PENDING hoặc là admin
            if (!complain.getStatus().equals(StatusComplain.PENDING) && !isAdmin) {
                throw new RuntimeException("You can only update image for pending complaints");
            }
            String imageUrl = fileStorageService.storeFile(dto.getComplainImage(), 
                    "complain_update_" + user.getId() + "_" + System.currentTimeMillis());
            complain.setComplainImageUrl(imageUrl);
        }
        
        return complainRepository.save(complain);
    }

    public Page<Complain> getComplainsByMonthAndYear(int year, int month, Pageable pageable) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        return complainRepository.findByCreatedAtBetweenAndAttendance_UserIsDeletedFalseOrAttendance_UserIsDeletedIsNull(start, end, pageable);
    }

    // Convert Complain to ComplainResponseDTO
    public ComplainResponseDTO convertToResponseDTO(Complain complain) {
        ComplainResponseDTO dto = new ComplainResponseDTO();
        dto.setId(complain.getId());
        dto.setContent(complain.getContent());
        dto.setStatus(complain.getStatus());
        dto.setComplainImageUrl(complain.getComplainImageUrl());
        dto.setCreatedAt(complain.getCreatedAt());
        
        // Convert Attendance to AttendanceDTO
        if (complain.getAttendance() != null) {
            AttendanceDTO attendanceDTO = new AttendanceDTO();
            attendanceDTO.setId(complain.getAttendance().getId());
            attendanceDTO.setUser(complain.getAttendance().getUser());
            attendanceDTO.setCheckIn(complain.getAttendance().getCheckIn());
            attendanceDTO.setCheckOut(complain.getAttendance().getCheckOut());
            attendanceDTO.setCheckInImageUrl(complain.getAttendance().getCheckInImageUrl());
            attendanceDTO.setCheckOutImageUrl(complain.getAttendance().getCheckOutImageUrl());
            
            dto.setAttendance(attendanceDTO);
        }
        
        return dto;
    }

    // Get all complaints as DTO
    public List<ComplainResponseDTO> getAllComplaintsAsDTO() {
        return getAllComplaints().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get complaints by status as DTO
    public List<ComplainResponseDTO> getComplainsByStatusAsDTO(StatusComplain status) {
        return getComplainsByStatus(status).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get complaints by user as DTO
    public List<ComplainResponseDTO> getComplainsByUserAsDTO(User user) {
        return getComplainsByUser(user).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get complaints by month and year as DTO
    public Page<ComplainResponseDTO> getComplainsByMonthAndYearAsDTO(int year, int month, Pageable pageable) {
        Page<Complain> complainsPage = getComplainsByMonthAndYear(year, month, pageable);
        return complainsPage.map(this::convertToResponseDTO);
    }

}
