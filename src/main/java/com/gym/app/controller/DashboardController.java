package com.gym.app.controller;

import com.gym.app.dao.AttendanceDAO;
import com.gym.app.dao.MemberDAO;
import com.gym.app.dao.PaymentDAO;
import com.gym.app.dao.SubscriptionDAO;
import com.gym.app.model.Member;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

/**
 * Controller for the Dashboard.
 * Displays key metrics (members, revenue, attendance) and visual charts.
 * Auto-refreshes data periodically.
 */
public class DashboardController {

    // Simple textual metrics
    @FXML
    private Label totalMembersLabel;
    @FXML
    private Label monthlyRevenueLabel;
    @FXML
    private Label activeSessionsLabel;

    // Container for the "Expiring Soon" list
    @FXML
    private VBox expiringMembersContainer;

    // Charts for analytics
    @FXML
    private BarChart<String, Number> registrationChart;
    @FXML
    private LineChart<String, Number> revenueChart;
    @FXML
    private PieChart planPieChart;

    // Data Access Objects
    private final MemberDAO memberDAO = new MemberDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    // Container for the Weekly Schedule (Coach Sessions)
    @FXML
    private VBox scheduleContainer;

    private final com.gym.app.dao.ScheduleDAO scheduleDAO = new com.gym.app.dao.ScheduleDAO();

    /**
     * Initializes the dashboard.
     * Loads initial data and sets up a 5-second auto-refresh timer.
     */
    @FXML
    public void initialize() {
        refreshDashboard();
        loadWeeklySchedule();

        // Create a timeline to refresh data every 5 seconds (Real-time dashboard feel)
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            refreshDashboard();
            loadWeeklySchedule();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Fetches and displays the weekly schedule of classes.
     * Generates UI cards for each schedule item.
     */
    private void loadWeeklySchedule() {
        if (scheduleContainer == null)
            return;
        scheduleContainer.getChildren().clear();

        List<com.gym.app.model.ScheduleItem> schedules = scheduleDAO.getAllSchedules();

        if (schedules.isEmpty()) {
            Label placeholder = new Label("No info.");
            placeholder.setStyle("-fx-text-fill: #666;");
            scheduleContainer.getChildren().add(placeholder);
            return;
        }

        // Create a visual card for each class/session
        for (com.gym.app.model.ScheduleItem item : schedules) {
            javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(15);
            card.getStyleClass().add("schedule-card");
            card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // 1. Time Pill (HH:mm - HH:mm)
            Label timeLabel = new Label(item.getStartTime() + " - " + item.getEndTime());
            timeLabel.getStyleClass().add("schedule-time-text");
            javafx.scene.layout.StackPane timePill = new javafx.scene.layout.StackPane(timeLabel);
            timePill.getStyleClass().add("schedule-time-pill");

            // 2. Info Block (Title + Coach)
            VBox infoBox = new VBox(2);
            Label titleLabel = new Label(item.getTitle());
            titleLabel.getStyleClass().add("schedule-class-title");

            Label coachLabel = new Label("Coach: " + (item.getCoachName() != null ? item.getCoachName() : "Unknown"));
            coachLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

            infoBox.getChildren().addAll(titleLabel, coachLabel);

            // 3. Flexible Spacer to push Day Label to the right
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            // 4. Day Badge (e.g., "Monday")
            Label dayLabel = new Label(item.getDay());
            dayLabel.setStyle("-fx-text-fill: white; -fx-opacity: 0.5; -fx-font-weight: bold; -fx-font-size: 14px;");

            card.getChildren().addAll(timePill, infoBox, spacer, dayLabel);
            scheduleContainer.getChildren().add(card);
        }
    }

    /**
     * Refreshes all metrics and charts on the dashboard.
     */
    private void refreshDashboard() {
        // 1. Total Members
        int memberCount = memberDAO.getMemberCount();
        totalMembersLabel.setText(String.valueOf(memberCount));

        // 2. Revenue (Lifetime Total)
        double revenue = paymentDAO.getTotalRevenue();
        monthlyRevenueLabel.setText(String.format("%.0f MAD", revenue));

        // 3. Active Sessions (Today's check-ins)
        int activeSessions = attendanceDAO.getTodayCheckInCount();
        activeSessionsLabel.setText(String.valueOf(activeSessions));

        // 4. Update the "Expiring Soon" list
        updateWarnings();

        // 5. Update Charts
        updateCharts();
    }

    /**
     * Refreshes the three analytics charts:
     * - New Members (Bar Chart)
     * - Revenue Trend (Line Chart)
     * - Plan Distribution (Pie Chart)
     */
    private void updateCharts() {
        // Bar Chart: Registrations in last 6 months
        Map<String, Integer> regStats = memberDAO.getRegistrationsLast6Months();
        XYChart.Series<String, Number> regSeries = new XYChart.Series<>();
        regSeries.setName("New Members");
        for (Map.Entry<String, Integer> entry : regStats.entrySet()) {
            regSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        registrationChart.getData().clear();
        registrationChart.getData().add(regSeries);

        // Line Chart: Revenue in last 6 months
        Map<String, Double> revStats = paymentDAO.getRevenueLast6Months();
        XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
        revSeries.setName("Revenue (MAD)");
        for (Map.Entry<String, Double> entry : revStats.entrySet()) {
            revSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        revenueChart.getData().clear();
        revenueChart.getData().add(revSeries);

        // Pie Chart: Active Plans distribution
        Map<String, Integer> planStats = subscriptionDAO.getPlanDistribution();
        planPieChart.getData().clear();
        for (Map.Entry<String, Integer> entry : planStats.entrySet()) {
            planPieChart.getData().add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * fetches and displays members whose subscriptions expire within 7 days.
     */
    private void updateWarnings() {
        expiringMembersContainer.getChildren().clear();
        List<com.gym.app.model.ExpiringMember> expiring = memberDAO.getMembersWithExpiringSubscriptions(7);

        if (expiring.isEmpty()) {
            Label placeholder = new Label("No subscriptions expiring soon.");
            placeholder.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
            expiringMembersContainer.getChildren().add(placeholder);
        } else {
            for (com.gym.app.model.ExpiringMember info : expiring) {
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label nameLabel = new Label(info.getMember().getFullName());
                nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                Label daysLabel = new Label(info.getDaysLeft() + " days left");
                if (info.getDaysLeft() <= 3) {
                    daysLabel.setStyle("-fx-text-fill: #ff4444;"); // Red for < 3 days
                } else {
                    daysLabel.setStyle("-fx-text-fill: #ffab40;"); // Orange for < 7 days
                }

                row.getChildren().addAll(nameLabel, daysLabel);
                expiringMembersContainer.getChildren().add(row);
            }
        }
    }
}
