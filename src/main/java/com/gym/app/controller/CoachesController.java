package com.gym.app.controller;

import com.gym.app.dao.CoachDAO;
import com.gym.app.dao.ScheduleDAO;
import com.gym.app.model.Coach;
import com.gym.app.model.ScheduleItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the Coaches & Schedule View.
 * Left Side: List of Coaches.
 * Right Side: Weekly Schedule for the selected coach.
 */
public class CoachesController {

    @FXML
    private ListView<Coach> coachesList;

    // Container for the schedule cards
    @FXML
    private VBox scheduleContainer;

    private final CoachDAO coachDAO = new CoachDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    private Coach selectedCoach;

    @FXML
    public void initialize() {
        refreshList();

        // Custom Cell Factory for Coach List (Name + Specialization)
        coachesList.setCellFactory(param -> new ListCell<Coach>() {
            @Override
            protected void updateItem(Coach item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(item.getName() + " - " + item.getSpecialization());
                    setStyle(
                            "-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10; -fx-cursor: hand;");
                }
            }
        });

        // Listener: Load schedule when a coach is clicked
        coachesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedCoach = newVal;
            if (newVal != null) {
                loadSchedule(newVal);
            } else {
                scheduleContainer.getChildren().clear();
            }
        });

        // Context Menu (Right Click) for Edit/Delete
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit Coach");
        editItem.setOnAction(e -> {
            if (selectedCoach != null)
                openCoachModal(selectedCoach);
        });

        MenuItem deleteItem = new MenuItem("Delete Coach");
        deleteItem.setOnAction(e -> {
            if (selectedCoach != null) {
                coachDAO.deleteCoach(selectedCoach.getId());
                refreshList();
                scheduleContainer.getChildren().clear();
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        coachesList.setContextMenu(contextMenu);
    }

    /**
     * Dynamically builds the schedule UI for the selected coach.
     */
    private void loadSchedule(Coach coach) {
        scheduleContainer.getChildren().clear();

        // Header with "Add Session" Button
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Weekly Plan for " + coach.getName());
        title.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addPlanBtn = new Button("+ Add Session");
        addPlanBtn.setStyle(
                "-fx-background-color: #29b6f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        addPlanBtn.setOnAction(e -> openScheduleModal(coach.getId()));

        header.getChildren().addAll(title, spacer, addPlanBtn);
        scheduleContainer.getChildren().add(header);

        // Fetch items from DB
        java.util.List<ScheduleItem> items = scheduleDAO.getSchedulesByCoachId(coach.getId());

        if (items.isEmpty()) {
            Label empty = new Label("No sessions planned yet.");
            empty.setStyle("-fx-text-fill: #666; -fx-padding: 20 0 0 0;");
            scheduleContainer.getChildren().add(empty);
        } else {
            // Add a card for each session
            for (ScheduleItem item : items) {
                addScheduleCard(item);
            }
        }
    }

    /**
     * Creates a UI Card for a single schedule item.
     */
    private void addScheduleCard(ScheduleItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("schedule-card");

        // Top Row: Day + Delete Button
        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label dayLabel = new Label(item.getDay());
        dayLabel.getStyleClass().add("schedule-day-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-size: 10px; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            scheduleDAO.deleteScheduleItem(item.getId());
            loadSchedule(selectedCoach); // refresh list
        });

        headerRow.getChildren().addAll(dayLabel, spacer, deleteBtn);

        // Bottom Row: Time Frame + Class Title
        HBox contentRow = new HBox(15);
        contentRow.setAlignment(Pos.CENTER_LEFT);

        HBox timePill = new HBox();
        timePill.getStyleClass().add("schedule-time-pill");
        Label timeLabel = new Label(
                item.getStartTime() + (item.getEndTime().isEmpty() ? "" : " - " + item.getEndTime()));
        timeLabel.getStyleClass().add("schedule-time-text");
        timePill.getChildren().add(timeLabel);

        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("schedule-class-title");

        contentRow.getChildren().addAll(timePill, titleLabel);

        card.getChildren().addAll(headerRow, contentRow);

        scheduleContainer.getChildren().add(card);
    }

    private void refreshList() {
        coachesList.setItems(FXCollections.observableArrayList(coachDAO.getAllCoaches()));
    }

    @FXML
    public void handleAddCoach() {
        openCoachModal(null);
    }

    private void openCoachModal(Coach coach) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/coach_form.fxml"));
            Parent root = loader.load();

            CoachFormController controller = loader.getController();
            controller.setCoachToEdit(coach);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(coach == null ? "Add Coach" : "Edit Coach");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            refreshList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openScheduleModal(int coachId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/schedule_form.fxml"));
            Parent root = loader.load();

            ScheduleFormController controller = loader.getController();
            controller.setCoachId(coachId);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add Session");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (selectedCoach != null)
                loadSchedule(selectedCoach);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
