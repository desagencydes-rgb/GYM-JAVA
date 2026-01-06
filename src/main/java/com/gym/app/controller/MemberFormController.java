package com.gym.app.controller;

import com.gym.app.dao.MemberDAO;
import com.gym.app.model.Member;
import com.gym.app.util.TimeService;
import com.gym.app.util.ValidationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the Member Entry Form (Add/Edit).
 * Handles input validation and integration with Facial Recognition enrollment.
 */
public class MemberFormController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> genderBox;

    private final MemberDAO memberDAO = new MemberDAO();
    private Member currentMember = null; // If null, we are in CREATE mode; otherwise EDIT mode.

    // Facial Recognition Controls
    @FXML
    private Button enrollButton;
    @FXML
    private Label faceIdStatusLabel;
    @FXML
    private javafx.scene.layout.VBox cameraContainer;

    private com.gym.app.component.FaceCamView faceCamView;
    private final com.gym.app.service.FaceRecognitionService faceService = new com.gym.app.service.LuxandFaceRecognitionService();

    // Store the ID returned by the API temporarily until user clicks "Save"
    private String enrolledFaceId = null;

    @FXML
    public void initialize() {
        // Init Gender dropdown
        genderBox.getItems().addAll("Male", "Female");
        genderBox.getSelectionModel().selectFirst();

        // Initialize Camera Component
        faceCamView = new com.gym.app.component.FaceCamView();
        cameraContainer.getChildren().add(0, faceCamView); // Add at top
    }

    /**
     * Pre-fills the form with existing member data (Edit Mode).
     */
    public void setMemberToEdit(Member member) {
        this.currentMember = member;
        firstNameField.setText(member.getFirstName());
        lastNameField.setText(member.getLastName());
        phoneField.setText(member.getPhone());
        emailField.setText(member.getEmail());
        genderBox.setValue(member.getGender());

        // Check if member already has a face enrolled
        if (member.getFaceId() != null && !member.getFaceId().isEmpty()) {
            this.enrolledFaceId = member.getFaceId();
            faceIdStatusLabel.setText("Face Already Enrolled");
            faceIdStatusLabel.setStyle("-fx-text-fill: #00e676;");
        }
    }

    /**
     * Toggles the facial recognition camera interface.
     */
    @FXML
    void handleEnrollFace(ActionEvent event) {
        if (!cameraContainer.isVisible()) {
            // Start Interface
            cameraContainer.setVisible(true);
            cameraContainer.setManaged(true);
            faceCamView.start();
            enrollButton.setText("Close Camera");
        } else {
            // Stop Interface
            cameraContainer.setVisible(false);
            cameraContainer.setManaged(false);
            faceCamView.stop();
            enrollButton.setText("Enroll Face ID");
        }
    }

    /**
     * Captures a frame from the camera and sends it to the API for enrollment.
     */
    @FXML
    void handleCaptureFace(ActionEvent event) {
        java.awt.image.BufferedImage bImg = faceCamView.getCurrentFrame();
        if (bImg == null) {
            showAlert("Error", "Camera is not ready.");
            return;
        }

        // We use the person's name as the label in the AI service
        String name = firstNameField.getText() + " " + lastNameField.getText();
        if (name.trim().isEmpty()) {
            showAlert("Required", "Please enter First and Last Name before enrolling face.");
            return;
        }

        try {
            // Save frame to temporary file
            java.io.File tempFile = java.io.File.createTempFile("face_capture", ".jpg");
            javax.imageio.ImageIO.write(bImg, "JPG", tempFile);

            // Call API
            String token = faceService.enrollFace(name, tempFile);
            this.enrolledFaceId = token;

            // UI Feedback
            faceIdStatusLabel
                    .setText("Face Enrolled! (ID: " + token.substring(0, Math.min(token.length(), 8)) + "...)");
            faceIdStatusLabel.setStyle("-fx-text-fill: #00e676;");

            // Auto-close camera after success
            handleEnrollFace(null);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Enrollment Failed", "Error: " + e.getMessage());
        }
    }

    /**
     * Saves the member to the database (Insert or Update).
     */
    @FXML
    void handleSave(ActionEvent event) {
        String fname = firstNameField.getText();
        String lname = lastNameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String gender = genderBox.getValue();

        // 1. Validation
        if (fname.isEmpty() || lname.isEmpty()) {
            showAlert("Error", "Name fields cannot be empty.");
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            showAlert("Invalid Input", "Phone number must be exactly 10 digits.");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            showAlert("Invalid Input", "Please enter a valid email address.");
            return;
        }

        try {
            if (currentMember == null) {
                // CREATE NEW
                Member newMember = new Member(0, fname, lname, phone, email, gender, "", TimeService.getToday(),
                        enrolledFaceId);
                memberDAO.addMember(newMember);
            } else {
                // UPDATE EXISTING
                // Keep old Face ID if a new one wasn't enrolled
                String faceTokenToUse = (enrolledFaceId != null) ? enrolledFaceId : currentMember.getFaceId();

                Member updatedMember = new Member(currentMember.getId(), fname, lname, phone, email, gender,
                        currentMember.getPhotoPath(), currentMember.getRegistrationDate(),
                        faceTokenToUse);
                memberDAO.updateMember(updatedMember);
            }

            // Cleanup
            if (faceCamView != null)
                faceCamView.stop();

            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Could not save member: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        if (faceCamView != null)
            faceCamView.stop();
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
