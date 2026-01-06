package com.gym.app.controller;

import com.gym.app.dao.ScheduleDAO;
import com.gym.app.model.ScheduleItem;
import com.gym.app.util.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for adding a new Class/Session to the Schedule.
 */
public class ScheduleFormController {

    @FXML
    private ComboBox<String> dayBox;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private TextField titleField;

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private int coachId;

    /**
     * Initializes the dropdown with days of the week.
     */
    @FXML
    public void initialize() {
        dayBox.setItems(FXCollections.observableArrayList(
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"));
    }

    public void setCoachId(int coachId) {
        this.coachId = coachId;
    }

    /**
     * Helper to show alert messages.
     */
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Saves the schedule item.
     */
    @FXML
    void handleSave(ActionEvent event) {
        String day = dayBox.getValue();
        String start = startTimeField.getText();
        String end = endTimeField.getText();
        String title = titleField.getText();

        boolean isValid = true;

        // Basic Checks
        if (day == null || title.isEmpty()) {
            showAlert("Day and Title are required.");
            return;
        }

        // Time Validation (HH:mm)
        if (!ValidationUtils.isValidTime(start)) {
            startTimeField.setStyle("-fx-border-color: red; -fx-background-color: #333; -fx-text-fill: white;");
            isValid = false;
        } else {
            startTimeField.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        }

        if (!end.isEmpty() && !ValidationUtils.isValidTime(end)) {
            endTimeField.setStyle("-fx-border-color: red; -fx-background-color: #333; -fx-text-fill: white;");
            isValid = false;
        } else {
            endTimeField.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        }

        if (!isValid) {
            showAlert("Invalid time format. Use HH:mm (e.g. 09:00, 18:30).");
            return;
        }

        // Save
        ScheduleItem item = new ScheduleItem(0, coachId, day, start, end, title);
        scheduleDAO.addScheduleItem(item);

        Stage stage = (Stage) dayBox.getScene().getWindow();
        stage.close();
    }
}
