package com.example.attendanceTracker.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {
    private static final Logger logger = LoggerFactory.getLogger(SupabaseStorageService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket:attendencetracker}")
    private String bucketName;
    
    private final RestTemplate restTemplate;
    
    public SupabaseStorageService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Uploads a file to Supabase Storage and returns the public URL
     * 
     * @param file The file to upload
     * @param filename Custom filename (optional)
     * @return The public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String filename) {
        try {
            logger.info("Starting file upload to Supabase, bucket: {}", bucketName);
            logger.debug("Using Supabase URL: {}", supabaseUrl);
            
            // Tạo tên file duy nhất nếu không có tên file
            if (filename == null || filename.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                filename = UUID.randomUUID().toString() + extension;
            }
            
            logger.info("Uploading file with name: {}", filename);
            
            // URL encode filename
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            
            // Endpoint để upload file
            String endpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + encodedFilename;
            logger.debug("Upload endpoint: {}", endpoint);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));
            logger.debug("File content type: {}", file.getContentType());
            
            // Create request entity
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            
            // Execute the request
            logger.info("Sending request to Supabase...");
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    requestEntity,
                    String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + encodedFilename;
                logger.info("File upload successful, public URL: {}", publicUrl);
                return publicUrl;
            } else {
                logger.error("File upload failed. Status code: {}, Response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to upload file to Supabase. Status code: " + 
                        response.getStatusCode() + ", Response: " + response.getBody());
            }
        } catch (Exception e) {
            logger.error("Error uploading file to Supabase", e);
            throw new RuntimeException("Failed to upload file to Supabase: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes a file from Supabase Storage
     * 
     * @param fileUrl The URL of the file to delete
     * @return true if deleted successfully
     */
    public boolean deleteFile(String fileUrl) {
        try {
            logger.info("Starting file deletion from Supabase, URL: {}", fileUrl);
            
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.toString());
            
            // Endpoint để xóa file
            String endpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + encodedFilename;
            logger.debug("Delete endpoint: {}", endpoint);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            
            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            // Execute the request
            logger.info("Sending delete request to Supabase...");
            ResponseEntity<Void> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.DELETE,
                    requestEntity,
                    Void.class);
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            logger.info("File deletion {}", success ? "successful" : "failed");
            return success;
        } catch (Exception e) {
            logger.error("Error deleting file from Supabase", e);
            throw new RuntimeException("Failed to delete file from Supabase: " + e.getMessage(), e);
        }
    }
}
