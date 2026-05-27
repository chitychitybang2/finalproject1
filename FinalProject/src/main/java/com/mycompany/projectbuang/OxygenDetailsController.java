package com.mycompany.projectbuang;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class OxygenDetailsController {

    @FXML private Label lblOxygenRange, lblCurrentOxygen, lblCurrentTemp, lblUpdateStatus;
    @FXML private LineChart<String, Number> chartOxygen;
    @FXML private TextField txtOxygenInput, txtTempInput;
    @FXML private javafx.scene.layout.VBox vboxScaleDetailsList;

    private MockDatabase.UserAccount activePatient;
    private PatientDashboardController parentController;
    private MockDatabase.PatientProfile patientProfile;
    private javafx.scene.Parent rootNode;

    public void setRootNode(javafx.scene.Parent rootNode) {
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
        if (patientProfile == null) return;

        lblCurrentOxygen.setText(patientProfile.oxygen != null ? patientProfile.oxygen : "--");
        lblCurrentTemp.setText(patientProfile.temperature != null ? patientProfile.temperature : "--");
        lblUpdateStatus.setText("Updated: Today at 9:41 AM");

        // Parse min/max from oxygenHistory
        int min = 100;
        int max = 0;
        if (patientProfile.oxygenHistory != null && !patientProfile.oxygenHistory.isEmpty()) {
            String[] parts = patientProfile.oxygenHistory.split(",");
            for (String p : parts) {
                try {
                    int val = Integer.parseInt(p.trim());
                    if (val < min) min = val;
                    if (val > max) max = val;
                } catch (NumberFormatException e) {}
            }
        }
        if (max >= min) {
            lblOxygenRange.setText(min + "–" + max + "%");
        } else {
            lblOxygenRange.setText("95–99%");
        }

        drawOxygenChart();
    }

    private javafx.scene.layout.HBox createDetailRow(String leftText, String rightText, String colorHex) {
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox();
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hbox.setPadding(new javafx.geometry.Insets(4, 0, 4, 0));
        
        javafx.scene.control.Label lblLeft = new javafx.scene.control.Label(leftText);
        lblLeft.setStyle("-fx-text-fill: #1D1D1F; -fx-font-size: 13px; -fx-font-weight: 500;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        javafx.scene.control.Label lblRight = new javafx.scene.control.Label(rightText);
        lblRight.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        
        hbox.getChildren().addAll(lblLeft, spacer, lblRight);
        return hbox;
    }

    private void drawOxygenChart() {
        chartOxygen.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Oxygen Saturation");

        if (vboxScaleDetailsList != null) {
            vboxScaleDetailsList.getChildren().clear();
        }

        if (patientProfile.oxygenHistory != null && !patientProfile.oxygenHistory.isEmpty()) {
            String[] parts = patientProfile.oxygenHistory.split(",");
            String[] times = {"08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"};
            
            for (int i = 0; i < parts.length; i++) {
                try {
                    int val = Integer.parseInt(parts[i].trim());
                    String timeLabel = (i < times.length) ? times[i] : ("Point " + (i + 1));
                    series.getData().add(new XYChart.Data<>(timeLabel, val));
                    if (vboxScaleDetailsList != null) {
                        vboxScaleDetailsList.getChildren().add(createDetailRow(timeLabel + " (Reading)", val + "% SpO2", "#0071E3"));
                    }
                } catch (NumberFormatException e) {}
            }
        }

        chartOxygen.getData().add(series);

        Platform.runLater(() -> {
            if (series.getNode() != null) {
                Node line = series.getNode().lookup(".chart-series-line");
                if (line != null) {
                    line.setStyle("-fx-stroke: #0071E3; -fx-stroke-width: 2.5;");
                }
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-color: #0071E3, white; -fx-background-radius: 4px; -fx-padding: 4px;");
                    }
                }
            }
        });
    }

    @FXML
    private void handleLogOxygen() {
        String oxInput = txtOxygenInput.getText().trim();
        String tempInput = txtTempInput != null ? txtTempInput.getText().trim() : "";

        if (oxInput.isEmpty() && tempInput.isEmpty()) {
            showAlert("Invalid Input", "Please enter at least one value (Oxygen Level or Temperature).");
            return;
        }

        boolean hasOx = !oxInput.isEmpty();
        boolean hasTemp = !tempInput.isEmpty();

        int newSpO2 = -1;
        double newTemp = -1;

        if (hasOx) {
            try {
                newSpO2 = Integer.parseInt(oxInput);
                if (newSpO2 < 50 || newSpO2 > 100) {
                    showAlert("Invalid Range", "Please enter a realistic blood oxygen percentage between 50% and 100%.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Format", "Please enter a valid whole number for blood oxygen.");
                return;
            }
        }

        if (hasTemp) {
            try {
                newTemp = Double.parseDouble(tempInput);
                if (newTemp < 30.0 || newTemp > 45.0) {
                    showAlert("Invalid Range", "Please enter a realistic body temperature between 30.0°C and 45.0°C.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Format", "Please enter a valid decimal number for temperature.");
                return;
            }
        }

        // Apply updates
        if (hasOx) {
            patientProfile.oxygen = String.valueOf(newSpO2);
            // Append to sliding history window
            if (patientProfile.oxygenHistory == null || patientProfile.oxygenHistory.isEmpty()) {
                patientProfile.oxygenHistory = String.valueOf(newSpO2);
            } else {
                String[] parts = patientProfile.oxygenHistory.split(",");
                StringBuilder sb = new StringBuilder();
                int startIdx = parts.length >= 8 ? 1 : 0;
                for (int i = startIdx; i < parts.length; i++) {
                    sb.append(parts[i]).append(",");
                }
                sb.append(newSpO2);
                patientProfile.oxygenHistory = sb.toString();
            }
            patientProfile.clinicalNotes.add("[LIVE OVERVIEW]: Updated Blood Oxygen saturation locked at: " + newSpO2 + "%");
        }

        if (hasTemp) {
            String tempStr = String.format(java.util.Locale.US, "%.1f", newTemp);
            patientProfile.temperature = tempStr;
            patientProfile.clinicalNotes.add("[LIVE OVERVIEW]: Updated Body Temperature locked at: " + tempStr + "°C");
        }

        MockDatabase.saveDatabase();
        loadProfileData();

        if (parentController != null) {
            parentController.initializeSession(activePatient);
        }

        txtOxygenInput.clear();
        if (txtTempInput != null) {
            txtTempInput.clear();
        }
    }

    @FXML
    private void handleClose() {
        if (parentController != null && rootNode != null) {
            parentController.shrinkDetailView(rootNode);
        } else {
            Stage stage = (Stage) chartOxygen.getScene().getWindow();
            if (stage != null) stage.close();
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
