package com.example.attendanceTracker.DTO;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.attendanceTracker.model.Gender;
import com.example.attendanceTracker.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;

public class UpdateUserDTO {
    @Email
    private String email;

    private String name;

    private String avatarUrl;

    @Past
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private java.time.LocalDate dateOfBirth;

    private Role role;

    private String position;

    private boolean isDeleted;

    private java.time.LocalDate deletedDate;

    private String phoneNumber;

    private String address;

    private Gender gender;

    // === GETTERS ===
    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public java.time.LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

   public Role getRole() { return role; }

    public String getPosition() {
        return position;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public java.time.LocalDate getDeletedDate() {
        return deletedDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public Gender getGender() {
        return gender;
    }

    // === SETTERS ===
    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setDateOfBirth(java.time.LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setRole(Role role) { this.role = role; }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setIsDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    public void setDeletedDate(java.time.LocalDate deletedDate) {
        this.deletedDate = deletedDate;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
