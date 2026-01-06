package com.gym.app.dao;

import com.gym.app.util.DatabaseHelper;
import com.gym.app.util.TimeService;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for determining attendance.
 * Handles database operations related to checking in and tracking member
 * visits.
 */
public class AttendanceDAO {

    /**
     * Records a new check-in for a member.
     * Uses the current time and date from TimeService.
     *
     * @param memberId The ID of the member checking in.
     */
    public void checkIn(int memberId) {
        // SQL query to insert a new attendance record
        String sql = "INSERT INTO attendance (member_id, check_in_time, date) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set parameters
            pstmt.setInt(1, memberId);
            pstmt.setString(2, TimeService.getNow().toString()); // Capture exact timestamp
            pstmt.setString(3, TimeService.getToday().toString()); // Capture date for easy grouping

            // Execute the insertion
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of the 20 most recent check-ins.
     * Joins with the members table to include Member Names in the result.
     *
     * @return List of AttendanceRecord items.
     */
    public List<com.gym.app.model.AttendanceRecord> getRecentAttendance() {
        List<com.gym.app.model.AttendanceRecord> list = new ArrayList<>();

        // Join 'attendance' and 'members' to get readable names instead of just IDs
        // Order by ID descending to show newest first
        String sql = "SELECT a.check_in_time, m.first_name, m.last_name FROM attendance a " +
                "JOIN members m ON a.member_id = m.id " +
                "ORDER BY a.id DESC LIMIT 20";

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Parse result set data
                String timeStr = rs.getString("check_in_time");
                String name = rs.getString("first_name") + " " + rs.getString("last_name");
                LocalDateTime time = LocalDateTime.parse(timeStr);

                // Add to list
                list.add(new com.gym.app.model.AttendanceRecord(name, time));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Counts how many members have checked in today.
     * Useful for dashboard stats.
     *
     * @return The count of check-ins for the current date.
     */
    public int getTodayCheckInCount() {
        String todayStr = TimeService.getToday().toString();
        String sql = "SELECT COUNT(*) FROM attendance WHERE date = ?";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todayStr);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
