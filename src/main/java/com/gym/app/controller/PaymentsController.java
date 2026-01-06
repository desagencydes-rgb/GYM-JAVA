package com.gym.app.controller;

import com.gym.app.dao.MemberDAO;
import com.gym.app.dao.PaymentDAO;
import com.gym.app.model.Member;
import com.gym.app.model.Payment;
import com.gym.app.model.PaymentDTO;
import com.gym.app.util.TimeService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * Controller for the Payments Screen.
 * Lists recent payments and allows adding new payments.
 */
public class PaymentsController {

    @FXML
    private ListView<PaymentDTO> paymentsListView;
    @FXML
    private ComboBox<Member> memberComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private ComboBox<String> methodBox;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final MemberDAO memberDAO = new MemberDAO();

    @FXML
    public void initialize() {
        // Init Inputs
        methodBox.getItems().addAll("Cash", "TPE");
        methodBox.getSelectionModel().selectFirst();

        memberComboBox.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
        memberComboBox.setConverter(new StringConverter<Member>() {
            @Override
            public String toString(Member m) {
                return m == null ? "" : m.getFullName();
            }

            @Override
            public Member fromString(String s) {
                return null;
            }
        });

        refreshList();

        // Set Custom Cell Factory for Payment Cards
        paymentsListView.setCellFactory(param -> new PaymentListCell());
    }

    /**
     * Adds a new payment record.
     */
    @FXML
    void handleAddPayment(ActionEvent event) {
        Member m = memberComboBox.getValue();
        String amtStr = amountField.getText();
        String method = methodBox.getValue();

        if (m != null && !amtStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amtStr);

                // Add Payment
                Payment p = new Payment(0, m.getId(), amount, TimeService.getToday(), method);
                paymentDAO.addPayment(p);

                // Reset UI
                amountField.clear();
                refreshList();

            } catch (Exception e) {
                // Invalid number
            }
        }
    }

    private void refreshList() {
        paymentsListView.setItems(FXCollections.observableArrayList(paymentDAO.getAllPaymentDTOs()));
    }

    // --- Custom Payment List Cell ---
    private class PaymentListCell extends ListCell<PaymentDTO> {
        private final HBox root;
        private final Label amountLabel;
        private final Label nameLabel;
        private final Label dateLabel;
        private final Label methodLabel;
        private final Button deleteBtn;

        public PaymentListCell() {
            root = new HBox(15);
            root.getStyleClass().add("payment-card");
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(10, 15, 10, 15));

            // Amount Badge (Green background)
            VBox amountBox = new VBox();
            amountBox.setAlignment(Pos.CENTER);
            amountBox.setStyle(
                    "-fx-background-color: rgba(0, 230, 118, 0.1); -fx-background-radius: 8; -fx-padding: 5 10;");

            amountLabel = new Label();
            amountLabel.setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold; -fx-font-size: 16px;");
            amountBox.getChildren().add(amountLabel);

            // Member and Date Info
            VBox infoBox = new VBox(2);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            nameLabel = new Label();
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
            dateLabel = new Label();
            dateLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px;");
            infoBox.getChildren().addAll(nameLabel, dateLabel);

            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Payment Method Pill (Cash/TPE)
            methodLabel = new Label();
            methodLabel.setStyle(
                    "-fx-background-color: #333; -fx-text-fill: #ccc; -fx-background-radius: 10; -fx-padding: 3 8; -fx-font-size: 10px;");

            // Delete Action
            deleteBtn = new Button("✕");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                PaymentDTO item = getItem();
                if (item != null) {
                    paymentDAO.deletePayment(item.getId());
                    refreshList();
                }
            });

            root.getChildren().addAll(amountBox, infoBox, spacer, methodLabel, deleteBtn);
        }

        @Override
        protected void updateItem(PaymentDTO item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                amountLabel.setText(String.format("%.0f MAD", item.getAmount()));
                nameLabel.setText(item.getMemberName());
                dateLabel.setText(item.getDate().toString());
                methodLabel.setText(item.getMethod());

                setGraphic(root);
                setStyle("-fx-background-color: transparent;");
            }
        }
    }
}
