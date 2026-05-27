package com.mycompany.projectbuang;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.util.StringConverter;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;

import java.util.Optional;

public class SleepDetailsController {

    @FXML private Button btnTabDay, btnTabWeek, btnTabMonth, btnTabYear;
    @FXML private Label lblDateSelector, lblSleepHoursVal, lblSleepMinutesVal;
    @FXML private LineChart<String, Number> chartSleepLine;
    @FXML private StackedBarChart<String, Number> chartSleepBar;
    @FXML private VBox vboxScaleDetailsList;

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
        this.rootNode = rootNode;
    }

    private String currentTab = "Day"; // "Day", "Week", "Month", "Year"

    @FXML
    public void initialize() {
        updateTabStyles();
        
        NumberAxis yAxis = (NumberAxis) chartSleepLine.getYAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(3);
        yAxis.setTickUnit(1);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                int val = object.intValue();
                if (val == 0) return "Deep";
                if (val == 1) return "Light";
                if (val == 2) return "REM";
                if (val == 3) return "Awake";
                return "";
            }
            @Override
            public Number fromString(String string) {
                return 0;
            }
        });
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

        // Display current session hours and minutes
        lblSleepHoursVal.setText(patientProfile.sleepHours != null ? patientProfile.sleepHours : "0");
        lblSleepMinutesVal.setText(patientProfile.sleepMinutes != null ? patientProfile.sleepMinutes : "0");

        // Update headers based on selected tab
        if (currentTab.equals("Day")) {
            lblDateSelector.setText("Fri, 27 Mar");
        } else if (currentTab.equals("Week")) {
            lblDateSelector.setText("23 Mar – 29 Mar");
        } else if (currentTab.equals("Month")) {
            lblDateSelector.setText("March 2026");
        } else {
            lblDateSelector.setText("Year 2026");
        }

        updateTabStyles();
        drawSleepChart();
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

    private void drawSleepChart() {
        boolean isDay = currentTab.equals("Day");
        chartSleepLine.setVisible(isDay);
        chartSleepLine.setManaged(isDay);
        chartSleepBar.setVisible(!isDay);
        chartSleepBar.setManaged(!isDay);

        if (vboxScaleDetailsList != null) {
            vboxScaleDetailsList.getChildren().clear();
        }

        if (isDay) {
            chartSleepLine.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Sleep Stage");

            String[] times = {"22:00", "22:30", "23:30", "01:00", "01:45", "03:00", "04:15", "05:00", "06:30"};
            int[] states = {3, 1, 0, 2, 1, 0, 2, 1, 3};

            for (int i = 0; i < times.length; i++) {
                series.getData().add(new XYChart.Data<>(times[i], states[i]));
                if (vboxScaleDetailsList != null) {
                    String stageName = "";
                    String color = "#FF9500";
                    if (states[i] == 0) { stageName = "Deep sleep"; color = "#0040DD"; }
                    else if (states[i] == 1) { stageName = "Light sleep"; color = "#5856D6"; }
                    else if (states[i] == 2) { stageName = "REM sleep"; color = "#5AC8FA"; }
                    else { stageName = "Awake"; color = "#FF9500"; }
                    vboxScaleDetailsList.getChildren().add(createDetailRow(times[i] + " (Hypnogram Stage)", stageName, color));
                }
            }

            chartSleepLine.getData().add(series);

            Platform.runLater(() -> {
                if (series.getNode() != null) {
                    Node line = series.getNode().lookup(".chart-series-line");
                    if (line != null) {
                        line.setStyle("-fx-stroke: #5856D6; -fx-stroke-width: 3.0;");
                    }
                }
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        int state = data.getYValue().intValue();
                        String colorHex = "#FF9500";
                        if (state == 0) colorHex = "#0040DD";
                        else if (state == 1) colorHex = "#5856D6";
                        else if (state == 2) colorHex = "#5AC8FA";
                        data.getNode().setStyle("-fx-background-color: " + colorHex + ", white; -fx-background-radius: 4px; -fx-padding: 4px;");
                    }
                }
            });

        } else {
            chartSleepBar.getData().clear();
            
            XYChart.Series<String, Number> seriesDeep = new XYChart.Series<>();
            XYChart.Series<String, Number> seriesLight = new XYChart.Series<>();
            XYChart.Series<String, Number> seriesREM = new XYChart.Series<>();
            XYChart.Series<String, Number> seriesAwake = new XYChart.Series<>();

            seriesDeep.setName("Deep");
            seriesLight.setName("Light");
            seriesREM.setName("REM");
            seriesAwake.setName("Awake");

            if (currentTab.equals("Week")) {
                String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
                double[][] daySleepData = {
                    {2.0, 4.5, 1.2, 0.3},
                    {1.8, 5.0, 1.0, 0.2},
                    {2.2, 4.2, 1.5, 0.5},
                    {1.5, 3.8, 1.1, 0.4},
                    {2.5, 5.2, 1.6, 0.2},
                    {2.8, 6.0, 2.0, 0.6},
                    {2.3, 4.8, 1.4, 0.3}
                };

                for (int i = 0; i < days.length; i++) {
                    seriesDeep.getData().add(new XYChart.Data<>(days[i], daySleepData[i][0]));
                    seriesLight.getData().add(new XYChart.Data<>(days[i], daySleepData[i][1]));
                    seriesREM.getData().add(new XYChart.Data<>(days[i], daySleepData[i][2]));
                    seriesAwake.getData().add(new XYChart.Data<>(days[i], daySleepData[i][3]));
                    if (vboxScaleDetailsList != null) {
                        double total = daySleepData[i][0] + daySleepData[i][1] + daySleepData[i][2] + daySleepData[i][3];
                        vboxScaleDetailsList.getChildren().add(createDetailRow(days[i] + " (Daily sleep duration)", String.format("%.1f hours", total), "#5856D6"));
                    }
                }
            } else if (currentTab.equals("Month")) {
                String[] weeksText = {"Week 1", "Week 2", "Week 3", "Week 4"};
                double[][] weekSleepData = {
                    {1.8, 4.8, 1.3, 0.3},
                    {2.1, 4.5, 1.4, 0.4},
                    {2.3, 5.0, 1.6, 0.2},
                    {1.9, 4.2, 1.2, 0.5}
                };

                for (int i = 0; i < weeksText.length; i++) {
                    seriesDeep.getData().add(new XYChart.Data<>(weeksText[i], weekSleepData[i][0]));
                    seriesLight.getData().add(new XYChart.Data<>(weeksText[i], weekSleepData[i][1]));
                    seriesREM.getData().add(new XYChart.Data<>(weeksText[i], weekSleepData[i][2]));
                    seriesAwake.getData().add(new XYChart.Data<>(weeksText[i], weekSleepData[i][3]));
                    if (vboxScaleDetailsList != null) {
                        double total = weekSleepData[i][0] + weekSleepData[i][1] + weekSleepData[i][2] + weekSleepData[i][3];
                        vboxScaleDetailsList.getChildren().add(createDetailRow(weeksText[i] + " (Weekly average)", String.format("%.1f hours", total), "#5856D6"));
                    }
                }
            } else {
                String[] quartersText = {"Q1", "Q2", "Q3", "Q4"};
                double[][] qSleepData = {
                    {2.0, 4.6, 1.4, 0.3},
                    {1.9, 4.4, 1.3, 0.4},
                    {2.1, 4.9, 1.5, 0.2},
                    {2.2, 4.8, 1.6, 0.3}
                };

                for (int i = 0; i < quartersText.length; i++) {
                    seriesDeep.getData().add(new XYChart.Data<>(quartersText[i], qSleepData[i][0]));
                    seriesLight.getData().add(new XYChart.Data<>(quartersText[i], qSleepData[i][1]));
                    seriesREM.getData().add(new XYChart.Data<>(quartersText[i], qSleepData[i][2]));
                    seriesAwake.getData().add(new XYChart.Data<>(quartersText[i], qSleepData[i][3]));
                    if (vboxScaleDetailsList != null) {
                        double total = qSleepData[i][0] + qSleepData[i][1] + qSleepData[i][2] + qSleepData[i][3];
                        vboxScaleDetailsList.getChildren().add(createDetailRow(quartersText[i] + " (Quarterly average)", String.format("%.1f hours", total), "#5856D6"));
                    }
                }
            }

            chartSleepBar.getData().addAll(seriesDeep, seriesLight, seriesREM, seriesAwake);

            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : seriesDeep.getData()) {
                    if (data.getNode() != null) data.getNode().setStyle("-fx-bar-fill: #0040DD;");
                }
                for (XYChart.Data<String, Number> data : seriesLight.getData()) {
                    if (data.getNode() != null) data.getNode().setStyle("-fx-bar-fill: #5856D6;");
                }
                for (XYChart.Data<String, Number> data : seriesREM.getData()) {
                    if (data.getNode() != null) data.getNode().setStyle("-fx-bar-fill: #5AC8FA;");
                }
                for (XYChart.Data<String, Number> data : seriesAwake.getData()) {
                    if (data.getNode() != null) data.getNode().setStyle("-fx-bar-fill: #FF9500;");
                }
            });
        }
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

    @FXML
    private void handleSelectDayTab() {
        currentTab = "Day";
        loadProfileData();
    }

    @FXML
    private void handleSelectWeekTab() {
        currentTab = "Week";
        loadProfileData();
    }

    @FXML
    private void handleSelectMonthTab() {
        currentTab = "Month";
        loadProfileData();
    }

    @FXML
    private void handleSelectYearTab() {
        currentTab = "Year";
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
    private void handleLogSleep() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Log Sleep Duration");
        dialog.setHeaderText("Record sleep duration for User " + activePatient.fullName);
        
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 40, 20, 20));
        
        TextField txtHours = new TextField(patientProfile.sleepHours != null ? patientProfile.sleepHours : "8");
        TextField txtMinutes = new TextField(patientProfile.sleepMinutes != null ? patientProfile.sleepMinutes : "0");
        
        txtHours.setPrefWidth(60);
        txtMinutes.setPrefWidth(60);
        
        grid.add(new Label("Hours:"), 0, 0);
        grid.add(txtHours, 1, 0);
        grid.add(new Label("Minutes:"), 0, 1);
        grid.add(txtMinutes, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                int hrs = Integer.parseInt(txtHours.getText().trim());
                int mins = Integer.parseInt(txtMinutes.getText().trim());
                
                if (hrs >= 0 && hrs <= 24 && mins >= 0 && mins < 60) {
                    patientProfile.sleepHours = String.valueOf(hrs);
                    patientProfile.sleepMinutes = String.valueOf(mins);
                    
                    // Add system clinical note update
                    patientProfile.clinicalNotes.add("[SYSTEM NOTE]: Sleep manually logged: " + hrs + " hr " + mins + " min slept.");
                    
                    // Persistence
                    MockDatabase.saveDatabase();
                    loadProfileData();
                    
                    // Sync to main dashboard
                    if (parentController != null) {
                        parentController.initializeSession(activePatient);
                    }
                } else {
                    showAlert("Invalid Input", "Hours must be between 0 and 24, and minutes between 0 and 59.");
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Format", "Please enter valid whole numbers for hours and minutes.");
            }
        }
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
