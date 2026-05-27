package com.mycompany.projectbuang;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WeightDetailsController {

    @FXML private Button btnTabDay, btnTabWeek, btnTabMonth, btnTabYear;
    @FXML private Label lblCurrentWeight, lblWeightCompareSub;
    @FXML private javafx.scene.layout.Pane paneDay, paneWeek;
    
    // Day View Nodes
    @FXML private Label lblGoalDaysLeft, lblStartWeightGoal, lblEndWeightGoal;
    @FXML private Rectangle rectProgressBar;
    
    // Week View Nodes
    @FXML private Label lblChartRange;
    @FXML private LineChart<String, Number> chartWeight;
    @FXML private Label lblOverviewWeight, lblOverviewFat, lblOverviewMuscle;
    @FXML private VBox vboxScaleDetailsList;
    
    // Stay Fit Nodes
    @FXML private Canvas canvasCalorieGauge;
    @FXML private Label lblDeficitVal, lblTotalBurnt, lblTotalConsumed;
    @FXML private Label lblCalBreakfast, lblCalLunch, lblCalDinner;

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
        this.rootNode = rootNode;
    }
    
    // In-memory meal calories
    private int caloriesBreakfast = 0;
    private int caloriesLunch = 0;
    private int caloriesDinner = 0;
    private int caloriesExtra = 0;

    @FXML
    public void initialize() {
        // Run initial configuration
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

        // Current weight labels
        lblCurrentWeight.setText(patientProfile.weight != null ? patientProfile.weight : "0");
        lblOverviewWeight.setText((patientProfile.weight != null ? patientProfile.weight : "0") + " kg");

        // Set start and goal weights
        double currentW = parseDoubleSafe(patientProfile.weight, 60.0);
        double goalW = parseDoubleSafe(patientProfile.fitPlanGoal, currentW - 1.5);
        double startW = goalW + 1.5; // Assume start weight is goal + 1.5 for goal tracking visual

        lblStartWeightGoal.setText(String.format("%.1f kg", startW));
        lblEndWeightGoal.setText(String.format("%.1f kg", goalW));

        // Comparison sub label
        double comp = currentW - startW;
        if (comp < 0) {
            lblWeightCompareSub.setText(String.format("%.1f kg down from start weight", Math.abs(comp)));
            lblWeightCompareSub.setStyle("-fx-text-fill: #34C759; -fx-font-size: 13px;");
        } else if (comp > 0) {
            lblWeightCompareSub.setText(String.format("%.1f kg up from start weight", comp));
            lblWeightCompareSub.setStyle("-fx-text-fill: #FF9500; -fx-font-size: 13px;");
        } else {
            lblWeightCompareSub.setText("Same as start weight");
            lblWeightCompareSub.setStyle("-fx-text-fill: #8E8E93; -fx-font-size: 13px;");
        }

        // Update Lose Weight progress bar width
        double progressPct = 0.0;
        double diffTotal = startW - goalW;
        if (diffTotal > 0) {
            double lost = startW - currentW;
            progressPct = Math.max(0.0, Math.min(1.0, lost / diffTotal));
        } else {
            progressPct = 1.0; // Goal completed
        }
        
        // Progress bar track width is 200px
        rectProgressBar.setWidth(progressPct * 200.0);

        // Populate dynamic days left (mock value from fitPlanGoal configuration or default 27)
        lblGoalDaysLeft.setText("Days left: 27 • Manage");

        // Load Meal Calorie Map
        parseMealsMap(patientProfile.mealsCalorieMap);
        
        // Display meal labels
        lblCalBreakfast.setText(caloriesBreakfast + " kcal");
        lblCalLunch.setText(caloriesLunch + " kcal");
        lblCalDinner.setText(caloriesDinner + " kcal");

        // Burnt / Deficit values
        int burnt = parseIntSafe(patientProfile.calorieBurnt, 156);
        int consumed = caloriesBreakfast + caloriesLunch + caloriesDinner + caloriesExtra;
        int deficit = burnt - consumed;

        lblTotalBurnt.setText(burnt + " / 397 kcal");
        lblTotalConsumed.setText(consumed + " kcal");
        lblDeficitVal.setText(String.valueOf(deficit));
        
        patientProfile.calorieConsumed = String.valueOf(consumed);

        // Draw Canvas Chart and Calorie Gauge
        drawCalorieGauge(burnt, consumed, deficit);
        drawWeightChart(goalW);
    }

    private void parseMealsMap(String mapStr) {
        // Format: "breakfast=100;lunch=200;dinner=0;extra=0"
        caloriesBreakfast = 0;
        caloriesLunch = 0;
        caloriesDinner = 0;
        caloriesExtra = 0;
        
        if (mapStr == null || mapStr.trim().isEmpty()) return;
        
        try {
            String[] tokens = mapStr.split(";");
            for (String t : tokens) {
                String[] pair = t.split("=");
                if (pair.length == 2) {
                    String meal = pair[0].toLowerCase();
                    int cals = Integer.parseInt(pair[1]);
                    if (meal.equals("breakfast")) caloriesBreakfast = cals;
                    else if (meal.equals("lunch")) caloriesLunch = cals;
                    else if (meal.equals("dinner")) caloriesDinner = cals;
                    else if (meal.equals("extra")) caloriesExtra = cals;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String serializeMealsMap() {
        return "breakfast=" + caloriesBreakfast + ";lunch=" + caloriesLunch + ";dinner=" + caloriesDinner + ";extra=" + caloriesExtra;
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
        lblChartRange.setText("24–30 May");
        String active = "-fx-background-color: white; -fx-text-fill: #1D1D1F; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);";
        String inactive = "-fx-background-color: transparent; -fx-text-fill: #8E8E93; -fx-background-radius: 9; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 6 0 6 0;";
        btnTabWeek.setStyle(active);
        btnTabDay.setStyle(inactive);
        btnTabMonth.setStyle(inactive);
        btnTabYear.setStyle(inactive);
        
        double currentW = parseDoubleSafe(patientProfile.weight, 60.0);
        double goalW = parseDoubleSafe(patientProfile.fitPlanGoal, currentW - 1.5);
        drawWeightChart(goalW);
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
        
        double currentW = parseDoubleSafe(patientProfile.weight, 60.0);
        double goalW = parseDoubleSafe(patientProfile.fitPlanGoal, currentW - 1.5);
        drawWeightChart(goalW);
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
        
        double currentW = parseDoubleSafe(patientProfile.weight, 60.0);
        double goalW = parseDoubleSafe(patientProfile.fitPlanGoal, currentW - 1.5);
        drawWeightChart(goalW);
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

    // Weekly Line Chart Drawing
    private void drawWeightChart(double targetW) {
        Platform.runLater(() -> {
            chartWeight.getData().clear();
            
            // Actual Weight trend
            XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
            actualSeries.setName("Weight");
            
            String history = patientProfile.weightHistory != null ? patientProfile.weightHistory : "60";
            String[] logs = history.split(",");
            int numPoints = logs.length;
            
            String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            
            if (vboxScaleDetailsList != null) {
                vboxScaleDetailsList.getChildren().clear();
            }

            double minW = targetW;
            double maxW = targetW;

            for (int i = 0; i < 7; i++) {
                double val = targetW + 1.5;
                if (i < numPoints) {
                    val = parseDoubleSafe(logs[i], targetW + 1.5);
                } else if (numPoints > 0) {
                    val = parseDoubleSafe(logs[numPoints - 1], targetW + 1.5);
                }
                
                if (val < minW) minW = val;
                if (val > maxW) maxW = val;
                
                actualSeries.getData().add(new XYChart.Data<>(days[i], val));
                if (vboxScaleDetailsList != null) {
                    vboxScaleDetailsList.getChildren().add(createDetailRow(days[i] + " (Weekly Progress)", String.format("Weight: %.1f kg | Target: %.1f kg", val, targetW), "#BF5AF2"));
                }
            }
            
            // Limit Y-axis bounds to focus on data range
            NumberAxis yAxis = (NumberAxis) chartWeight.getYAxis();
            yAxis.setAnimated(false);
            double lower = Math.floor(minW - 2.0);
            double upper = Math.ceil(maxW + 2.0);
            if (lower < 0) lower = 0;
            
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(lower);
            yAxis.setUpperBound(upper);
            double range = upper - lower;
            if (range <= 10) {
                yAxis.setTickUnit(1.0);
            } else if (range <= 20) {
                yAxis.setTickUnit(2.0);
            } else {
                yAxis.setTickUnit(5.0);
            }
            
            // Target Weight line
            XYChart.Series<String, Number> targetSeries = new XYChart.Series<>();
            targetSeries.setName("Target Goal");
            for (int i = 0; i < 7; i++) {
                targetSeries.getData().add(new XYChart.Data<>(days[i], targetW));
            }
            
            chartWeight.getData().addAll(actualSeries, targetSeries);
            
            Platform.runLater(() -> {
                if (targetSeries.getNode() != null) {
                    Node targetLine = targetSeries.getNode().lookup(".chart-series-line");
                    if (targetLine != null) {
                        targetLine.setStyle("-fx-stroke: #34C759; -fx-stroke-width: 2.0; -fx-stroke-dash-array: 5 5;");
                    }
                    for (XYChart.Data<String, Number> data : targetSeries.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-background-color: transparent, transparent; -fx-background-radius: 0; -fx-padding: 0;");
                        }
                    }
                }
                if (actualSeries.getNode() != null) {
                    Node actualLine = actualSeries.getNode().lookup(".chart-series-line");
                    if (actualLine != null) {
                        actualLine.setStyle("-fx-stroke: #BF5AF2; -fx-stroke-width: 2.5;");
                    }
                    for (XYChart.Data<String, Number> data : actualSeries.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-background-color: #BF5AF2, white; -fx-background-radius: 4px; -fx-padding: 4px;");
                        }
                    }
                }
            });
        });
    }

    // Calorie Deficit Gauge Drawing
    private void drawCalorieGauge(int burnt, int consumed, int deficit) {
        GraphicsContext gc = canvasCalorieGauge.getGraphicsContext2D();
        double w = canvasCalorieGauge.getWidth();
        double h = canvasCalorieGauge.getHeight();
        
        // Background
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);
        
        // Center coordinates
        double cx = w / 2.0;
        double cy = h - 20.0;
        double r = 110.0;
        
        // Draw Gauge Colored Arc (semi-circle starting from left 180 deg to right 0 deg)
        // Red (Surplus) -> Yellow -> Green (Deficit)
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineWidth(10.0);
        
        // Segment 1: Red/Orange (-800 to -300)
        gc.setStroke(Color.web("#FF3B30"));
        gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 180, -60, javafx.scene.shape.ArcType.OPEN);
        
        // Segment 2: Yellow (-300 to 200)
        gc.setStroke(Color.web("#FFCC00"));
        gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 120, -60, javafx.scene.shape.ArcType.OPEN);
        
        // Segment 3: Green/Aqua (200 to 800)
        gc.setStroke(Color.web("#34C759"));
        gc.strokeArc(cx - r, cy - r, r * 2, r * 2, 60, -60, javafx.scene.shape.ArcType.OPEN);

        // Draw Needle pointing to current deficit value (-800 to 800)
        double minD = -800.0;
        double maxD = 800.0;
        double clampedD = Math.max(minD, Math.min(maxD, (double) deficit));
        double pct = (clampedD - minD) / (maxD - minD);
        
        // Angle in radians (from left 180 degrees to right 0 degrees)
        double angleDeg = 180.0 - (pct * 180.0);
        double angleRad = Math.toRadians(angleDeg);
        
        double needleLen = r - 15.0;
        double nx = cx + needleLen * Math.cos(angleRad);
        double ny = cy - needleLen * Math.sin(angleRad);
        
        // Draw Needle Pin (Dark color for light mode visibility)
        gc.setFill(Color.web("#1D1D1F"));
        gc.fillOval(cx - 6, cy - 6, 12, 12);
        
        // Draw Needle Line
        gc.setStroke(Color.web("#1D1D1F"));
        gc.setLineWidth(3.0);
        gc.strokeLine(cx, cy, nx, ny);
        
        // Draw Tick labels
        gc.setFill(Color.web("#8E8E93"));
        gc.fillText("-800", cx - r - 25, cy + 5);
        gc.fillText("0", cx - 4, cy - r - 12);
        gc.fillText("800", cx + r + 10, cy + 5);
        gc.fillText("-400", cx - r/Math.sqrt(2) - 25, cy - r/Math.sqrt(2));
        gc.fillText("400", cx + r/Math.sqrt(2) + 10, cy - r/Math.sqrt(2));
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
    private void handleAddRecord() {
        TextInputDialog dialog = new TextInputDialog(patientProfile.weight);
        dialog.setTitle("Add Weight Record");
        dialog.setHeaderText("Log new weight entry (kg)");
        dialog.setContentText("Enter current weight:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double newW = Double.parseDouble(result.get().trim());
                if (newW < 20.0 || newW > 300.0) {
                    showAlert("Invalid Weight", "Please enter a weight between 20.0 kg and 300.0 kg.");
                } else {
                    patientProfile.weight = String.format("%.1f", newW);
                    
                    // Recalculate BMI
                    double h = parseDoubleSafe(patientProfile.height, 170.0) / 100.0;
                    if (h > 0) {
                        patientProfile.bmi = String.format("%.1f", newW / (h * h));
                    }
                    
                    // Update weight history
                    if (patientProfile.weightHistory == null || patientProfile.weightHistory.isEmpty()) {
                        patientProfile.weightHistory = patientProfile.weight;
                    } else {
                        // Keep a sliding window of last 7 entries
                        String[] parts = patientProfile.weightHistory.split(",");
                        StringBuilder sb = new StringBuilder();
                        int startIdx = parts.length >= 7 ? 1 : 0;
                        for (int i = startIdx; i < parts.length; i++) {
                            sb.append(parts[i]).append(",");
                        }
                        sb.append(patientProfile.weight);
                        patientProfile.weightHistory = sb.toString();
                    }
                    
                    // Clinical note log
                    patientProfile.clinicalNotes.add("[USER NOTE]: Weight logged: " + patientProfile.weight + " kg. New BMI: " + patientProfile.bmi);
                    
                    // Save and refresh
                    MockDatabase.saveDatabase();
                    loadProfileData();
                    
                    if (parentController != null) {
                        parentController.initializeSession(activePatient);
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid numeric weight.");
            }
        }
    }



    // Meal buttons logging
    @FXML private void handleLogBreakfast() { logMeal("breakfast"); }
    @FXML private void handleLogLunch() { logMeal("lunch"); }
    @FXML private void handleLogDinner() { logMeal("dinner"); }

    @FXML
    private void handleAddExtraMeal() {
        logMeal("extra");
    }

    private void logMeal(String mealName) {
        TextInputDialog dialog = new TextInputDialog("0");
        dialog.setTitle("Log Calories");
        dialog.setHeaderText("Add consumed calories for " + mealName.toUpperCase());
        dialog.setContentText("Enter calories (kcal):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int cals = Integer.parseInt(result.get().trim());
                if (cals < 0 || cals > 3000) {
                    showAlert("Invalid Input", "Calories must be between 0 and 3000 kcal.");
                    return;
                }
                
                if (mealName.equals("breakfast")) caloriesBreakfast += cals;
                else if (mealName.equals("lunch")) caloriesLunch += cals;
                else if (mealName.equals("dinner")) caloriesDinner += cals;
                else if (mealName.equals("extra")) caloriesExtra += cals;
                
                // Save meal maps
                patientProfile.mealsCalorieMap = serializeMealsMap();
                
                // Save database and reload
                MockDatabase.saveDatabase();
                loadProfileData();
                
                if (parentController != null) {
                    parentController.initializeSession(activePatient);
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Input", "Please enter a valid whole number for calories.");
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

    private double parseDoubleSafe(String val, double defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private int parseIntSafe(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
