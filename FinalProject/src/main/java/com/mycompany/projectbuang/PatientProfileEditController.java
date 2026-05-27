package com.mycompany.projectbuang;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class PatientProfileEditController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtAge;
    @FXML private ComboBox<String> comboGender;
    @FXML private ComboBox<String> comboBloodType;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtAddress;

    private Parent rootNode;
    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;

    @FXML
    public void initialize() {
        comboGender.setItems(FXCollections.observableArrayList("Male", "Female", "Other", "Prefer not to say"));
        comboBloodType.setItems(FXCollections.observableArrayList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"));
    }

    public void setRootNode(Parent rootNode) {
        this.rootNode = rootNode;
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
        txtFullName.setText(activePatient.fullName);
        txtAge.setText(patientProfile.age != null ? patientProfile.age : "");
        comboGender.setValue(patientProfile.gender != null && !patientProfile.gender.isEmpty() ? patientProfile.gender : null);
        comboBloodType.setValue(patientProfile.bloodType != null && !patientProfile.bloodType.isEmpty() ? patientProfile.bloodType : null);
        txtPhone.setText(patientProfile.phone != null ? patientProfile.phone : "");
        txtEmail.setText(patientProfile.email != null ? patientProfile.email : "");
        txtAddress.setText(patientProfile.address != null ? patientProfile.address : "");
    }

    @FXML
    private void handleSave() {
        String newFullName = txtFullName.getText().trim();
        String newAge = txtAge.getText().trim();
        String newGender = comboGender.getValue();
        String newBloodType = comboBloodType.getValue();
        String newPhone = txtPhone.getText().trim();
        String newEmail = txtEmail.getText().trim();
        String newAddress = txtAddress.getText().trim();

        if (newFullName.isEmpty()) {
            showAlert("Full Name cannot be empty.");
            return;
        }

        // Validate Age (must be integer between 0 and 120)
        if (newAge.isEmpty()) {
            showAlert("Age cannot be empty.");
            return;
        }
        try {
            int ageVal = Integer.parseInt(newAge);
            if (ageVal < 0 || ageVal > 120) {
                showAlert("Age must be between 0 and 120.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Age must be a valid whole number.");
            return;
        }

        // Validate Phone (exactly 11 digits, starts with 09)
        if (newPhone.isEmpty()) {
            showAlert("Phone number cannot be empty.");
            return;
        }
        if (!newPhone.matches("^09\\d{9}$")) {
            showAlert("Phone number must be exactly 11 digits and start with '09'.");
            return;
        }

        // Validate Email (must not contain any numerical digits)
        if (!newEmail.isEmpty()) {
            if (newEmail.matches(".*\\d.*")) {
                showAlert("Email address must not contain any numbers.");
                return;
            }
            if (!newEmail.contains("@") || !newEmail.contains(".")) {
                showAlert("Please enter a valid email address.");
                return;
            }
        }

        String oldFullName = activePatient.fullName;
        if (!newFullName.equals(oldFullName)) {
            // Update the name in UserAccount and transfer the PatientProfile key
            activePatient.fullName = newFullName;
            MockDatabase.PatientProfile profile = MockDatabase.patientDatabase.remove(oldFullName);
            if (profile != null) {
                profile.name = newFullName;
                MockDatabase.patientDatabase.put(newFullName, profile);
                this.patientProfile = profile;
            }
        }

        if (patientProfile != null) {
            patientProfile.age = newAge;
            patientProfile.gender = newGender != null ? newGender : "";
            patientProfile.bloodType = newBloodType != null ? newBloodType : "";
            patientProfile.phone = newPhone;
            patientProfile.email = newEmail;
            patientProfile.address = newAddress;

            // Clean up any existing "Age: XX" or "Age: XX | " from currentMeta
            String currentMeta = patientProfile.meta != null ? patientProfile.meta : "";
            if (currentMeta.contains("Age:")) {
                int pipeIndex = currentMeta.indexOf("|");
                if (pipeIndex != -1) {
                    currentMeta = currentMeta.substring(pipeIndex + 1).trim();
                } else {
                    currentMeta = "";
                }
            }
            patientProfile.meta = currentMeta;
        }

        // Save changes to physical database
        MockDatabase.saveDatabase();
        MockDatabase.logActivity("Profile updated for " + newFullName);

        // Refresh and close inline edit pane
        if (parentController != null) {
            parentController.initializeSession(activePatient);
            parentController.shrinkDetailView(rootNode);
        }
    }

    @FXML
    private void handleClose() {
        if (parentController != null && rootNode != null) {
            parentController.shrinkDetailView(rootNode);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
