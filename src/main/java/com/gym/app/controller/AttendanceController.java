package com.gym.app.controller;

import com.gym.app.dao.AttendanceDAO;
import com.gym.app.dao.MemberDAO;
import com.gym.app.model.AttendanceRecord;
import com.gym.app.model.Member;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Attendance Screen.
 * Handles Manual Check-ins (via dropdown) and Facial Recognition Auto-scan.
 */
public class AttendanceController {

    @FXML
    private ComboBox<Member> memberComboBox;
    @FXML
    private ListView<AttendanceRecord> attendanceList;

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final MemberDAO memberDAO = new MemberDAO();

    // --- Auto-Attendance (Facial Recognition) UI ---
    @FXML
    private javafx.scene.layout.VBox faceScanContainer;
    @FXML
    private javafx.scene.control.Button toggleAutoBtn;

    private com.gym.app.component.FaceCamView faceCamView;
    private final com.gym.app.service.FaceRecognitionService faceService = new com.gym.app.service.LuxandFaceRecognitionService();

    // Timer to repeatedly scan frames
    private java.util.Timer scanTimer;
    private boolean isScanning = false;

    @FXML
    public void initialize() {
        refreshList();

        // Populate manual check-in dropdown
        memberComboBox.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
        memberComboBox.setConverter(new StringConverter<Member>() {
            @Override
            public String toString(Member m) {
                return m == null ? "" : m.getFullName() + " (ID: " + m.getId() + ")";
            }

            @Override
            public Member fromString(String s) {
                return null; // Not needed
            }
        });

        // Set Custom Cell for the Activity Feed
        attendanceList.setCellFactory(listView -> new ActivityListCell());

        // Initialize Camera Component (Hidden by default)
        faceCamView = new com.gym.app.component.FaceCamView();
        faceScanContainer.getChildren().add(faceCamView);
        faceScanContainer.setVisible(false);
        faceScanContainer.setManaged(false);
    }

    /**
     * Toggles the automatic facial recognition scanning mode.
     */
    @FXML
    void handleToggleAuto(ActionEvent event) {
        if (isScanning) {
            stopScanning();
        } else {
            startScanning();
        }
    }

    private void startScanning() {
        isScanning = true;
        toggleAutoBtn.setText("Stop Scanning");
        toggleAutoBtn.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-weight: bold;");

        // Show camera
        faceScanContainer.setVisible(true);
        faceScanContainer.setManaged(true);
        faceCamView.start();

        // Schedule periodic scans
        scanTimer = new java.util.Timer(true);
        scanTimer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                if (!isScanning) {
                    cancel();
                    return;
                }
                performFaceScan();
            }
        }, 2000, 3000); // Wait 2s, then scan every 3s
    }

    private void stopScanning() {
        isScanning = false;
        toggleAutoBtn.setText("Start Scanning");
        toggleAutoBtn.setStyle("-fx-background-color: #7c4dff; -fx-text-fill: white; -fx-font-weight: bold;");

        if (scanTimer != null) {
            scanTimer.cancel();
            scanTimer = null;
        }

        faceCamView.stop();
        faceScanContainer.setVisible(false);
        faceScanContainer.setManaged(false);
    }

    /**
     * Captures a frame and attempts to recognize the face.
     * Note: Runs on a Timer thread, so UI updates must be wrapped in
     * Platform.runLater.
     */
    private void performFaceScan() {
        if (!faceService.isConfigured()) {
            javafx.application.Platform.runLater(
                    () -> faceCamView.showFeedback("API Config Missing", javafx.scene.paint.Color.ORANGE, 2000));
            return;
        }

        try {
            java.awt.image.BufferedImage bImg = faceCamView.getCurrentFrame();
            if (bImg != null) {
                // Save frame to temp file
                java.io.File temp = java.io.File.createTempFile("scan", ".jpg");
                javax.imageio.ImageIO.write(bImg, "JPG", temp);

                // Call Recognition API
                String recognizedName = faceService.recognizeFace(temp);

                if (recognizedName != null) {
                    // Find member in DB by name
                    Member found = findMemberByName(recognizedName);
                    if (found != null) {
                        javafx.application.Platform.runLater(() -> {
                            // Check-in logic
                            attendanceDAO.checkIn(found.getId());
                            // UI Feedback
                            faceCamView.showFeedback("Welcome, " + found.getFirstName() + "!",
                                    javafx.scene.paint.Color.LIME, 4000);
                            refreshList();
                        });
                    } else {
                        // Name recognized but not found in local DB (Data inconsistency)
                        javafx.application.Platform.runLater(() -> faceCamView
                                .showFeedback("ID Mismatch: " + recognizedName, javafx.scene.paint.Color.ORANGE, 2000));
                    }
                } else {
                    javafx.application.Platform.runLater(
                            () -> faceCamView.showFeedback("Not Recognized", javafx.scene.paint.Color.RED, 1500));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Basic helper to find member by Name.
     * Ideally, the Face Service should return the FaceID/Token, which we can look
     * up directly.
     * But Luxand returns the "Name" we assigned it.
     */
    private Member findMemberByName(String fullName) {
        for (Member m : memberDAO.getAllMembers()) {
            if (m.getFullName().equalsIgnoreCase(fullName)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Manual Check-in handler.
     */
    @FXML
    void handleCheckIn(ActionEvent event) {
        Member selected = memberComboBox.getValue();
        if (selected != null) {
            attendanceDAO.checkIn(selected.getId());
            memberComboBox.getSelectionModel().clearSelection();
            refreshList();
        }
    }

    private void refreshList() {
        attendanceList.setItems(FXCollections.observableArrayList(attendanceDAO.getRecentAttendance()));
    }

    // --- Custom ListCell for Attendance History ---
    private static class ActivityListCell extends ListCell<AttendanceRecord> {
        private final HBox root;
        private final Label nameLabel;
        private final Label timeLabel;
        private final Label dateLabel;
        private final Circle icon;

        public ActivityListCell() {
            root = new HBox(15);
            root.getStyleClass().add("activity-card");
            root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            root.setPadding(new javafx.geometry.Insets(10));

            // Green indicator
            icon = new Circle(5);
            icon.setStyle("-fx-fill: #00e676;");

            VBox infoBox = new VBox(2);
            nameLabel = new Label();
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

            timeLabel = new Label();
            timeLabel.setStyle("-fx-text-fill: #29b6f6; -fx-font-weight: bold;");

            HBox detailsRow = new HBox(10, timeLabel);
            infoBox.getChildren().addAll(nameLabel, detailsRow);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            dateLabel = new Label();
            dateLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

            root.getChildren().addAll(icon, infoBox, spacer, dateLabel);
        }

        @Override
        protected void updateItem(AttendanceRecord item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                nameLabel.setText(item.getMemberName());
                timeLabel.setText(item.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                dateLabel.setText(item.getCheckInTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

                setGraphic(root);
                setStyle("-fx-background-color: transparent;");
            }
        }
    }
}
