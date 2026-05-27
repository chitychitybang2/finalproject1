package com.mycompany.projectbuang;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
public class AdminDashboardController {
    @FXML private Label lblAdminName, lblAdminRole, lblPatientName, lblPatientMeta, lblHeartRate, lblTemperature, lblOxygen, lblAdminCondition;
    @FXML private ListView<String> patientListView;
    @FXML private VBox vboxNotesContainer;
    @FXML private TextArea txtNewNoteInput;
    @FXML private Button btnDeleteAccount;
    
    @FXML private VBox vboxDemographics;
    @FXML private HBox hboxTelemetry;
    @FXML private HBox hboxTelemetry2;
    @FXML private VBox vboxNotesLogCard;
    
    @FXML private Label lblAdminActivity;
    @FXML private Label lblAdminWeight;
    @FXML private Label lblAdminBMI;
    @FXML private Label lblAdminSleep;
    
    // Analytics
    @FXML private VBox vboxAnalytics;
    @FXML private Label lblTotalPatients, lblActiveAlerts, lblTotalLogs, lblAvgHeartRate;
    @FXML private VBox vboxSymptomStats, vboxActivitiesLog;
    @FXML private Label lblAdminAge;
    @FXML private Label lblAdminGender;
    @FXML private Label lblAdminBloodType;
    @FXML private Label lblAdminPhone;
    @FXML private Label lblAdminEmail;
    @FXML private Label lblAdminAddress;

    private MockDatabase.UserAccount currentAdmin;
    private MockDatabase.PatientProfile selectedProfile;
    public void initializeSession(MockDatabase.UserAccount account) {
        this.currentAdmin = account;
        lblAdminName.setText(account.fullName);
        lblAdminRole.setText(account.roleTitle + " [ROOT]");
        
        ObservableList<String> entries = FXCollections.observableArrayList();
        entries.addAll(MockDatabase.patientDatabase.keySet());
        entries.add("SYSTEM ANALYTICS");
        entries.add("GLOBAL DIAGNOSTIC LOGS");
        
        patientListView.setItems(entries);
        patientListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadSelection(newV));
        patientListView.getSelectionModel().selectFirst();
    }
    private void loadSelection(String node) {
        if (node == null) return;
        vboxNotesContainer.getChildren().clear();
        
        if (node.equals("SYSTEM ANALYTICS")) {
            selectedProfile = null;
            txtNewNoteInput.setDisable(true);
            lblPatientName.setText("System Analytics");
            lblPatientMeta.setText("Overview of user metrics and recent activity streams.");
            
            vboxDemographics.setVisible(false);
            vboxDemographics.setManaged(false);
            hboxTelemetry.setVisible(false);
            hboxTelemetry.setManaged(false);
            hboxTelemetry2.setVisible(false);
            hboxTelemetry2.setManaged(false);
            vboxNotesLogCard.setVisible(false);
            vboxNotesLogCard.setManaged(false);
            
            vboxAnalytics.setVisible(true);
            vboxAnalytics.setManaged(true);
            btnDeleteAccount.setVisible(false);
            btnDeleteAccount.setManaged(false);
            
            renderAnalytics();
        } else if (node.equals("GLOBAL DIAGNOSTIC LOGS")) {
            selectedProfile = null;
            txtNewNoteInput.setDisable(true);
            lblPatientName.setText("System Overview");
            lblPatientMeta.setText("Displaying system execution event logs.");
            lblHeartRate.setText("OK"); lblTemperature.setText("SEC"); lblOxygen.setText("100");
            lblAdminCondition.setText("--");
            lblAdminCondition.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #8E8E93;");
            
            vboxDemographics.setVisible(false);
            vboxDemographics.setManaged(false);
            hboxTelemetry.setVisible(false);
            hboxTelemetry.setManaged(false);
            hboxTelemetry2.setVisible(false);
            hboxTelemetry2.setManaged(false);
            vboxNotesLogCard.setVisible(true);
            vboxNotesLogCard.setManaged(true);
            
            vboxAnalytics.setVisible(false);
            vboxAnalytics.setManaged(false);
            btnDeleteAccount.setVisible(false);
            btnDeleteAccount.setManaged(false);
            
            for (String log : MockDatabase.globalAuditLogs) appendBubble(log, "#E0F2FE", "#0369A1");
        } else {
            selectedProfile = MockDatabase.patientDatabase.get(node);
            txtNewNoteInput.setDisable(false);
            lblPatientName.setText(selectedProfile.name);
            lblPatientMeta.setText(selectedProfile.meta);
            lblHeartRate.setText(selectedProfile.heartRate);
            lblTemperature.setText(selectedProfile.temperature);
            lblOxygen.setText(selectedProfile.oxygen);
            
            String condText = selectedProfile.currentCondition != null ? selectedProfile.currentCondition : "Healthy";
            lblAdminCondition.setText(condText);
            if (condText.equalsIgnoreCase("Healthy") || condText.equalsIgnoreCase("Normal") || condText.equalsIgnoreCase("None")) {
                lblAdminCondition.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #34C759;");
            } else {
                lblAdminCondition.setStyle("-fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #FF3B30;");
            }
            
            vboxDemographics.setVisible(true);
            vboxDemographics.setManaged(true);
            hboxTelemetry.setVisible(true);
            hboxTelemetry.setManaged(true);
            hboxTelemetry2.setVisible(true);
            hboxTelemetry2.setManaged(true);
            vboxNotesLogCard.setVisible(true);
            vboxNotesLogCard.setManaged(true);
            
            vboxAnalytics.setVisible(false);
            vboxAnalytics.setManaged(false);
            btnDeleteAccount.setVisible(true);
            btnDeleteAccount.setManaged(true);
            
            lblAdminAge.setText(selectedProfile.age != null && !selectedProfile.age.isEmpty() ? selectedProfile.age : "--");
            lblAdminGender.setText(selectedProfile.gender != null && !selectedProfile.gender.isEmpty() ? selectedProfile.gender : "--");
            lblAdminBloodType.setText(selectedProfile.bloodType != null && !selectedProfile.bloodType.isEmpty() ? selectedProfile.bloodType : "--");
            lblAdminPhone.setText(selectedProfile.phone != null && !selectedProfile.phone.isEmpty() ? selectedProfile.phone : "--");
            lblAdminEmail.setText(selectedProfile.email != null && !selectedProfile.email.isEmpty() ? selectedProfile.email : "--");
            lblAdminAddress.setText(selectedProfile.address != null && !selectedProfile.address.isEmpty() ? selectedProfile.address : "--");
            
            lblAdminWeight.setText(selectedProfile.weight != null ? selectedProfile.weight + " kg" : "--");
            lblAdminBMI.setText("BMI: " + (selectedProfile.bmi != null ? selectedProfile.bmi : "--"));
            lblAdminSleep.setText((selectedProfile.sleepHours != null ? selectedProfile.sleepHours : "0") + "h " + (selectedProfile.sleepMinutes != null ? selectedProfile.sleepMinutes : "0") + "m");
            lblAdminActivity.setText((selectedProfile.moveCal != null ? selectedProfile.moveCal : "0") + " kcal | " + (selectedProfile.stepCount != null ? selectedProfile.stepCount : "0") + " steps");
            
            for (String note : selectedProfile.clinicalNotes) appendBubble(note, "#E5E5EA", "#1D1D1F");
        }
    }
    @FXML
    private void handleAddNewNote() {
        String msg = txtNewNoteInput.getText().trim();
        if (msg.isEmpty() || selectedProfile == null) return;
        String formatted = "[ADMIN OVERRIDE - " + currentAdmin.fullName + "]: " + msg;
        selectedProfile.clinicalNotes.add(formatted);
        MockDatabase.logActivity("Admin modification applied to chart index: " + selectedProfile.name);
        MockDatabase.saveDatabase();
        appendBubble(formatted, "#E5E5EA", "#1D1D1F");
        txtNewNoteInput.clear();
    }
    private void appendBubble(String message, String bg, String text) {
        Label lbl = new Label(message);
        lbl.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + text + "; -fx-background-radius: 8; -fx-font-size: 13px;");
        lbl.setPadding(new Insets(8, 12, 8, 12));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setWrapText(true);
        vboxNotesContainer.getChildren().add(lbl);
    }

    private void renderAnalytics() {
        int totalPatients = MockDatabase.patientDatabase.size();
        lblTotalPatients.setText(String.valueOf(totalPatients));
        
        int activeAlerts = 0;
        double sumHR = 0.0;
        int parsedHRCount = 0;
        java.util.Map<String, Integer> conditionCounts = new java.util.HashMap<>();
        
        for (MockDatabase.PatientProfile profile : MockDatabase.patientDatabase.values()) {
            String cond = profile.currentCondition != null ? profile.currentCondition : "Healthy";
            if (!cond.equalsIgnoreCase("Healthy") && !cond.equalsIgnoreCase("Normal") && !cond.equalsIgnoreCase("None")) {
                activeAlerts++;
            }
            conditionCounts.put(cond, conditionCounts.getOrDefault(cond, 0) + 1);
            
            if (profile.heartRate != null && !profile.heartRate.equals("--") && !profile.heartRate.equals("0")) {
                try {
                    sumHR += Double.parseDouble(profile.heartRate.trim());
                    parsedHRCount++;
                } catch (NumberFormatException ignored) {}
            }
        }
        
        lblActiveAlerts.setText(String.valueOf(activeAlerts));
        lblTotalLogs.setText(String.valueOf(MockDatabase.globalAuditLogs.size()));
        
        if (parsedHRCount > 0) {
            lblAvgHeartRate.setText(String.format("%.1f BPM", sumHR / parsedHRCount));
        } else {
            lblAvgHeartRate.setText("--");
        }
        
        // Render Symptom stats
        vboxSymptomStats.getChildren().clear();
        for (java.util.Map.Entry<String, Integer> entry : conditionCounts.entrySet()) {
            String cond = entry.getKey();
            int count = entry.getValue();
            
            HBox row = new HBox();
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setSpacing(12.0);
            
            Label lblSymptom = new Label(cond);
            lblSymptom.setPrefWidth(140.0);
            lblSymptom.setStyle("-fx-font-weight: bold; -fx-text-fill: #1D1D1F; -fx-font-size: 13px;");
            
            // Progress Bar representation
            ProgressBar bar = new ProgressBar();
            bar.setProgress(totalPatients > 0 ? (double) count / totalPatients : 0);
            bar.setPrefWidth(300.0);
            
            // Styled differently based on severity
            String barColor = "#34C759"; // healthy (green)
            if (!cond.equalsIgnoreCase("Healthy") && !cond.equalsIgnoreCase("Normal") && !cond.equalsIgnoreCase("None")) {
                barColor = "#FF3B30"; // alert (red)
            }
            bar.setStyle("-fx-accent: " + barColor + ";");
            
            Label lblCount = new Label(count + " User(s)");
            lblCount.setStyle("-fx-text-fill: #86868B; -fx-font-size: 13px;");
            
            row.getChildren().addAll(lblSymptom, bar, lblCount);
            vboxSymptomStats.getChildren().add(row);
        }
        
        // Render Activities log with icons
        vboxActivitiesLog.getChildren().clear();
        java.util.List<String> logs = MockDatabase.globalAuditLogs;
        // Show last 20 logs in reverse chronological order
        int start = Math.max(0, logs.size() - 20);
        for (int i = logs.size() - 1; i >= start; i--) {
            String logEntry = logs.get(i);
            
            HBox row = new HBox();
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setSpacing(10.0);
            row.setPadding(new Insets(8, 12, 8, 12));
            row.setStyle("-fx-background-color: #F5F5F7; -fx-background-radius: 10;");
            
            String emoji = "ℹ️";
            
            if (logEntry.contains("Condition updated")) {
                emoji = "📋";
            } else if (logEntry.contains("reset")) {
                emoji = "🔄";
            } else if (logEntry.contains("Weight record added") || logEntry.contains("Weight record")) {
                emoji = "⚖";
            } else if (logEntry.contains("Stay Fit Plan updated")) {
                emoji = "🏃";
            } else if (logEntry.contains("Steps logged")) {
                emoji = "🔥";
            } else if (logEntry.contains("Sleep logged")) {
                emoji = "💤";
            } else if (logEntry.contains("Heart rate measurement") || logEntry.contains("SpO2 measurement")) {
                emoji = "❤️";
            }
            
            Label lblEmoji = new Label(emoji);
            lblEmoji.setStyle("-fx-font-size: 16px; -fx-min-width: 24px; -fx-alignment: CENTER;");
            
            Label lblText = new Label(logEntry);
            lblText.setStyle("-fx-font-size: 13px; -fx-text-fill: #1D1D1F;");
            lblText.setWrapText(true);
            
            row.getChildren().addAll(lblEmoji, lblText);
            vboxActivitiesLog.getChildren().add(row);
        }
    }

    @FXML
    private void handleDeleteAccount() {
        if (selectedProfile == null) return;
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Delete " + selectedProfile.name + "'s Account?");
        alert.setContentText("This will permanently delete the user profile and their login account. This action cannot be undone.");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            String patientName = selectedProfile.name;
            
            // 1. Remove from patientDatabase
            MockDatabase.patientDatabase.remove(patientName);
            
            // 2. Remove corresponding login user account where fullName matches patientName
            String targetUsername = null;
            for (MockDatabase.UserAccount acc : MockDatabase.userDatabase.values()) {
                if (acc.fullName != null && acc.fullName.equalsIgnoreCase(patientName)) {
                    targetUsername = acc.username;
                    break;
                }
            }
            if (targetUsername != null) {
                MockDatabase.userDatabase.remove(targetUsername);
            }
            
            // 3. Log activity
            MockDatabase.logActivity("Account and profile deleted for user: " + patientName);
            
            // 4. Save to database files
            MockDatabase.saveDatabase();
            
            // 5. Refresh sidebar list
            ObservableList<String> entries = FXCollections.observableArrayList();
            entries.addAll(MockDatabase.patientDatabase.keySet());
            entries.add("SYSTEM ANALYTICS");
            entries.add("GLOBAL DIAGNOSTIC LOGS");
            patientListView.setItems(entries);
            
            // 6. Select first item (usually another patient, or System Analytics if empty)
            patientListView.getSelectionModel().selectFirst();
            
            javafx.scene.control.Alert success = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            success.setTitle("Account Deleted");
            success.setHeaderText(null);
            success.setContentText("The user account and user profile for " + patientName + " have been successfully deleted.");
            success.showAndWait();
        }
    }

    private void showReadOnlyPopup(String featureName, String currentValue, String status, String statusColor, String description) {
        if (selectedProfile == null) return;
        
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        stage.setTitle("Monitoring Record - " + featureName);
        
        VBox root = new VBox(16.0);
        root.setPadding(new Insets(24.0));
        root.setStyle("-fx-background-color: #F5F5F7; -fx-font-family: '.AppleSystemUIFont', 'SF Pro', sans-serif;");
        root.setPrefWidth(420.0);
        
        // Header
        Label lblHeader = new Label(featureName.toUpperCase() + " STATUS");
        lblHeader.setStyle("-fx-text-fill: #86868B; -fx-font-size: 11px; -fx-font-weight: 700; -fx-letter-spacing: 0.5px;");
        
        // Patient name
        Label lblPatient = new Label("User: " + selectedProfile.name);
        lblPatient.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1D1D1F;");
        
        // Card content area
        VBox card = new VBox(12.0);
        card.setPadding(new Insets(16.0));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 8, 0, 0, 2);");
        
        // Current value
        Label lblVal = new Label("Current Record: " + currentValue);
        lblVal.setStyle("-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #1D1D1F;");
        
        // Status indicator
        Label lblStatus = new Label("Health Status: " + status);
        lblStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: " + statusColor + ";");
        
        // Description / Threshold context
        Label lblDesc = new Label(description);
        lblDesc.setStyle("-fx-font-size: 12px; -fx-text-fill: #86868B;");
        lblDesc.setWrapText(true);
        
        card.getChildren().addAll(lblVal, lblStatus, lblDesc);
        
        // Close button
        Button btnClose = new Button("Close Monitor");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setOnAction(e -> stage.close());
        btnClose.setStyle("-fx-background-color: #0071E3; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 16 8 16; -fx-font-weight: 600; -fx-cursor: hand;");
        
        root.getChildren().addAll(lblHeader, lblPatient, card, btnClose);
        
        stage.setScene(new javafx.scene.Scene(root));
        stage.showAndWait();
    }
    
    @FXML
    private void handleOpenHeartDetails() {
        if (selectedProfile == null) return;
        String val = selectedProfile.heartRate;
        String currentValue = (val != null && !val.equals("--")) ? val + " BPM" : "--";
        String status = "No Data";
        String statusColor = "#8E8E93";
        if (val != null && !val.equals("--")) {
            try {
                double hr = Double.parseDouble(val.trim());
                if (hr >= 60 && hr <= 100) {
                    status = "Normal (Okay)";
                    statusColor = "#34C759";
                } else {
                    status = "Abnormal / High Alert";
                    statusColor = "#FF3B30";
                }
            } catch (NumberFormatException ignored) {}
        }
        showReadOnlyPopup("Heart Rate", currentValue, status, statusColor, "Normal resting heart rate range is between 60 and 100 BPM.");
    }
    
    @FXML
    private void handleOpenOxygenDetails() {
        if (selectedProfile == null) return;
        String val = selectedProfile.oxygen;
        String currentValue = (val != null && !val.equals("--")) ? val + "% SpO2" : "--";
        String status = "No Data";
        String statusColor = "#8E8E93";
        if (val != null && !val.equals("--")) {
            try {
                double ox = Double.parseDouble(val.trim());
                if (ox >= 95) {
                    status = "Normal (Okay)";
                    statusColor = "#34C759";
                } else {
                    status = "Low Oxygen Alert";
                    statusColor = "#FF3B30";
                }
            } catch (NumberFormatException ignored) {}
        }
        showReadOnlyPopup("Oxygen Matrix", currentValue, status, statusColor, "Normal blood oxygen saturation levels range between 95% and 100%.");
    }
    
    @FXML
    private void handleOpenConditionDetails() {
        if (selectedProfile == null) return;
        String cond = selectedProfile.currentCondition != null ? selectedProfile.currentCondition : "Healthy";
        String mood = selectedProfile.stateOfMind != null ? selectedProfile.stateOfMind : "Neutral";
        String currentValue = "Condition: " + cond + " | Mood: " + mood;
        String status;
        String statusColor;
        if (cond.equalsIgnoreCase("Healthy") || cond.equalsIgnoreCase("Normal") || cond.equalsIgnoreCase("None")) {
            status = "Normal (Healthy)";
            statusColor = "#34C759";
        } else {
            status = "Active Health Alert";
            statusColor = "#FF3B30";
        }
        showReadOnlyPopup("Current Condition", currentValue, status, statusColor, "Current diagnosed medical condition status and user state of mind log.");
    }
    
    @FXML
    private void handleOpenActivityDetails() {
        if (selectedProfile == null) return;
        String stepsVal = selectedProfile.stepCount != null ? selectedProfile.stepCount : "0";
        String moveVal = selectedProfile.moveCal != null ? selectedProfile.moveCal : "0";
        String currentValue = stepsVal + " steps | " + moveVal + " kcal";
        String status = "Inactive / Low Activity";
        String statusColor = "#FF9500";
        try {
            double steps = Double.parseDouble(stepsVal.trim());
            if (steps >= 5000) {
                status = "Normal (Okay)";
                statusColor = "#34C759";
            }
        } catch (NumberFormatException ignored) {}
        showReadOnlyPopup("Activity & Steps", currentValue, status, statusColor, "Active baseline target is 5,000 steps per day for moderate physical mobility.");
    }
    
    @FXML
    private void handleOpenWeightDetails() {
        if (selectedProfile == null) return;
        String weightVal = selectedProfile.weight != null ? selectedProfile.weight : "0";
        String bmiVal = selectedProfile.bmi != null ? selectedProfile.bmi : "0";
        String currentValue = weightVal + " kg (BMI: " + bmiVal + ")";
        String status = "Out of Range";
        String statusColor = "#FF9500";
        try {
            double bmi = Double.parseDouble(bmiVal.trim());
            if (bmi == 0.0) {
                status = "Not Set";
                statusColor = "#8E8E93";
            } else if (bmi < 18.5) {
                status = "Underweight";
                statusColor = "#0A84FF";
            } else if (bmi < 25.0) {
                status = "Normal Weight (Okay)";
                statusColor = "#34C759";
            } else if (bmi < 30.0) {
                status = "Overweight";
                statusColor = "#FF9500";
            } else {
                status = "Obese Alert";
                statusColor = "#FF3B30";
            }
        } catch (NumberFormatException ignored) {}
        showReadOnlyPopup("Weight & BMI", currentValue, status, statusColor, "Body Mass Index (BMI) categories: Underweight (<18.5), Normal (18.5 - 24.9), Overweight (25 - 29.9), Obese (30+).");
    }
    
    @FXML
    private void handleOpenSleepDetails() {
        if (selectedProfile == null) return;
        String hrsVal = selectedProfile.sleepHours != null ? selectedProfile.sleepHours : "0";
        String minsVal = selectedProfile.sleepMinutes != null ? selectedProfile.sleepMinutes : "0";
        String currentValue = hrsVal + "h " + minsVal + "m";
        String status = "Sleep Deprived Alert";
        String statusColor = "#FF3B30";
        try {
            double hrs = Double.parseDouble(hrsVal.trim());
            if (hrs >= 7) {
                status = "Normal (Okay)";
                statusColor = "#34C759";
            }
        } catch (NumberFormatException ignored) {}
        showReadOnlyPopup("Sleep Record", currentValue, status, statusColor, "Recommended healthy adult sleep duration is between 7 and 9 hours per night.");
    }
    
    @FXML
    private void handleOpenStayFitPlan() {
        if (selectedProfile == null) return;
        String val = selectedProfile.temperature;
        String currentValue = (val != null && !val.equals("--")) ? val + " °C" : "--";
        String status = "No Data";
        String statusColor = "#8E8E93";
        if (val != null && !val.equals("--")) {
            try {
                double temp = Double.parseDouble(val.trim());
                if (temp >= 36.1 && temp <= 37.2) {
                    status = "Normal (Okay)";
                    statusColor = "#34C759";
                } else {
                    status = "Fever / Abnormal Temp Alert";
                    statusColor = "#FF3B30";
                }
            } catch (NumberFormatException ignored) {}
        }
        showReadOnlyPopup("Temperature Monitoring", currentValue, status, statusColor, "Normal human body temperature range is 36.1°C to 37.2°C. Temperatures above 37.2°C indicate fever.");
    }

    @FXML
    private void handleLogout() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("Login.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) patientListView.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new javafx.scene.Scene(root));
            }
            stage.show();
            MockDatabase.logActivity("Admin Session terminated. Routed to Login.");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
}
