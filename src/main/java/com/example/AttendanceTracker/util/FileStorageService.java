package com.example.attendanceTracker.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final SupabaseStorageService supabaseStorageService;
    
    @Autowired
    public FileStorageService(SupabaseStorageService supabaseStorageService) {
        this.supabaseStorageService = supabaseStorageService;
    }

    /**
     * Uploads a file to Supabase Storage and returns the URL
     * 
     * @param file The file to upload
     * @param fileName Custom base filename (without extension)
     * @return The URL of the uploaded file
     */
    public String storeFile(MultipartFile file, String fileName) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        try {
            // Thêm extension vào fileName
            String extension = getFileExtension(file.getOriginalFilename());
            String finalFileName = fileName + extension;
            
            // Upload lên Supabase và trả về URL
            return supabaseStorageService.uploadFile(file, finalFileName);
        } catch (Exception ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }
    
    /**
     * Deletes a file from storage by URL
     * 
     * @param fileUrl The URL of the file to delete
     * @return true if successful
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        try {
            return supabaseStorageService.deleteFile(fileUrl);
        } catch (Exception ex) {
            throw new RuntimeException("Could not delete file. Please try again!", ex);
        }
    }
    
    /**
     * Gets the file extension from a filename
     * 
     * @param fileName The filename
     * @return The extension with dot (e.g., ".jpg")
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
    }
}
