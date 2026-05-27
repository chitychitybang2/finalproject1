package com.mycompany.projectbuang;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Optional;

public class ActivityDetailsController {

    @FXML private Button btnTabDay, btnTabWeek, btnTabMonth, btnTabYear;
    @FXML private Label lblSelectedDate, lblTimeInterval, lblCurrentSteps;
    @FXML private javafx.scene.layout.Pane paneDay, paneWeek;
    @FXML private VBox vboxScaleDetailsListDay;
    @FXML private VBox vboxScaleDetailsListWeek;
    
    // Day View Nodes
    @FXML private BarChart<String, Number> chartDaySteps;
    @FXML private Label lblStepsGoalFraction;
    @FXML private Rectangle rectProgressBar;
    @FXML private Label lblStepsAway;
    @FXML private Label lblCaloriesVal, lblCaloriesEquiv;
    @FXML private Label lblDistanceVal;
    
    // Week View Nodes
    @FXML private Label lblChartRange;
    @FXML private BarChart<String, Number> chartWeeklySteps;
    @FXML private Label lblAverageSteps;

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
        this.rootNode = rootNode;
    }

    @FXML
    public void initialize() {
        showPane(paneDay);
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

        // Current Steps
        int steps = parseIntSafe(patientProfile.stepCount, 5241);
        int goal = parseIntSafe(patientProfile.stepGoal, 10000);
        lblCurrentSteps.setText(String.format("%,d", steps));

        // Format Steps Goal
        lblStepsGoalFraction.setText(String.format("%,d / %,d steps", steps, goal));

        // Steps Away
        int away = goal - steps;
        if (away > 0) {
            lblStepsAway.setText(String.format("%,d steps away", away));
        } else {
            lblStepsAway.setText("Goal achieved! 🎉");
        }

        // Progress bar (track width is 200px)
        double progress = goal > 0 ? (double) steps / goal : 1.0;
        rectProgressBar.setWidth(Math.max(0.0, Math.min(1.0, progress)) * 200.0);

        // Calories & Distance calculations
        double cals = steps * 0.03; // ~0.03 kcal per step
        double dist = steps * 0.00075; // ~0.75m or 0.00075km per step
        lblCaloriesVal.setText(String.format("%.0f kcal", cals));
        
        // Calories Equivalents
        if (cals < 150) {
            lblCaloriesEquiv.setText("Equivalent to 1 apple");
        } else if (cals < 300) {
            lblCaloriesEquiv.setText("Equivalent to 1 soft serve");
        } else {
            lblCaloriesEquiv.setText("Equivalent to 1 slice of pizza");
        }

        lblDistanceVal.setText(String.format("%.2f km", dist));

        // Draw Canvas Charts
        drawDayStepsChart();
        drawWeeklyStepsChart(goal);
    }

    // Swapping panels
    @FXML
    private void handleSelectDayTab() {
        showPane(paneDay);
        String active = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";
        btnTabDay.setStyle(active);
        btnTabWeek.setStyle(inactive);
        btnTabMonth.setStyle(inactive);
        btnTabYear.setStyle(inactive);
    }

    @FXML
    private void handleSelectWeekTab() {
        showPane(paneWeek);
        lblChartRange.setText("May 24 – 30");
        String active = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";
        btnTabWeek.setStyle(active);
        btnTabDay.setStyle(inactive);
        btnTabMonth.setStyle(inactive);
        btnTabYear.setStyle(inactive);
        
        int goal = parseIntSafe(patientProfile.stepGoal, 10000);
        drawWeeklyStepsChart(goal);
    }

    @FXML
    private void handleSelectMonthTab() {
        showPane(paneWeek);
        lblChartRange.setText("May 2026");
        String active = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";
        btnTabMonth.setStyle(active);
        btnTabDay.setStyle(inactive);
        btnTabWeek.setStyle(inactive);
        btnTabYear.setStyle(inactive);
        
        int goal = parseIntSafe(patientProfile.stepGoal, 10000);
        drawWeeklyStepsChart(goal);
    }

    @FXML
    private void handleSelectYearTab() {
        showPane(paneWeek);
        lblChartRange.setText("Year 2026");
        String active = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";
        btnTabYear.setStyle(active);
        btnTabDay.setStyle(inactive);
        btnTabWeek.setStyle(inactive);
        btnTabMonth.setStyle(inactive);
        
        int goal = parseIntSafe(patientProfile.stepGoal, 10000);
        drawWeeklyStepsChart(goal);
    }

    private void showPane(javafx.scene.layout.Pane target) {
        paneDay.setVisible(target == paneDay);
        paneDay.setManaged(target == paneDay);
        paneWeek.setVisible(target == paneWeek);
        paneWeek.setManaged(target == paneWeek);
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

    // Day Steps Intraday Chart Drawing
    private void drawDayStepsChart() {
        chartDaySteps.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Steps");
        
        double[] mockSteps = {0, 0, 100, 300, 800, 1200, 400, 700, 1500, 200, 41, 0};
        String[] hours = {"00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"};
        
        if (vboxScaleDetailsListDay != null) {
            vboxScaleDetailsListDay.getChildren().clear();
            for (int i = 0; i < mockSteps.length; i++) {
                series.getData().add(new XYChart.Data<>(hours[i], mockSteps[i]));
                if (mockSteps[i] > 0) {
                    vboxScaleDetailsListDay.getChildren().add(createDetailRow(hours[i] + " (Intraday Steps)", String.format("%,d steps", (int)mockSteps[i]), "#34C759"));
                }
            }
            if (vboxScaleDetailsListDay.getChildren().isEmpty()) {
                vboxScaleDetailsListDay.getChildren().add(new Label("No step data recorded today."));
            }
        } else {
            for (int i = 0; i < mockSteps.length; i++) {
                series.getData().add(new XYChart.Data<>(hours[i], mockSteps[i]));
            }
        }
        
        chartDaySteps.getData().add(series);
        
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #34C759;");
                }
            }
        });
    }

    // Weekly Step Bar Chart Drawing
    private void drawWeeklyStepsChart(int stepGoal) {
        chartWeeklySteps.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Steps");
        
        String hist = patientProfile.stepHistory != null ? patientProfile.stepHistory : "5000";
        String[] logs = hist.split(",");
        int numDays = logs.length;

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int totalWeekSteps = 0;

        if (vboxScaleDetailsListWeek != null) {
            vboxScaleDetailsListWeek.getChildren().clear();
        }

        for (int i = 0; i < 7; i++) {
            int stepsVal = 0;
            if (i < numDays) {
                stepsVal = parseIntSafe(logs[i], 0);
            }
            totalWeekSteps += stepsVal;
            series.getData().add(new XYChart.Data<>(days[i], stepsVal));
            
            if (vboxScaleDetailsListWeek != null) {
                String color = stepsVal >= stepGoal ? "#34C759" : "#FF9500";
                vboxScaleDetailsListWeek.getChildren().add(createDetailRow(days[i] + " (Daily Log)", String.format("%,d steps", stepsVal), color));
            }
        }

        chartWeeklySteps.getData().add(series);

        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    int stepsVal = data.getYValue().intValue();
                    if (stepsVal >= stepGoal) {
                        data.getNode().setStyle("-fx-bar-fill: #34C759;");
                    } else {
                        data.getNode().setStyle("-fx-bar-fill: #FF9500;");
                    }
                }
            }
        });

        int avg = totalWeekSteps / Math.max(1, numDays);
        lblAverageSteps.setText(String.format("%,d steps/day", avg));
    }

    // Action handlers
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
    private void handleLogSteps() {
        TextInputDialog dialog = new TextInputDialog("2000");
        dialog.setTitle("Log Steps Walked");
        dialog.setHeaderText("Add steps from your walk or run activity");
        dialog.setContentText("Enter number of steps:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int stepsAdded = Integer.parseInt(result.get().trim());
                if (stepsAdded > 0) {
                    int oldSteps = parseIntSafe(patientProfile.stepCount, 5241);
                    int newSteps = oldSteps + stepsAdded;
                    patientProfile.stepCount = String.valueOf(newSteps);

                    // Map steps to Move calories: add logged steps * 0.03
                    double calBurned = stepsAdded * 0.03;
                    int oldMove = parseIntSafe(patientProfile.moveCal, 0);
                    patientProfile.moveCal = String.valueOf(oldMove + (int) Math.round(calBurned));

                    // Append steps to step history sliding window
                    String hist = patientProfile.stepHistory != null ? patientProfile.stepHistory : "5000";
                    String[] parts = hist.split(",");
                    StringBuilder sb = new StringBuilder();
                    int startIdx = parts.length >= 7 ? 1 : 0;
                    for (int i = startIdx; i < parts.length - 1; i++) {
                        sb.append(parts[i]).append(",");
                    }
                    if (parts.length >= 7) {
                        sb.append(parts[parts.length - 1]).append(",");
                    }
                    sb.append(newSteps);
                    patientProfile.stepHistory = sb.toString();

                    // Log activity
                    patientProfile.clinicalNotes.add("[USER NOTE]: Logged " + stepsAdded + " steps. Calories burnt: +" + (int) Math.round(calBurned) + " kcal.");

                    // Sync and reload
                    MockDatabase.saveDatabase();
                    loadProfileData();

                    if (parentController != null) {
                        parentController.initializeSession(activePatient);
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid number of steps.");
            }
        }
    }

    @FXML
    private void handleEditStepsGoal() {
        TextInputDialog dialog = new TextInputDialog(patientProfile.stepGoal);
        dialog.setTitle("Edit Step Goal");
        dialog.setHeaderText("Set your daily goal target steps");
        dialog.setContentText("Enter step goal target:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int newGoal = Integer.parseInt(result.get().trim());
                if (newGoal > 0) {
                    patientProfile.stepGoal = String.valueOf(newGoal);
                    
                    // Sync and reload
                    MockDatabase.saveDatabase();
                    loadProfileData();

                    if (parentController != null) {
                        parentController.initializeSession(activePatient);
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid numeric goal.");
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

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private double parseDoubleSafe(String val, double defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
