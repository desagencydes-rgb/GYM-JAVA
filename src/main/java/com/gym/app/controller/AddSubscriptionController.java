package com.gym.app.controller;

import com.gym.app.dao.MemberDAO;
import com.gym.app.dao.SubscriptionDAO;
import com.gym.app.model.Member;
import com.gym.app.model.Subscription;
import com.gym.app.util.TimeService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;

/**
 * Controller for assigning a Subscription Plan to a Member.
 */
public class AddSubscriptionController {

    @FXML
    private ComboBox<Member> memberComboBox;
    @FXML
    private ComboBox<String> planComboBox;

    // Field for custom plan name (e.g. "Special Promo")
    @FXML
    private TextField customPlanNameField;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextField priceField;

    private final MemberDAO memberDAO = new MemberDAO();
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();

    @FXML
    public void initialize() {
        // Populate Members
        memberComboBox.setItems(FXCollections.observableArrayList(memberDAO.getAllMembers()));
        memberComboBox.setConverter(new StringConverter<Member>() {
            @Override
            public String toString(Member m) {
                return m == null ? "" : m.getId() + " - " + m.getFullName();
            }

            @Override
            public Member fromString(String string) {
                return null;
            }
        });

        // Predefined Plans
        planComboBox.getItems().addAll("1 Month - Basic", "3 Months - Standard", "1 Year - Premium", "Custom");

        // Default start date is today
        startDatePicker.setValue(TimeService.getToday());

        // Add listeners to auto-fill details based on plan selection
        planComboBox.setOnAction(e -> updatePlanDetails());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updatePlanDetails());
    }

    /**
     * Updates Price and End Date fields based on the selected Plan and Start Date.
     */
    private void updatePlanDetails() {
        String plan = planComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        if (plan == null || start == null)
            return;

        if (plan.equals("Custom")) {
            // Enable manual editing for Custom plans
            customPlanNameField.setDisable(false);
            endDatePicker.setDisable(false);
            priceField.setEditable(true);

            if (priceField.getText().isEmpty())
                priceField.setText("0");

        } else {
            // Auto-fill and disable editing for Standard Plans
            customPlanNameField.setDisable(true);
            customPlanNameField.clear();

            endDatePicker.setDisable(true);
            priceField.setEditable(false);

            if (plan.contains("1 Month")) {
                priceField.setText("300");
                endDatePicker.setValue(start.plusMonths(1));
            } else if (plan.contains("3 Months")) {
                priceField.setText("800");
                endDatePicker.setValue(start.plusMonths(3));
            } else if (plan.contains("1 Year")) {
                priceField.setText("2500");
                endDatePicker.setValue(start.plusYears(1));
            }
        }
    }

    @FXML
    void handleSave(ActionEvent event) {
        Member selectedMember = memberComboBox.getValue();
        String selectedPlan = planComboBox.getValue();
        String customName = customPlanNameField.getText();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String priceText = priceField.getText();

        // Basic Validation
        if (selectedMember == null || selectedPlan == null || startDate == null || endDate == null
                || priceText.isEmpty()) {
            return;
        }

        try {
            Double price = Double.parseDouble(priceText);

            // Determine final plan name
            if (selectedPlan.equals("Custom") && !customName.isEmpty()) {
                selectedPlan = customName;
            } else if (selectedPlan.equals("Custom")) {
                selectedPlan = "Custom Plan";
            }

            Subscription sub = new Subscription(0, selectedMember.getId(), selectedPlan, startDate, endDate, price,
                    "Active");
            subscriptionDAO.addSubscription(sub);

            closeWindow();

        } catch (NumberFormatException e) {
            // Invalid price entered
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) memberComboBox.getScene().getWindow();
        stage.close();
    }
}
