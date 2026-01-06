package com.gym.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeService {

    // A mock date used for debugging/testing purposes to simulate different dates
    private static LocalDate mockDate = null;

    /**
     * Sets a mock date to override the system date.
     * 
     * @param date The simulated date to use.
     */
    public static void setMockDate(LocalDate date) {
        mockDate = date;
    }

    /**
     * Resets the mock date, reverting to the actual system date.
     */
    public static void reset() {
        mockDate = null;
    }

    /**
     * Returns the current date.
     * If a mock date is set, returns that instead of the real system date.
     * 
     * @return The effective 'today' date.
     */
    public static LocalDate getToday() {
        if (mockDate != null) {
            return mockDate;
        }
        return LocalDate.now();
    }

    /**
     * Returns the current date-time.
     * uses the mock date (if set) combined with the current time.
     * 
     * @return The effective current LocalDateTime.
     */
    public static LocalDateTime getNow() {
        if (mockDate != null) {
            return LocalDateTime.of(mockDate, LocalTime.now());
        }
        return LocalDateTime.now();
    }
}
