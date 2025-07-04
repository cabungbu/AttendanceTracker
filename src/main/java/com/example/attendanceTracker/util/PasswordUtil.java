package com.example.attendanceTracker.util;

import java.security.SecureRandom;

public class PasswordUtil {
     private static final SecureRandom RANDOM = new SecureRandom();
    private static final int LENGTH = 8;

    public static String generateNumericPassword() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int digit = RANDOM.nextInt(10);
            sb.append(digit);
        }
        return sb.toString();
    }
}
