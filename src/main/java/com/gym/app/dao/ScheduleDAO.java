package com.gym.app.dao;

import com.gym.app.model.ScheduleItem;
import com.gym.app.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Schedule/Class entries.
 * Manages the gym class schedule.
 */
public class ScheduleDAO {

    /**
     * Adds a new item to the schedule (e.g., a new class session).
     * 
     * @param item The ScheduleItem to add.
     */
    public void addScheduleItem(ScheduleItem item) {
        String sql = "INSERT INTO schedules (coach_id, day, start_time, end_time, title) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, item.getCoachId());
            pstmt.setString(2, item.getDay());
            pstmt.setString(3, item.getStartTime());
            pstmt.setString(4, item.getEndTime());
            pstmt.setString(5, item.getTitle());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all schedule items for a specific coach.
     * 
     * @param coachId The ID of the coach.
     * @return List of ScheduleItems.
     */
    public List<ScheduleItem> getSchedulesByCoachId(int coachId) {
        List<ScheduleItem> list = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE coach_id = ?";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, coachId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                list.add(new ScheduleItem(
                        rs.getInt("id"),
                        rs.getInt("coach_id"),
                        rs.getString("day"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("title")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Deletes a schedule item by ID.
     * 
     * @param id The ID of the schedule item.
     */
    public void deleteScheduleItem(int id) {
        String sql = "DELETE FROM schedules WHERE id = ?";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all scheduled classes, sorted by day and time.
     * joins with the coaches table to fetch the Coach Name.
     *
     * @return List of ScheduleItems populated with Coach Names.
     */
    public List<ScheduleItem> getAllSchedules() {
        List<ScheduleItem> list = new ArrayList<>();

        // Left Join to get coach name even if coach missing (though FK constraint
        // usually prevents this)
        String sql = "SELECT s.*, c.name as coach_name FROM schedules s LEFT JOIN coaches c ON s.coach_id = c.id ORDER BY s.day, s.start_time";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                ScheduleItem item = new ScheduleItem(
                        rs.getInt("id"),
                        rs.getInt("coach_id"),
                        rs.getString("day"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("title"));

                // Set the extra non-DB field `coachName`
                item.setCoachName(rs.getString("coach_name"));
                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
