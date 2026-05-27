package com.mycompany.projectbuang;

import javafx.collections.FXCollections;
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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StayFitWizardController {

    @FXML private VBox paneStep1, paneStep2, paneStep3, paneStep4, paneStep5;
    @FXML private Slider sliderHeight, sliderWeight, sliderRate, sliderGoalWeight;
    @FXML private Label lblHeightVal, lblWeightVal, lblBMIResultVal, lblBMICategoryBadge;
    @FXML private Label lblRateVal, lblGoalWeightVal, lblPlanCreationDate, lblSummaryLoss, lblSummaryDays;
    @FXML private ComboBox<String> comboBodyShape, comboExperience;
    @FXML private ComboBox<String> comboReminderTime;
    @FXML private Button btnStep1Next;
    @FXML private Circle circleBMIMarker;
    
    // Focus Areas Buttons
    @FXML private Button btnFocusAbs, btnFocusLegs, btnFocusGlutes, btnFocusArms, btnFocusBack, btnFocusChest, btnFocusWholeBody;
    
    // Training Day Buttons
    @FXML private Button btnMon, btnTue, btnWed, btnThu, btnFri, btnSat, btnSun;
    
    // Summary Canvas
    @FXML private LineChart<String, Number> chartTrend;
    @FXML private VBox vboxScaleDetailsList;

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
        this.rootNode = rootNode;
    }

    // Track selected focus areas
    private final List<String> selectedFocus = new ArrayList<>();
    // Track selected training days
    private final List<String> selectedDays = new ArrayList<>();

    @FXML
    public void initialize() {
        // Step 1 setups
        comboBodyShape.setItems(FXCollections.observableArrayList("(None)", "Hourglass", "Rectangle", "Inverted Triangle", "Oval"));
        comboBodyShape.setValue("(None)");
        
        comboExperience.setItems(FXCollections.observableArrayList("No", "Beginner", "Intermediate", "Advanced"));
        comboExperience.setValue("No");
        
        comboReminderTime.setItems(FXCollections.observableArrayList("07:00", "08:00", "12:00", "17:00", "18:00", "18:30", "19:00", "20:00", "21:00"));
        comboReminderTime.setValue("18:30");



        // Sliders change listeners
        sliderHeight.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblHeightVal.setText(String.format("%.0f cm", newVal.doubleValue()));
            recalculateBMI();
        });

        sliderWeight.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblWeightVal.setText(String.format("%.0f kg", newVal.doubleValue()));
            // Also adjust goal weight slider bounds based on new weight
            sliderGoalWeight.setMax(newVal.doubleValue() + 20.0);
            sliderGoalWeight.setMin(newVal.doubleValue() - 40.0);
            recalculateBMI();
        });

        sliderRate.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblRateVal.setText(String.format("Recommended: Lose %.1f kg every week", newVal.doubleValue()));
        });

        sliderGoalWeight.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblGoalWeightVal.setText(String.format("%.1f kg", newVal.doubleValue()));
        });
        
        // Initialize default focus selection
        selectedFocus.add("Whole body");
        // Initialize default days
        selectedDays.add("Mon");
        selectedDays.add("Wed");
        selectedDays.add("Thu");
        selectedDays.add("Sat");
    }

    public void setSessionContext(MockDatabase.UserAccount account, PatientDashboardController parent) {
        this.activePatient = account;
        this.parentController = parent;
        this.patientProfile = MockDatabase.patientDatabase.get(account.fullName);

        if (patientProfile != null) {
            // Load existing stats
            double h = parseDoubleSafe(patientProfile.height, 160.0);
            double w = parseDoubleSafe(patientProfile.weight, 60.0);
            sliderHeight.setValue(h);
            sliderWeight.setValue(w);
            
            double goal = parseDoubleSafe(patientProfile.fitPlanGoal, w - 1.5);
            sliderGoalWeight.setValue(goal);
            
            // Load focus areas
            if (patientProfile.fitPlanFocus != null && !patientProfile.fitPlanFocus.isEmpty()) {
                selectedFocus.clear();
                String[] areas = patientProfile.fitPlanFocus.split(", ");
                for (String a : areas) {
                    selectedFocus.add(a);
                }
                updateFocusButtonsUI();
            }
            
            // Load days
            if (patientProfile.fitPlanDays != null && !patientProfile.fitPlanDays.isEmpty()) {
                selectedDays.clear();
                String[] days = patientProfile.fitPlanDays.split(", ");
                for (String d : days) {
                    selectedDays.add(d);
                }
                updateDaysButtonsUI();
            }
            
            if (patientProfile.fitPlanTime != null && !patientProfile.fitPlanTime.isEmpty()) {
                comboReminderTime.setValue(patientProfile.fitPlanTime);
            }
        }
        recalculateBMI();
    }

    private void recalculateBMI() {
        double weight = sliderWeight.getValue();
        double height = sliderHeight.getValue() / 100.0;
        if (height <= 0) return;
        double bmi = weight / (height * height);
        
        lblBMIResultVal.setText(String.format("%.1f", bmi));
        
        // Category styling
        if (bmi < 18.5) {
            lblBMICategoryBadge.setText("Underweight");
            lblBMICategoryBadge.setStyle("-fx-text-fill: #0A84FF; -fx-font-size: 12px; -fx-font-weight: 700;");
        } else if (bmi < 25.0) {
            lblBMICategoryBadge.setText("Normal");
            lblBMICategoryBadge.setStyle("-fx-text-fill: #34C759; -fx-font-size: 12px; -fx-font-weight: 700;");
        } else if (bmi < 30.0) {
            lblBMICategoryBadge.setText("Overweight");
            lblBMICategoryBadge.setStyle("-fx-text-fill: #FF9500; -fx-font-size: 12px; -fx-font-weight: 700;");
        } else {
            lblBMICategoryBadge.setText("Obese");
            lblBMICategoryBadge.setStyle("-fx-text-fill: #FF3B30; -fx-font-size: 12px; -fx-font-weight: 700;");
        }

        // Translate circle marker: scale BMI from 10 to 40
        double minBmi = 10.0;
        double maxBmi = 40.0;
        double pct = (bmi - minBmi) / (maxBmi - minBmi);
        pct = Math.max(0.0, Math.min(1.0, pct)); // Clamp
        
        // Total track width inside Page 1 card is roughly 400px
        double trackWidth = 400.0;
        circleBMIMarker.setLayoutX(16 + pct * trackWidth);
    }

    private double parseDoubleSafe(String val, double def) {
        if (val == null) return def;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    // Step 1 navigation
    @FXML
    private void handleStep1Next() {
        paneStep1.setVisible(false);
        paneStep2.setVisible(true);
    }

    // Step 2 navigation
    @FXML
    private void handleBackTo1() {
        paneStep2.setVisible(false);
        paneStep1.setVisible(true);
    }

    @FXML
    private void handleStep2Next() {
        paneStep2.setVisible(false);
        paneStep3.setVisible(true);
    }

    // Step 3 Focus areas toggling
    @FXML private void handleToggleAbs() { toggleFocus("Abs", btnFocusAbs); }
    @FXML private void handleToggleLegs() { toggleFocus("Legs", btnFocusLegs); }
    @FXML private void handleToggleGlutes() { toggleFocus("Glutes", btnFocusGlutes); }
    @FXML private void handleToggleArms() { toggleFocus("Arms", btnFocusArms); }
    @FXML private void handleToggleBack() { toggleFocus("Back", btnFocusBack); }
    @FXML private void handleToggleChest() { toggleFocus("Chest", btnFocusChest); }
    @FXML private void handleToggleWholeBody() { toggleFocus("Whole body", btnFocusWholeBody); }

    private void toggleFocus(String area, Button btn) {
        if (selectedFocus.contains(area)) {
            selectedFocus.remove(area);
            btn.setStyle("-fx-background-color: #EEEEF0; -fx-text-fill: #1D1D1F; -fx-background-radius: 12; -fx-font-weight: 600;");
            btn.setText(area + " ⚪");
        } else {
            selectedFocus.add(area);
            btn.setStyle("-fx-background-color: #FF9500; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: 600;");
            btn.setText(area + " 🟠");
        }
    }

    private void updateFocusButtonsUI() {
        updateFocusBtnStyle(btnFocusAbs, "Abs");
        updateFocusBtnStyle(btnFocusLegs, "Legs");
        updateFocusBtnStyle(btnFocusGlutes, "Glutes");
        updateFocusBtnStyle(btnFocusArms, "Arms");
        updateFocusBtnStyle(btnFocusBack, "Back");
        updateFocusBtnStyle(btnFocusChest, "Chest");
        updateFocusBtnStyle(btnFocusWholeBody, "Whole body");
    }

    private void updateFocusBtnStyle(Button btn, String area) {
        if (selectedFocus.contains(area)) {
            btn.setStyle("-fx-background-color: #FF9500; -fx-text-fill: white; -fx-background-radius: 12; -fx-font-weight: 600;");
            btn.setText(area + " 🟠");
        } else {
            btn.setStyle("-fx-background-color: #EEEEF0; -fx-text-fill: #1D1D1F; -fx-background-radius: 12; -fx-font-weight: 600;");
            btn.setText(area + " ⚪");
        }
    }

    @FXML
    private void handleBackTo2() {
        paneStep3.setVisible(false);
        paneStep2.setVisible(true);
    }

    @FXML
    private void handleStep3Next() {
        paneStep3.setVisible(false);
        paneStep4.setVisible(true);
    }

    // Step 4 Training Days toggling
    @FXML private void handleToggleMon() { toggleDay("Mon", btnMon); }
    @FXML private void handleToggleTue() { toggleDay("Tue", btnTue); }
    @FXML private void handleToggleWed() { toggleDay("Wed", btnWed); }
    @FXML private void handleToggleThu() { toggleDay("Thu", btnThu); }
    @FXML private void handleToggleFri() { toggleDay("Fri", btnFri); }
    @FXML private void handleToggleSat() { toggleDay("Sat", btnSat); }
    @FXML private void handleToggleSun() { toggleDay("Sun", btnSun); }

    private void toggleDay(String day, Button btn) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day);
            btn.setStyle("-fx-background-color: #EEEEF0; -fx-text-fill: #1D1D1F; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            selectedDays.add(day);
            btn.setStyle("-fx-background-color: #FF9500; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
    }

    private void updateDaysButtonsUI() {
        updateDayBtnStyle(btnMon, "Mon");
        updateDayBtnStyle(btnTue, "Tue");
        updateDayBtnStyle(btnWed, "Wed");
        updateDayBtnStyle(btnThu, "Thu");
        updateDayBtnStyle(btnFri, "Fri");
        updateDayBtnStyle(btnSat, "Sat");
        updateDayBtnStyle(btnSun, "Sun");
    }

    private void updateDayBtnStyle(Button btn, String day) {
        if (selectedDays.contains(day)) {
            btn.setStyle("-fx-background-color: #FF9500; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            btn.setStyle("-fx-background-color: #EEEEF0; -fx-text-fill: #1D1D1F; -fx-background-radius: 8; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
    }

    @FXML
    private void handleBackTo3() {
        paneStep4.setVisible(false);
        paneStep3.setVisible(true);
    }

    @FXML
    private void handleStep4Next() {
        paneStep4.setVisible(false);
        paneStep5.setVisible(true);
        generateSummaryPlan();
    }

    // Step 5 Result & Draw Curve
    private void generateSummaryPlan() {
        double currentW = sliderWeight.getValue();
        double goalW = sliderGoalWeight.getValue();
        double loss = Math.max(0.0, currentW - goalW);
        
        lblSummaryLoss.setText(String.format("%.1f kg", loss));
        lblPlanCreationDate.setText("Created on " + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")));
        
        // Calculate weeks: e.g. lose rate = 0.4 kg/week
        double rate = sliderRate.getValue();
        double weeks = rate > 0 ? (loss / rate) : 4.0;
        int totalDays = (int) Math.round(weeks * 7.0);
        lblSummaryDays.setText(totalDays + " days");

        drawCurveChart(currentW, goalW);
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

    private void drawCurveChart(double startW, double endW) {
        Platform.runLater(() -> {
            chartTrend.setAnimated(false);
            CategoryAxis xAxis = (CategoryAxis) chartTrend.getXAxis();
            xAxis.setAnimated(false);
            
            NumberAxis yAxis = (NumberAxis) chartTrend.getYAxis();
            yAxis.setAnimated(false);
            
            // Limit weight Y-axis scaling to focus on the data range
            double minW = Math.min(startW, endW);
            double maxW = Math.max(startW, endW);
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

            String[] stages = {"Base", "Improv.", "Strength.", "Consol.", "Stable"};
            xAxis.getCategories().setAll(stages);
            chartTrend.getData().clear();
            
            XYChart.Series<String, Number> projectedSeries = new XYChart.Series<>();
            projectedSeries.setName("Projected Target");
            
            XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
            actualSeries.setName("Actual");
            
            projectedSeries.getData().add(new XYChart.Data<>(stages[0], startW));
            projectedSeries.getData().add(new XYChart.Data<>(stages[1], startW - (startW - endW) * 0.25));
            projectedSeries.getData().add(new XYChart.Data<>(stages[2], startW - (startW - endW) * 0.60));
            projectedSeries.getData().add(new XYChart.Data<>(stages[3], startW - (startW - endW) * 0.85));
            projectedSeries.getData().add(new XYChart.Data<>(stages[4], endW));

            actualSeries.getData().add(new XYChart.Data<>(stages[0], startW));
            actualSeries.getData().add(new XYChart.Data<>(stages[1], startW - (startW - endW) * 0.20));
            actualSeries.getData().add(new XYChart.Data<>(stages[2], startW - (startW - endW) * 0.55));

            if (vboxScaleDetailsList != null) {
                vboxScaleDetailsList.getChildren().clear();
                double[] projectedW = {
                    startW,
                    startW - (startW - endW) * 0.25,
                    startW - (startW - endW) * 0.60,
                    startW - (startW - endW) * 0.85,
                    endW
                };
                double[] actualW = {
                    startW,
                    startW - (startW - endW) * 0.20,
                    startW - (startW - endW) * 0.55
                };
                
                for (int i = 0; i < stages.length; i++) {
                    String actStr = (i < actualW.length) ? String.format("%.1f kg", actualW[i]) : "--";
                    vboxScaleDetailsList.getChildren().add(createDetailRow(stages[i] + " Week", String.format("Proj: %.1f kg | Act: %s", projectedW[i], actStr), "#FF9500"));
                }
            }

            chartTrend.getData().addAll(projectedSeries, actualSeries);

            Platform.runLater(() -> {
                if (projectedSeries.getNode() != null) {
                    Node projectedLine = projectedSeries.getNode().lookup(".chart-series-line");
                    if (projectedLine != null) {
                        projectedLine.setStyle("-fx-stroke: #FF9500; -fx-stroke-width: 2.0; -fx-stroke-dash-array: 5 5;");
                    }
                    for (XYChart.Data<String, Number> data : projectedSeries.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-background-color: #FF9500, white; -fx-background-radius: 4px; -fx-padding: 4px;");
                        }
                    }
                }
                if (actualSeries.getNode() != null) {
                    Node actualLine = actualSeries.getNode().lookup(".chart-series-line");
                    if (actualLine != null) {
                        actualLine.setStyle("-fx-stroke: #FFCC00; -fx-stroke-width: 3.0;");
                    }
                    for (XYChart.Data<String, Number> data : actualSeries.getData()) {
                        if (data.getNode() != null) {
                            data.getNode().setStyle("-fx-background-color: #FFCC00, white; -fx-background-radius: 4px; -fx-padding: 4px;");
                        }
                    }
                }
            });
        });
    }

    @FXML
    private void handleBackTo4() {
        paneStep5.setVisible(false);
        paneStep4.setVisible(true);
    }

    @FXML
    private void handleFinishWizard() {
        if (patientProfile != null) {
            // Save updated stats back to patient profile
            patientProfile.height = String.format("%.0f", sliderHeight.getValue());
            patientProfile.weight = String.format("%.0f", sliderWeight.getValue());
            patientProfile.bmi = lblBMIResultVal.getText();
            
            patientProfile.fitPlanGoal = String.format("%.1f", sliderGoalWeight.getValue());
            
            // Join selected focus areas
            if (selectedFocus.isEmpty()) {
                patientProfile.fitPlanFocus = "Whole body";
            } else {
                patientProfile.fitPlanFocus = String.join(", ", selectedFocus);
            }
            
            // Join selected days
            if (selectedDays.isEmpty()) {
                patientProfile.fitPlanDays = "Mon, Wed, Fri";
            } else {
                patientProfile.fitPlanDays = String.join(", ", selectedDays);
            }
            
            patientProfile.fitPlanTime = comboReminderTime.getValue();
            
            // Add a log in notes
            patientProfile.clinicalNotes.add("[SYSTEM NOTE]: Stay Fit Plan Activated. Focus: " + 
                patientProfile.fitPlanFocus + " | Target weight: " + patientProfile.fitPlanGoal + " kg. Plan initialized successfully.");
            
            // Save database changes dynamically
            MockDatabase.saveDatabase();
            
            // Refresh parent view
            if (parentController != null) {
                parentController.initializeSession(activePatient);
            }
        }
        
        if (parentController != null && rootNode != null) {
            parentController.shrinkDetailView(rootNode);
        } else {
            // Close modal stage
            Stage stage = (Stage) btnStep1Next.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }

    @FXML
    private void handleClose() {
        if (parentController != null && rootNode != null) {
            parentController.shrinkDetailView(rootNode);
        } else {
            Stage stage = (Stage) btnStep1Next.getScene().getWindow();
            if (stage != null) stage.close();
        }
    }
}
