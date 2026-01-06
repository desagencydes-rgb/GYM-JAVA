package com.gym.app.controller;

import com.gym.app.util.TimeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import java.time.LocalDate;

/**
 * Controller for the Debug/Test screen.
 * Allows changing the application's "Current Date" logically to test time-based
 * features (e.g., expiry, attendance).
 */
public class DebugTimeController {

    @FXML
    private DatePicker datePicker;

    @FXML
    private Label currentStatusLabel;

    /**
     * Initializes the controller class.
     * Updates the status label with the current system date.
     */
    @FXML
    public void initialize() {
        updateStatus();
    }

    /**
     * Sets the Mock Date in TimeService.
     */
    @FXML
    void handleSetDate(ActionEvent event) {
        LocalDate selected = datePicker.getValue();
        if (selected != null) {
            TimeService.setMockDate(selected);
            updateStatus();
        }
    }

    /**
     * Resets the system to use the real current date.
     */
    @FXML
    void handleResetDate(ActionEvent event) {
        TimeService.setMockDate(null);
        datePicker.setValue(null);
        updateStatus();
    }

    /**
     * Updates the status label to show what the app thinks is "Today".
     */
    private void updateStatus() {
        LocalDate now = TimeService.getToday();
        currentStatusLabel.setText("System Date is currently set to: " + now.toString());
    }
}
