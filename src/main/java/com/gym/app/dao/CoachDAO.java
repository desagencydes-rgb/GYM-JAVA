package com.gym.app.dao;

import com.gym.app.model.Coach;
import com.gym.app.util.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Coach entities.
 * Handles database operations for gym trainers/coaches.
 */
public class CoachDAO {

    /**
     * Retrieves all coaches from the database.
     * 
     * @return List of Coach objects.
     */
    public List<Coach> getAllCoaches() {
        List<Coach> list = new ArrayList<>();
        String sql = "SELECT * FROM coaches";

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Coach(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("phone")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Adds a new coach to the database.
     * 
     * @param coach The Coach object to add.
     */
    public void addCoach(Coach coach) {
        String sql = "INSERT INTO coaches (name, specialization, phone) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, coach.getName());
            pstmt.setString(2, coach.getSpecialization());
            pstmt.setString(3, coach.getPhone());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a coach by their ID.
     * 
     * @param id The ID of the coach to remove.
     */
    public void deleteCoach(int id) {
        String sql = "DELETE FROM coaches WHERE id=?";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
