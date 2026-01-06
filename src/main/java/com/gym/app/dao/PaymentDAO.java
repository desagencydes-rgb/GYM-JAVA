package com.gym.app.dao;

import com.gym.app.util.DatabaseHelper;
import java.sql.*;

/**
 * Data Access Object for Payment records.
 * Handles financial transaction storage and retrieval.
 */
public class PaymentDAO {

    /**
     * Calculates the total revenue from all payments.
     * 
     * @return The sum of all 'amount' values in the payments table.
     */
    public double getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM payments";
        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Retrieves all payments.
     * 
     * @return A list of Payment objects.
     */
    public java.util.List<com.gym.app.model.Payment> getAllPayments() {
        java.util.List<com.gym.app.model.Payment> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY id DESC"; // Newest first

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new com.gym.app.model.Payment(
                        rs.getInt("id"),
                        rs.getInt("member_id"),
                        rs.getDouble("amount"),
                        java.time.LocalDate.parse(rs.getString("payment_date")),
                        rs.getString("method")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Retrieves all payments as Data Transfer Objects (DTOs).
     * This includes the Member's Name (First + Last) by joining tables.
     * Useful for UI display tables where names are preferred over IDs.
     *
     * @return List of PaymentDTOs.
     */
    public java.util.List<com.gym.app.model.PaymentDTO> getAllPaymentDTOs() {
        java.util.List<com.gym.app.model.PaymentDTO> list = new java.util.ArrayList<>();

        // Join payments with members to get descriptive data
        String sql = "SELECT p.id, p.amount, p.payment_date, p.method, m.first_name, m.last_name " +
                "FROM payments p JOIN members m ON p.member_id = m.id " +
                "ORDER BY p.payment_date DESC";

        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new com.gym.app.model.PaymentDTO(
                        rs.getInt("id"),
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getDouble("amount"),
                        java.time.LocalDate.parse(rs.getString("payment_date")),
                        rs.getString("method")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Records a new payment in the database.
     * 
     * @param p The Payment object to save.
     */
    public void addPayment(com.gym.app.model.Payment p) {
        String sql = "INSERT INTO payments (member_id, amount, payment_date, method) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, p.getMemberId());
            pstmt.setDouble(2, p.getAmount());
            pstmt.setString(3, p.getPaymentDate().toString());
            pstmt.setString(4, p.getMethod());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a payment record by ID.
     * 
     * @param id The ID to delete.
     */
    public void deletePayment(int id) {
        String sql = "DELETE FROM payments WHERE id=?";
        try (Connection conn = DatabaseHelper.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Public method to access revenue statistics.
     * Delegates to the private implementation handling Java-side logic.
     * 
     * @return Map of Month -> Revenue.
     */
    public java.util.Map<String, Double> getRevenueLast6Months() {
        return getRevenueLast6MonthsJavaSide();
    }

    /**
     * Calculates revenue for the last 6 months.
     * Initializes all months to 0.0 first to ensure the chart has all data points.
     * 
     * @return Ordered Map of Month Name to Revenue Amount.
     */
    private java.util.Map<String, Double> getRevenueLast6MonthsJavaSide() {
        // LinkedHashMap to keep month order (oldest to newest)
        java.util.Map<String, Double> stats = new java.util.LinkedHashMap<>();
        java.time.LocalDate today = com.gym.app.util.TimeService.getToday();

        // 1. Initialize buckets for the last 6 months with 0.0 revenue
        for (int i = 5; i >= 0; i--) {
            java.time.LocalDate d = today.minusMonths(i);
            String key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
            stats.put(key, 0.0);
        }

        // 2. Fetch all values and aggregate them in Java
        // (Could be done with SQL GROUP BY, but this is database-agnostic/simpler for
        // SQLite dates stored as strings)
        String sql = "SELECT * FROM payments";
        try (Connection conn = DatabaseHelper.connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                java.time.LocalDate pDate = java.time.LocalDate.parse(rs.getString("payment_date"));
                String key = pDate.getYear() + "-" + String.format("%02d", pDate.getMonthValue());

                // Add amount if the payment falls within our 6-month window
                if (stats.containsKey(key)) {
                    stats.put(key, stats.get(key) + rs.getDouble("amount"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Convert keys (2023-01) to friendly labels (JAN)
        java.util.Map<String, Double> result = new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, Double> entry : stats.entrySet()) {
            String[] parts = entry.getKey().split("-");
            java.time.Month m = java.time.Month.of(Integer.parseInt(parts[1]));
            result.put(m.name().substring(0, 3), entry.getValue());
        }
        return result;
    }
}
