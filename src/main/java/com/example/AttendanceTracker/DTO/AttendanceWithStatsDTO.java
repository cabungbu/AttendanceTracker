package com.example.attendanceTracker.DTO;

import java.util.List;

import com.example.attendanceTracker.model.Attendance;

public class AttendanceWithStatsDTO {

    private List<Attendance> records;
    private double totalHours;
    private double averageHoursPerDay;
    private int presentDays;
    private int violationDays;

    public AttendanceWithStatsDTO() {}

    public AttendanceWithStatsDTO(List<Attendance> records, double totalHours, double averageHoursPerDay, int presentDays, int violationDays) {
        this.records = records;
        this.totalHours = totalHours;
        this.averageHoursPerDay = averageHoursPerDay;
        this.presentDays = presentDays;
        this.violationDays = violationDays;
    }

    public List<Attendance> getRecords() {
        return records;
    }

    public void setRecords(List<Attendance> records) {
        this.records = records;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getAverageHoursPerDay() {
        return averageHoursPerDay;
    }

    public void setAverageHoursPerDay(double averageHoursPerDay) {
        this.averageHoursPerDay = averageHoursPerDay;
    }

    public int getPresentDays() {
        return presentDays;
    }

    public void setPresentDays(int presentDays) {
        this.presentDays = presentDays;
    }

    public int getViolationDays() {
        return violationDays;
    }

    public void setViolationDays(int violationDays) {
        this.violationDays = violationDays;
    }
}
