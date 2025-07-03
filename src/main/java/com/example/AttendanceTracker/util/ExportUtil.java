package com.example.AttendanceTracker.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.example.AttendanceTracker.model.Attendance;
import com.example.AttendanceTracker.service.AttendanceService;

import java.io.ByteArrayOutputStream;

@Component
public class ExportUtil {
    
    private final AttendanceService attendanceService;
    
    public ExportUtil(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    
    public byte[] exportAttendanceToExcel(int year, Integer month, UUID userId) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // Create sheet
            Sheet sheet = workbook.createSheet("Attendance Report");
            
            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFont(headerFont);
            
            // Create date style
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy hh:mm:ss"));
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"User ID", "User Name", "Email", "Check In", "Check Out", "Duration (hours)"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Get attendance data
            List<Attendance> attendances;
            if (month != null && userId != null) {
                // Get monthly report for specific user
                attendances = attendanceService.getMonthlyAttendanceForUser(userId, year, month);
            } else if (month != null) {
                // Get monthly report for all users
                attendances = attendanceService.getMonthlyAttendanceForAllUsers(year, month);
            } else if (userId != null) {
                // Get yearly report for specific user
                attendances = attendanceService.getYearlyAttendanceForUser(userId, year);
            } else {
                // Get yearly report for all users
                attendances = attendanceService.getYearlyAttendanceForAllUsers(year);
            }
            
            // Fill data rows
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Attendance attendance : attendances) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(attendance.getUser().getId().toString());
                row.createCell(1).setCellValue(attendance.getUser().getName());
                row.createCell(2).setCellValue(attendance.getUser().getEmail());
                
                if (attendance.getCheckIn() != null) {
                    Cell checkInCell = row.createCell(3);
                    checkInCell.setCellValue(attendance.getCheckIn().format(formatter));
                }
                
                if (attendance.getCheckOut() != null) {
                    Cell checkOutCell = row.createCell(4);
                    checkOutCell.setCellValue(attendance.getCheckOut().format(formatter));
                    
                    // Calculate hours
                    LocalDateTime checkIn = attendance.getCheckIn();
                    LocalDateTime checkOut = attendance.getCheckOut();
                    if (checkIn != null && checkOut != null) {
                        double hours = (checkOut.toLocalTime().toSecondOfDay() - 
                                       checkIn.toLocalTime().toSecondOfDay()) / 3600.0;
                        row.createCell(5).setCellValue(Math.round(hours * 100.0) / 100.0);
                    }
                }
            }
            
            // Resize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to output stream
            workbook.write(out);
            return out.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to export data to Excel", e);
        }
    }
}
