package com.gym.app.controller;

import com.gym.app.dao.CoachDAO;
import com.gym.app.model.Coach;
import com.gym.app.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the Coach Entry/Edit Form.
 */
public class CoachFormController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField specializationField;
    @FXML
    private TextField phoneField;

    private final CoachDAO coachDAO = new CoachDAO();
    private Coach coachToEdit;

    /**
     * Pre-fills the form if editing an existing coach.
     */
    public void setCoachToEdit(Coach coach) {
        this.coachToEdit = coach;
        if (coach != null) {
            nameField.setText(coach.getName());
            specializationField.setText(coach.getSpecialization());
            phoneField.setText(coach.getPhone());
        }
    }

    /**
     * Saves the coach details to the database.
     */
    @FXML
    void handleSave(ActionEvent event) {
        String name = nameField.getText();
        String spec = specializationField.getText();
        String phone = phoneField.getText();

        // Validation
        boolean isValid = true;

        if (name.isEmpty() || spec.isEmpty()) {
            showAlert("Name and Specialization are required.");
            return;
        }

        if (!ValidationUtils.isValidPhone(phone)) {
            phoneField.setStyle("-fx-border-color: red; -fx-background-color: #333; -fx-text-fill: white;");
            showAlert("Phone number must be exactly 10 digits.");
            isValid = false;
        } else {
            phoneField.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        }

        if (!isValid)
            return;

        // Perform Database Operation
        if (coachToEdit == null) {
            // Add New
            Coach newCoach = new Coach(0, name, spec, phone);
            coachDAO.addCoach(newCoach);
        } else {
            // Update (Delete & Re-add strategy used here for simplicity in earlier phases,
            // ideally should be an UPDATE SQL command)
            coachDAO.deleteCoach(coachToEdit.getId());
            coachDAO.addCoach(new Coach(0, name, spec, phone));
        }

        // Close Dialog
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
