package com.mycompany.projectbuang;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Random;

public class HeartDetailsController {

    @FXML private Button btnTabDay, btnTabWeek, btnTabMonth, btnTabYear;
    @FXML private Label lblDateSelector, lblHeartRateRange, lblLastUpdatedValue, lblLastUpdatedTime, lblRestingPill;
    @FXML private Label lblDetailOxygen, lblDetailTemperature;
    @FXML private VBox vboxLastUpdatedBlock;
    @FXML private LineChart<String, Number> chartHeart;
    @FXML private Button btnSubHeartRate, btnSubHRV;
    @FXML private VBox vboxScaleDetailsList;

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
        this.rootNode = rootNode;
    }

    private String currentTab = "Day"; // "Day", "Week", "Month", "Year"
    private String currentSubTab = "HeartRate"; // "HeartRate", "HRV"

    @FXML
    public void initialize() {
        // Initial button styles and defaults
        updateTabStyles();
        updateSubTabStyles();
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

        // Populate Resting Heart rate pill
        lblRestingPill.setText("Resting heart rate " + patientProfile.heartRate + " bpm");
        lblDetailOxygen.setText("SpO2: " + (patientProfile.oxygen != null ? patientProfile.oxygen : "--") + "%");
        lblDetailTemperature.setText("Temp: " + (patientProfile.temperature != null ? patientProfile.temperature : "--") + "°C");

        // Day vs Week vs Year text updates
        if (currentTab.equals("Day")) {
            lblDateSelector.setText("23 Apr (Thu)");
            lblHeartRateRange.setText(patientProfile.heartRateMinMaxRange != null ? patientProfile.heartRateMinMaxRange : "60-185");
            vboxLastUpdatedBlock.setVisible(true);
            vboxLastUpdatedBlock.setManaged(true);
            lblLastUpdatedValue.setText(patientProfile.heartRate != null ? patientProfile.heartRate : "98");
            lblLastUpdatedTime.setText("Last updated 20:05");
        } else if (currentTab.equals("Week")) {
            lblDateSelector.setText("20 Apr – 26 Apr");
            lblHeartRateRange.setText(patientProfile.heartRateMinMaxRange != null ? patientProfile.heartRateMinMaxRange : "60-185");
            vboxLastUpdatedBlock.setVisible(false);
            vboxLastUpdatedBlock.setManaged(false);
        } else if (currentTab.equals("Month")) {
            lblDateSelector.setText("April 2026");
            lblHeartRateRange.setText("50-185");
            vboxLastUpdatedBlock.setVisible(false);
            vboxLastUpdatedBlock.setManaged(false);
        } else { // Year
            lblDateSelector.setText("2026");
            lblHeartRateRange.setText("45-188");
            vboxLastUpdatedBlock.setVisible(false);
            vboxLastUpdatedBlock.setManaged(false);
        }

        if (currentSubTab.equals("HRV")) {
            // HRV units are ms, typical range is 30-100 ms
            lblHeartRateRange.setText("32–94");
            lblRestingPill.setText("Average HRV: 58 ms");
            if (currentTab.equals("Day")) {
                lblLastUpdatedValue.setText("62");
                lblLastUpdatedTime.setText("Last measured 20:05");
            }
        }

        // Draw Canvas
        drawHeartChart();
    }

    private javafx.scene.layout.HBox createDetailRow(String leftText, String rightText, String colorHex) {
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hbox.setPadding(new javafx.geometry.Insets(4, 0, 4, 0));
        
        Label lblLeft = new Label(leftText);
        lblLeft.setStyle("-fx-text-fill: #1D1D1F; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Label lblRight = new Label(rightText);
        lblRight.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        hbox.getChildren().addAll(lblLeft, spacer, lblRight);
        return hbox;
    }

      private void drawHeartChart() {
        chartHeart.getData().clear();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        String name = currentSubTab.equals("HeartRate") ? "Heart Rate" : "HRV";
        series.setName(name);

        String colorHex = currentSubTab.equals("HeartRate") ? "#FF2D55" : "#BF5AF2";

        if (vboxScaleDetailsList != null) {
            vboxScaleDetailsList.getChildren().clear();
        }

        if (currentTab.equals("Day")) {
            double[] hrs = {68, 110, 75, 120, 175, 160, 115, 130, 98, 88};
            String[] times = {"17:00", "17:15", "17:30", "17:45", "18:00", "18:30", "19:00", "19:15", "19:30", "19:45"};
            
            for (int i = 0; i < hrs.length; i++) {
                double val = hrs[i];
                if (currentSubTab.equals("HRV")) {
                    val = 32 + (val - 60) * 0.4;
                }
                series.getData().add(new XYChart.Data<>(times[i], val));
                if (vboxScaleDetailsList != null) {
                    String unit = currentSubTab.equals("HeartRate") ? " BPM" : " ms";
                    vboxScaleDetailsList.getChildren().add(createDetailRow(times[i] + " (Reading)", String.format("%.0f%s", val, unit), colorHex));
                }
            }
        } else if (currentTab.equals("Week")) {
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            double[] hrs = {65, 70, 72, 98, 80, 85, 75};
            for (int i = 0; i < days.length; i++) {
                double val = hrs[i];
                if (currentSubTab.equals("HRV")) {
                    val = 55 + (val - 75) * 0.5;
                }
                series.getData().add(new XYChart.Data<>(days[i], val));
                if (vboxScaleDetailsList != null) {
                    String unit = currentSubTab.equals("HeartRate") ? " BPM" : " ms";
                    vboxScaleDetailsList.getChildren().add(createDetailRow(days[i] + " (Average)", String.format("%.0f%s", val, unit), colorHex));
                }
            }
        } else if (currentTab.equals("Month")) {
            String[] weeks = {"Week 1", "Week 2", "Week 3", "Week 4"};
            double[] hrs = {72, 75, 68, 70};
            for (int i = 0; i < weeks.length; i++) {
                double val = hrs[i];
                if (currentSubTab.equals("HRV")) {
                    val = 58 + (val - 70) * 0.6;
                }
                series.getData().add(new XYChart.Data<>(weeks[i], val));
                if (vboxScaleDetailsList != null) {
                    String unit = currentSubTab.equals("HeartRate") ? " BPM" : " ms";
                    vboxScaleDetailsList.getChildren().add(createDetailRow(weeks[i] + " (Average)", String.format("%.0f%s", val, unit), colorHex));
                }
            }
        } else {
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            double[] hrs = {70, 72, 71, 74, 75, 73, 70, 68, 69, 72, 70, 71};
            for (int i = 0; i < months.length; i++) {
                double val = hrs[i];
                if (currentSubTab.equals("HRV")) {
                    val = 56 + (val - 70) * 0.5;
                }
                series.getData().add(new XYChart.Data<>(months[i], val));
                if (vboxScaleDetailsList != null) {
                    String unit = currentSubTab.equals("HeartRate") ? " BPM" : " ms";
                    vboxScaleDetailsList.getChildren().add(createDetailRow(months[i] + " (Average)", String.format("%.0f%s", val, unit), colorHex));
                }
            }
        }

        chartHeart.getData().add(series);

        Platform.runLater(() -> {
            if (series.getNode() != null) {
                Node line = series.getNode().lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: " + colorHex + "; -fx-stroke-width: 2.5;");
                }
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-color: " + colorHex + ", white; -fx-background-radius: 4px; -fx-padding: 4px;");
                    }
                }
            }
        });
    }

    private void updateTabStyles() {
        Button[] tabs = {btnTabDay, btnTabWeek, btnTabMonth, btnTabYear};
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";

        btnTabDay.setStyle(currentTab.equals("Day") ? activeStyle : inactiveStyle);
        btnTabWeek.setStyle(currentTab.equals("Week") ? activeStyle : inactiveStyle);
        btnTabMonth.setStyle(currentTab.equals("Month") ? activeStyle : inactiveStyle);
        btnTabYear.setStyle(currentTab.equals("Year") ? activeStyle : inactiveStyle);
    }

    private void updateSubTabStyles() {
        String activeStyle = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";

        btnSubHeartRate.setStyle(currentSubTab.equals("HeartRate") ? activeStyle : inactiveStyle);
        btnSubHRV.setStyle(currentSubTab.equals("HRV") ? activeStyle : inactiveStyle);
    }

    @FXML
    private void handleSelectDayTab() {
        currentTab = "Day";
        updateTabStyles();
        loadProfileData();
    }

    @FXML
    private void handleSelectWeekTab() {
        currentTab = "Week";
        updateTabStyles();
        loadProfileData();
    }

    @FXML
    private void handleSelectMonthTab() {
        currentTab = "Month";
        updateTabStyles();
        loadProfileData();
    }

    @FXML
    private void handleSelectYearTab() {
        currentTab = "Year";
        updateTabStyles();
        loadProfileData();
    }

    @FXML
    private void handleSelectSubHeartRate() {
        currentSubTab = "HeartRate";
        updateSubTabStyles();
        loadProfileData();
    }

    @FXML
    private void handleSelectSubHRV() {
        currentSubTab = "HRV";
        updateSubTabStyles();
        loadProfileData();
    }

    @FXML
    private void handleClose() {
        if (parentController != null && rootNode != null) {
            parentController.shrinkDetailView(rootNode);
        } else {
            Stage stage = (Stage) btnTabDay.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    @FXML
    private void handleLogPulse() {
        TextInputDialog dialog = new TextInputDialog(patientProfile.heartRate);
        dialog.setTitle("Log Heart Rate");
        dialog.setHeaderText("Record new resting heart rate (BPM)");
        dialog.setContentText("Enter current heart rate:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newHR = Integer.parseInt(result.get().trim());
                if (newHR > 30 && newHR < 250) {
                    patientProfile.heartRate = String.valueOf(newHR);

                    // Update Min/Max Range dynamically
                    if (patientProfile.heartRateMinMaxRange != null && patientProfile.heartRateMinMaxRange.contains("-")) {
                        String[] parts = patientProfile.heartRateMinMaxRange.split("-");
                        int min = Integer.parseInt(parts[0]);
                        int max = Integer.parseInt(parts[1]);
                        if (newHR < min) min = newHR;
                        if (newHR > max) max = newHR;
                        patientProfile.heartRateMinMaxRange = min + "-" + max;
                    } else {
                        patientProfile.heartRateMinMaxRange = (newHR - 10) + "-" + (newHR + 20);
                    }

                    // Append to sliding history window
                    if (patientProfile.heartRateHistory == null || patientProfile.heartRateHistory.isEmpty()) {
                        patientProfile.heartRateHistory = String.valueOf(newHR);
                    } else {
                        String[] parts = patientProfile.heartRateHistory.split(",");
                        StringBuilder sb = new StringBuilder();
                        int startIdx = parts.length >= 10 ? 1 : 0;
                        for (int i = startIdx; i < parts.length; i++) {
                            sb.append(parts[i]).append(",");
                        }
                        sb.append(newHR);
                        patientProfile.heartRateHistory = sb.toString();
                    }

                    // Save and refresh
                    MockDatabase.saveDatabase();
                    loadProfileData();

                    if (parentController != null) {
                        parentController.initializeSession(activePatient);
                    }
                } else {
                    showAlert("Invalid Value", "Please enter a realistic heart rate between 30 and 250 BPM.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid whole number.");
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
