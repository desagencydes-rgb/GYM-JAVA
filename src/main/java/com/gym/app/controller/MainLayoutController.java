package com.gym.app.controller;

import com.gym.app.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controller for the "Main Layout" (Sidebar + Content Area).
 * Handles navigation between different views (Dashboard, Members, etc.) logic
 * by swapping FXMLs into the content area.
 */
public class MainLayoutController {

    // The central area where views are injected
    @FXML
    private StackPane contentArea;

    /**
     * Initialize method called by JavaFX after FXML loading.
     * Sets the default view to the Dashboard.
     */
    @FXML
    public void initialize() {
        showDashboard(null);
    }

    /**
     * Loads a specific FXML view into the content area.
     * 
     * @param fxml The name of the FXML file (without .fxml extension) in
     *             /resources/fxml/
     */
    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml + ".fxml"));
            Parent view = loader.load();

            // Clear previous view and add the new one
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Navigation Handlers ---

    /**
     * Navigates to the Dashboard view.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void showDashboard(ActionEvent event) {
        loadView("dashboard");
    }

    /**
     * Navigates to the Members view.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void showMembers(ActionEvent event) {
        loadView("members_list");
    }

    /**
     * Navigates to the Coaches view.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void showCoaches(ActionEvent event) {
        loadView("coaches");
    }

    /**
     * Navigates to the Attendance view.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void showAttendance(ActionEvent event) {
        loadView("attendance");
    }

    /**
     * Navigates to the Payments view.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void showPayments(ActionEvent event) {
        loadView("payments");
    }

    /**
     * Navigates to the Debug view.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void showDebug(ActionEvent event) {
        loadView("debug_time");
    }

    /**
     * Logs the user out by returning to the Login screen.
     */
    @FXML
    void logout(ActionEvent event) throws IOException {
        Main.setRoot("login");
    }
}
