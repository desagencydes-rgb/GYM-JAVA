package com.gym.app.model;

import java.time.LocalDateTime;

public class AttendanceRecord {
    // Name of the member who checked in
    private String memberName;

    // Time of check-in
    private LocalDateTime checkInTime;

    /**
     * DTO for displaying attendance rows in the UI.
     * 
     * @param memberName  Name of member
     * @param checkInTime Time of check-in
     */
    public AttendanceRecord(String memberName, LocalDateTime checkInTime) {
        this.memberName = memberName;
        this.checkInTime = checkInTime;
    }

    public String getMemberName() {
        return memberName;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }
}
