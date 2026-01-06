package com.gym.app.dao;

import com.gym.app.model.Member;
import com.gym.app.util.DatabaseHelper;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for 'Member' entities.
 * Handles CRUD operations and complex queries for members in the SQLite
 * database.
 */
public class MemberDAO {

    /**
     * Adds a new member to the database.
     *
     * @param member The Member object containing details to save.
     */
    public void addMember(Member member) {
        // SQL Injection-safe query using PreparedStatement
        String sql = "INSERT INTO members(first_name, last_name, phone, email, gender, photo_path, registration_date, face_id) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Bind parameters
            pstmt.setString(1, member.getFirstName());
            pstmt.setString(2, member.getLastName());
            pstmt.setString(3, member.getPhone());
            pstmt.setString(4, member.getEmail());
            pstmt.setString(5, member.getGender());
            pstmt.setString(6, member.getPhotoPath());
            pstmt.setString(7, member.getRegistrationDate().toString()); // Store LocalDate as String
            pstmt.setString(8, member.getFaceId()); // Store specific Face Recognition ID

            // Execute Insert
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Retrieves all members from the database.
     *
     * @return A list of Member objects.
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM members";

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return members;
    }

    /**
     * Returns the total count of registered members.
     *
     * @return int count
     */
    public int getMemberCount() {
        String sql = "SELECT COUNT(*) FROM members";
        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Finds members whose subscriptions are expiring within a certain number of
     * days.
     * Warning: This logic assumes 'Active' status is accurate in the database.
     *
     * @param daysThreshold The number of days to look ahead (e.g., 5 days).
     * @return A list of ExpiringMember DTOs associated with the expiration data.
     */
    public List<com.gym.app.model.ExpiringMember> getMembersWithExpiringSubscriptions(int daysThreshold) {
        List<com.gym.app.model.ExpiringMember> list = new ArrayList<>();
        LocalDate today = com.gym.app.util.TimeService.getToday();
        LocalDate thresholdDate = today.plusDays(daysThreshold);

        // Join members with subscriptions to check dates
        String sql = "SELECT m.*, s.end_date FROM members m " +
                "JOIN subscriptions s ON m.id = s.member_id " +
                "WHERE s.status = 'Active' AND s.end_date BETWEEN ? AND ? " +
                "ORDER BY s.end_date ASC";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today.toString());
            pstmt.setString(2, thresholdDate.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Member m = mapResultSetToMember(rs);
                LocalDate end = LocalDate.parse(rs.getString("end_date"));

                // Calculate exact days remaining
                long days = java.time.temporal.ChronoUnit.DAYS.between(today, end);
                list.add(new com.gym.app.model.ExpiringMember(m, end, days));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates an existing member's details.
     *
     * @param member The updated Member object.
     * @throws SQLException If database error occurs.
     */
    public void updateMember(Member member) throws SQLException {
        String sql = "UPDATE members SET first_name=?, last_name=?, phone=?, email=?, gender=?, face_id=? WHERE id=?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getFirstName());
            pstmt.setString(2, member.getLastName());
            pstmt.setString(3, member.getPhone());
            pstmt.setString(4, member.getEmail());
            pstmt.setString(5, member.getGender());
            pstmt.setString(6, member.getFaceId());
            pstmt.setInt(7, member.getId());
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a member from the database by ID.
     *
     * @param id The member ID to delete.
     */
    public void deleteMember(int id) {
        String sql = "DELETE FROM members WHERE id=?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to map a SQL ResultSet row to a Member object.
     * Handles the 'face_id' column being potentially missing in older DB versions
     * via Try/Catch.
     *
     * @param rs The ResultSet positioned at the current row.
     * @return A populated Member object.
     * @throws SQLException If a column error occurs (except face_id).
     */
    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        String faceId = null;
        try {
            faceId = rs.getString("face_id");
        } catch (SQLException e) {
            // Column might not exist yet if migration failed or database is old,
            // ignore/treat as null.
        }

        return new Member(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("gender"),
                rs.getString("photo_path"),
                LocalDate.parse(rs.getString("registration_date")),
                faceId);
    }

    /**
     * Generates a statistic map of registrations over the last 6 months.
     * Used for dashboard charts.
     *
     * @return Map of MonthName (Short) -> Count.
     */
    public java.util.Map<String, Integer> getRegistrationsLast6Months() {
        // LinkedHashMap maintains insertion order
        java.util.Map<String, Integer> stats = new java.util.LinkedHashMap<>();
        LocalDate today = com.gym.app.util.TimeService.getToday();

        // 1. Initialize map with 0 for last 6 months to ensure no gaps
        for (int i = 5; i >= 0; i--) {
            LocalDate d = today.minusMonths(i);
            String key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            stats.put(key, 0);
        }

        // 2. Query all registration dates
        String sql = "SELECT registration_date FROM members";
        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate rDate = LocalDate.parse(rs.getString("registration_date"));
                // Create key in format YYYY-MM
                String key = rDate.getYear() + "-" + String.format("%02d", rDate.getMonthValue());

                // Increment if key exists (is within the last 6 months window we initialized)
                if (stats.containsKey(key)) {
                    stats.put(key, stats.get(key) + 1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Convert keys from "2023-11" to "NOV"
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, Integer> entry : stats.entrySet()) {
            String[] parts = entry.getKey().split("-");
            // Get Month enum from integer, taking month number
            java.time.Month m = java.time.Month.of(Integer.parseInt(parts[1]));
            // Use 3-letter abbreviation
            result.put(m.name().substring(0, 3), entry.getValue());
        }
        return result;
    }
}
