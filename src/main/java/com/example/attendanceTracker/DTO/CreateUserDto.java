package com.example.attendanceTracker.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.attendanceTracker.model.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

public class CreateUserDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String name;

    private String avatarUrl;

    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private java.time.LocalDate dateOfBirth;

    @NotBlank
    private String role;

    @NotBlank
    private String position;

    private boolean isDeleted = false;

    private java.time.LocalDate deletedDate;

    private String phoneNumber;

    private String address;

    private Gender gender;

   // === GETTERS ===
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public java.time.LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getRole() { return role; }
    public String getPosition() { return position; }
    public boolean isDeleted() { return isDeleted; }
    public java.time.LocalDate getDeletedDate() { return deletedDate; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public Gender getGender() { return gender; }

    // === SETTERS ===
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setDateOfBirth(java.time.LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setRole(String role) { this.role = role; }
    public void setPosition(String position) { this.position = position; }
    public void setDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
    public void setDeletedDate(java.time.LocalDate deletedDate) { this.deletedDate = deletedDate; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setGender(Gender gender) { this.gender = gender; }
}
