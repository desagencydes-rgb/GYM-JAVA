package com.gym.app.dao;

import com.gym.app.model.Subscription;
import com.gym.app.util.DatabaseHelper;
import java.sql.*;

/**
 * Data Access Object for Subscriptions.
 * Manages member plan details (Start/End dates, Type, Price).
 */
public class SubscriptionDAO {

    /**
     * Adds a new subscription for a member.
     * 
     * @param sub The Subscription object to save.
     */
    public void addSubscription(Subscription sub) {
        String sql = "INSERT INTO subscriptions (member_id, plan_name, start_date, end_date, price, status) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sub.getMemberId());
            pstmt.setString(2, sub.getPlanName());
            pstmt.setString(3, sub.getStartDate().toString());
            pstmt.setString(4, sub.getEndDate().toString());
            pstmt.setDouble(5, sub.getPrice());
            pstmt.setString(6, sub.getStatus()); // e.g., "Active"
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a distribution of plan types (e.g., how many "Gold" vs "Silver").
     * Used for charts.
     *
     * @return Map of Plan Name -> Count.
     */
    public java.util.Map<String, Integer> getPlanDistribution() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();

        // Group by plan name and count occurrences, filtering only Active plans
        String sql = "SELECT plan_name, COUNT(*) FROM subscriptions WHERE status='Active' GROUP BY plan_name";

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                stats.put(rs.getString(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    /**
     * Counts the total number of currently active subscriptions.
     * 
     * @return Count of active subscriptions.
     */
    public int getActiveSubscriptionCount() {
        String sql = "SELECT COUNT(*) FROM subscriptions WHERE status='Active'";

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
