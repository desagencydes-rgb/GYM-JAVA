package com.gym.app.controller;

import com.gym.app.dao.MemberDAO;
import com.gym.app.model.Member;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
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
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Members Management screen.
 * Lists all members and provides options to Add, Edit, Delete, or Assign Plans.
 */
public class MembersListController {

    @FXML
    private ListView<Member> membersListView;
    @FXML
    private TextField searchField;

    private final MemberDAO memberDAO = new MemberDAO();

    /**
     * Initializes the controller class.
     * Sets up the list view cell factory.
     */
    @FXML
    public void initialize() {
        refreshList();

        // Set custom cell factory to render member cards instead of default string
        membersListView.setCellFactory(listView -> new MemberListCell());
    }

    /**
     * Refreshes the ListView by fetching all members from the database.
     */
    private void refreshList() {
        membersListView.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
    }

    /**
     * Opens the modal to add a new member.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void handleAddMember(ActionEvent event) {
        openModal("/fxml/member_form.fxml", "Add New Member", null);
    }

    /**
     * Opens the modal to assign a subscription plan to a member.
     * 
     * @param event The ActionEvent triggered by the button.
     */
    @FXML
    void handleAssignPlan(ActionEvent event) {
        openModal("/fxml/add_subscription.fxml", "Assign Plan to Member", null);
    }

    /**
     * Custom ListCell implementation to display rich member cards.
     */
    private class MemberListCell extends ListCell<Member> {
        private final HBox root;
        private final Label nameLabel;
        private final Label dateLabel;
        private final Label statusLabel;
        private final Button editBtn;
        private final Button deleteBtn;

        /**
         * Constructor for MemberListCell.
         * Initializes the UI components for the cell.
         */
        public MemberListCell() {
            root = new HBox(15);
            root.setAlignment(Pos.CENTER_LEFT);
            root.getStyleClass().add("member-card");

            // Avatar placeholder (Circle)
            Circle avatar = new Circle(20);
            avatar.getStyleClass().add("avatar-circle");

            // Info Column: Name + Date
            VBox infoBox = new VBox(2);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            nameLabel = new Label();
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

            // Typically normally shows Active Subscription date, here showing Registration
            // Date for simplicity
            dateLabel = new Label();
            dateLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");

            infoBox.getChildren().addAll(nameLabel, dateLabel);

            // Spacer to push actions to right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Status Badge (Dummy "New" badge for now)
            statusLabel = new Label("New");
            statusLabel.getStyleClass().add("badge-new");

            // --- Action Buttons ---

            editBtn = new Button("Edit");
            editBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-underline: true; -fx-cursor: hand;");
            editBtn.setOnAction(e -> {
                Member m = getItem();
                if (m != null)
                    openModal("/fxml/member_form.fxml", "Edit Member", m);
            });

            deleteBtn = new Button("✕");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                Member m = getItem();
                if (m != null) {
                    memberDAO.deleteMember(m.getId());
                    refreshList();
                }
            });

            root.getChildren().addAll(avatar, infoBox, spacer, statusLabel, editBtn, deleteBtn);
        }

        @Override
        protected void updateItem(Member member, boolean empty) {
            super.updateItem(member, empty);

            if (empty || member == null) {
                setGraphic(null);
                setText(null);
            } else {
                // Bind data
                nameLabel.setText(member.getFullName());
                dateLabel.setText("Joined: " + member.getRegistrationDate().toString());

                // Visual feedback for selection
                if (isSelected()) {
                    root.getStyleClass().add("member-card-selected");
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                } else {
                    root.getStyleClass().remove("member-card-selected");
                    root.getStyleClass().add("member-card");
                }

                setGraphic(root);
            }
        }
    }

    /**
     * Helper to open a modal dialog (e.g. Add/Edit Member).
     */
    private void openModal(String fxml, String title, Member memberToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Pass the member object if editing
            if (memberToEdit != null && loader.getController() instanceof MemberFormController) {
                ((MemberFormController) loader.getController()).setMemberToEdit(memberToEdit);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh list after modal closes (in case of changes)
            refreshList();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
