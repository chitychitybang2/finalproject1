package com.mycompany.projectbuang;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class StateOfMindDetailsController {

    @FXML private Label lblMoodTitle;
    @FXML private Label lblConditionTitle;
    @FXML private ComboBox<String> comboMood;
    @FXML private ComboBox<String> comboCondition;
    // Removed txtMoodDesc TextArea

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
        this.rootNode = rootNode;
    }

    @FXML
    public void initialize() {
        comboMood.setItems(FXCollections.observableArrayList(
            "Calm", "Joyful", "Energetic", "Anxious", "Tired", "Stressed"
        ));
        comboCondition.setItems(FXCollections.observableArrayList(
            "Healthy", "Fever", "Cough", "Cold / Flu", "Headache", "Sore Throat", "Fatigued", "Shortness of Breath"
        ));
    }

    public void setSessionContext(MockDatabase.UserAccount account, PatientDashboardController parent) {
        this.activePatient = account;
        this.parentController = parent;
        this.patientProfile = MockDatabase.patientDatabase.get(account.fullName);

        if (patientProfile != null) {
            loadProfileData();
        }
    }

    private void loadProfileData() {
        if (patientProfile == null) return;

        String currentBadge = patientProfile.stateOfMindBadge != null ? patientProfile.stateOfMindBadge : "Calm";
        comboMood.setValue(currentBadge);
        
        String currentCond = patientProfile.currentCondition != null ? patientProfile.currentCondition : "Healthy";
        comboCondition.setValue(currentCond);
        
        // Description field removed from UI
        updateMoodDisplay(currentBadge);
        updateConditionDisplay(currentCond);
    }

    @FXML
    private void handleMoodSelected() {
        String selected = comboMood.getValue();
        if (selected != null) {
            updateMoodDisplay(selected);
        }
    }

    @FXML
    private void handleConditionSelected() {
        String selected = comboCondition.getValue();
        if (selected != null) {
            updateConditionDisplay(selected);
        }
    }

    private void updateMoodDisplay(String mood) {
        lblMoodTitle.setText(mood);
        String colorHex = "#34C759"; // default Green
        
        switch (mood) {
            case "Calm":
                colorHex = "#34C759"; // Green
                break;
            case "Joyful":
                colorHex = "#FFD60A"; // Yellow
                break;
            case "Energetic":
                colorHex = "#FF9500"; // Orange
                break;
            case "Anxious":
                colorHex = "#0071E3"; // Blue
                break;
            case "Tired":
                colorHex = "#8E8E93"; // Gray
                break;
            case "Stressed":
                colorHex = "#FF3B30"; // Red
                break;
        }
        
        lblMoodTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: " + colorHex + ";");
    }

    private void updateConditionDisplay(String cond) {
        lblConditionTitle.setText(cond);
        if (cond.equalsIgnoreCase("Healthy") || cond.equalsIgnoreCase("Normal") || cond.equalsIgnoreCase("None")) {
            lblConditionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #34C759;"); // Green
        } else {
            lblConditionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #FF3B30;"); // Red (Alert)
        }
    }

    @FXML
    private void handleSave() {
        if (patientProfile == null) return;

        String selectedMood = comboMood.getValue();
        if (selectedMood == null) {
            selectedMood = "Calm";
        }
        String selectedCond = comboCondition.getValue();
        if (selectedCond == null || selectedCond.trim().isEmpty()) {
            selectedCond = "Healthy";
        }
        patientProfile.stateOfMindBadge = selectedMood;
        patientProfile.stateOfMind = selectedMood;
        patientProfile.currentCondition = selectedCond;

        // Add notation to clinical updates
        String note = "[CONDITION UPDATE]: Condition: " + selectedCond + " | Mood: " + selectedMood;
        patientProfile.clinicalNotes.add(note);

        MockDatabase.saveDatabase();
        MockDatabase.logActivity("Condition updated to " + selectedCond + " (Mood: " + selectedMood + ") for " + activePatient.fullName);

        if (parentController != null) {
            parentController.initializeSession(activePatient);
            parentController.shrinkDetailView(rootNode);
        }
    }


    @FXML
    private void handleClose() {
        if (parentController != null && rootNode != null) {
            parentController.shrinkDetailView(rootNode);
        } else {
            Stage stage = (Stage) comboMood.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }
}
