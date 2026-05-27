package com.mycompany.projectbuang;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

public class PatientDashboardController {
    
    @FXML private Label lblWelcomeName, lblPatientMetaInfo, lblHeartRate, lblTemperature, lblOxygen;
    @FXML private Label lblMove, lblExercise, lblStand;
    @FXML private Label lblBMI, lblBMICategory, lblStayFitPlanStatus, lblStayFitPlanDetails;
    @FXML private Label lblWeight;
    @FXML private Label lblSleepHours, lblSleepMinutes;
    @FXML private Label lblStateOfMindBadge, lblCurrentCondition;
    
    @FXML private Arc arcMove, arcExercise, arcStand;
    @FXML private VBox vboxNotesContainer;
    @FXML private javafx.scene.layout.StackPane centerStack;
    @FXML private javafx.scene.control.ScrollPane scrollMainOverview;

    @FXML private VBox sidebarVBox;
    @FXML private Button btnToggleSidebar;
    @FXML private Button btnResetAll;
    @FXML private Label lblSidebarHeader, lblCategoriesHeader;
    
    @FXML private HBox itemSummary, itemActivity, itemBMI, itemWeight, itemHeart, itemOxygen, itemSleep, itemCondition;
    @FXML private Label lblMenuSummaryText, lblMenuActivityText, lblMenuBMIText, lblMenuWeightText, lblMenuHeartText, lblMenuOxygenText, lblMenuSleepText, lblMenuConditionText;

    private boolean sidebarCollapsed = false;
    private MockDatabase.UserAccount activePatient;

    public void initializeSession(MockDatabase.UserAccount patientAccount) {
        this.activePatient = patientAccount;
        lblWelcomeName.setText(patientAccount.fullName);
        
        MockDatabase.PatientProfile profile = MockDatabase.patientDatabase.get(patientAccount.fullName);
        if (profile != null) {
            if (profile.meta != null && !profile.meta.trim().isEmpty()) {
                lblPatientMetaInfo.setText("Last sync: Today at 9:41 AM | " + profile.meta.trim());
            } else {
                lblPatientMetaInfo.setText("Last sync: Today at 9:41 AM");
            }
            lblHeartRate.setText(profile.heartRate);
            lblTemperature.setText("Temp: " + profile.temperature + "°C");
            lblOxygen.setText(profile.oxygen != null ? profile.oxygen : "--");
            
            // Set dynamic fields with safety fallbacks
            lblWeight.setText(profile.weight != null ? profile.weight : "0");
            lblMove.setText(profile.moveCal != null ? profile.moveCal : "0");
            lblExercise.setText(profile.exerciseMin != null ? profile.exerciseMin : "0");
            lblStand.setText(profile.standHr != null ? profile.standHr : "0");
            lblSleepHours.setText(profile.sleepHours != null ? profile.sleepHours : "0");
            lblSleepMinutes.setText(profile.sleepMinutes != null ? profile.sleepMinutes : "0");
            
            // BMI card details update
            double bmiVal = parseDoubleSafe(profile.bmi, 0.0);
            lblBMI.setText(profile.bmi != null ? profile.bmi : "--");
            if (bmiVal == 0.0) {
                lblBMICategory.setText("Not Set");
                lblBMICategory.setStyle("-fx-font-size: 12px; -fx-text-fill: #86868B; -fx-background-color: #E5E5EA; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            } else if (bmiVal < 18.5) {
                lblBMICategory.setText("Underweight");
                lblBMICategory.setStyle("-fx-font-size: 12px; -fx-text-fill: #0A84FF; -fx-background-color: #E6F2FF; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            } else if (bmiVal < 25.0) {
                lblBMICategory.setText("Normal");
                lblBMICategory.setStyle("-fx-font-size: 12px; -fx-text-fill: #34C759; -fx-background-color: #E6F7ED; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            } else if (bmiVal < 30.0) {
                lblBMICategory.setText("Overweight");
                lblBMICategory.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9500; -fx-background-color: #FFF2E6; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            } else {
                lblBMICategory.setText("Obese");
                lblBMICategory.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF3B30; -fx-background-color: #FFE6E6; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            }

            if (profile.fitPlanGoal != null && !profile.fitPlanGoal.equals("0") && !profile.fitPlanGoal.equals("0.0")) {
                lblStayFitPlanStatus.setText("Plan Active");
                lblStayFitPlanStatus.setStyle("-fx-text-fill: #34C759; -fx-font-weight: 700; -fx-font-size: 11px;");
                lblStayFitPlanDetails.setText("Height: " + (profile.height != null ? profile.height : "--") + " cm | Goal: " + profile.fitPlanGoal + " kg. Focus: " + (profile.fitPlanFocus != null ? profile.fitPlanFocus : "None"));
            } else {
                lblStayFitPlanStatus.setText("No Plan");
                lblStayFitPlanStatus.setStyle("-fx-text-fill: #86868B; -fx-font-weight: 700; -fx-font-size: 11px;");
                lblStayFitPlanDetails.setText("Height: " + (profile.height != null ? profile.height : "--") + " cm. Click to set up your Stay Fit Plan wizard.");
            }
            
            String condText = profile.currentCondition != null ? profile.currentCondition : "Healthy";
            lblCurrentCondition.setText(condText);
            if (condText.equalsIgnoreCase("Healthy") || condText.equalsIgnoreCase("Normal") || condText.equalsIgnoreCase("None")) {
                lblCurrentCondition.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #34C759;");
            } else {
                lblCurrentCondition.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #FF3B30;");
            }
            
            String badgeText = profile.stateOfMindBadge != null ? profile.stateOfMindBadge : "Neutral";
            lblStateOfMindBadge.setText(badgeText);
            
            // Dynamically style State of Mind Badge to feel premium
            if (badgeText.equalsIgnoreCase("Relaxed") || badgeText.equalsIgnoreCase("Calm")) {
                lblStateOfMindBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #34C759; -fx-font-weight: 700; -fx-background-color: #E6F7ED; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            } else if (badgeText.equalsIgnoreCase("Tired") || badgeText.equalsIgnoreCase("Fatigued")) {
                lblStateOfMindBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF9500; -fx-font-weight: 700; -fx-background-color: #FFF2E6; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            } else {
                lblStateOfMindBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #0071E3; -fx-font-weight: 700; -fx-background-color: #E6F2FF; -fx-background-radius: 6; -fx-padding: 4 8 4 8;");
            }
            
            // Activity Ring Calculations
            double moveVal = parseDoubleSafe(profile.moveCal, 0.0);
            double exerciseVal = parseDoubleSafe(profile.exerciseMin, 0.0);
            double standVal = parseDoubleSafe(profile.standHr, 0.0);
            
            double moveGoal = 500.0;
            double exerciseGoal = 30.0;
            double standGoal = 12.0;
            
            double pMove = moveGoal > 0 ? Math.min(moveVal / moveGoal, 1.0) : 0.0;
            double pExercise = exerciseGoal > 0 ? Math.min(exerciseVal / exerciseGoal, 1.0) : 0.0;
            double pStand = standGoal > 0 ? Math.min(standVal / standGoal, 1.0) : 0.0;
            
            // In JavaFX, negative lengths draw clockwise starting from startAngle (90.0)
            if (arcMove != null) {
                arcMove.setLength(-pMove * 360.0);
            }
            if (arcExercise != null) {
                arcExercise.setLength(-pExercise * 360.0);
            }
            if (arcStand != null) {
                arcStand.setLength(-pStand * 360.0);
            }
            
            // Render clinical notes list
            vboxNotesContainer.getChildren().clear();
            for (String entryLog : profile.clinicalNotes) {
                Label item = new Label(entryLog);
                item.setStyle("-fx-background-color: #F5F5F7; -fx-text-fill: #1D1D1F; -fx-background-radius: 8; -fx-font-size: 13px;");
                item.setPadding(new Insets(10, 14, 10, 14));
                item.setMaxWidth(Double.MAX_VALUE);
                item.setWrapText(true);
                vboxNotesContainer.getChildren().add(item);
            }
        }
    }

    public void showDetailView(javafx.scene.Parent detailNode) {
        if (detailNode instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) detailNode).setMaxWidth(Double.MAX_VALUE);
            ((javafx.scene.layout.Region) detailNode).setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        }
        
        scrollMainOverview.setVisible(false);
        scrollMainOverview.setManaged(false);
        
        // Remove any existing detail views first
        if (centerStack.getChildren().size() > 1) {
            centerStack.getChildren().remove(1, centerStack.getChildren().size());
        }
        
        centerStack.getChildren().add(detailNode);
        javafx.scene.layout.StackPane.setAlignment(detailNode, javafx.geometry.Pos.TOP_CENTER);
    }

    public void shrinkDetailView(javafx.scene.Node detailNode) {
        centerStack.getChildren().remove(detailNode);
        scrollMainOverview.setVisible(true);
        scrollMainOverview.setManaged(true);
        setActiveTab(itemSummary);
    }

    @FXML
    private void handleOpenProfile() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("PatientProfileEdit.fxml"));
            javafx.scene.Parent root = loader.load();
            
            PatientProfileEditController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenStayFitPlan() {
        try {
            setActiveTab(itemBMI);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("StayFitWizard.fxml"));
            javafx.scene.Parent root = loader.load();
            
            StayFitWizardController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenWeightDetails() {
        try {
            setActiveTab(itemWeight);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("WeightDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            
            WeightDetailsController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenActivityDetails() {
        try {
            setActiveTab(itemActivity);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ActivityDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ActivityDetailsController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenHeartDetails() {
        try {
            setActiveTab(itemHeart);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("HeartDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            
            HeartDetailsController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenSleepDetails() {
        try {
            setActiveTab(itemSleep);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("SleepDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            
            SleepDetailsController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenOxygenDetails() {
        try {
            setActiveTab(itemOxygen);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("OxygenDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            
            OxygenDetailsController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOpenStateOfMind() {
        try {
            setActiveTab(itemCondition);
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("StateOfMindDetails.fxml"));
            javafx.scene.Parent root = loader.load();
            
            StateOfMindDetailsController controller = loader.getController();
            controller.setSessionContext(activePatient, this);
            controller.setRootNode(root);
            
            showDetailView(root);
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @FXML
    private void handleShowSummary() {
        setActiveTab(itemSummary);
        scrollMainOverview.setVisible(true);
        scrollMainOverview.setManaged(true);
        if (centerStack.getChildren().size() > 1) {
            centerStack.getChildren().remove(1, centerStack.getChildren().size());
        }
    }

    private void setActiveTab(HBox activeItem) {
        HBox[] allItems = {itemSummary, itemActivity, itemBMI, itemWeight, itemHeart, itemOxygen, itemSleep, itemCondition};
        for (HBox item : allItems) {
            if (item == null) continue;
            if (item == activeItem) {
                item.setStyle("-fx-background-color: #E8F2FF; -fx-background-radius: 10; -fx-padding: 10 12 10 12; -fx-cursor: hand;");
                Label txtLbl = (Label) item.getChildren().get(1);
                txtLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #0071E3;");
            } else {
                item.setStyle("-fx-background-color: transparent; -fx-background-radius: 10; -fx-padding: 10 12 10 12; -fx-cursor: hand;");
                Label txtLbl = (Label) item.getChildren().get(1);
                txtLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1D1D1F;");
            }
        }
    }

    @FXML
    private void handleToggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        if (sidebarCollapsed) {
            sidebarVBox.setPrefWidth(70.0);
            sidebarVBox.setStyle("-fx-background-color: white; -fx-padding: 24 10 24 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 0);");
            btnToggleSidebar.setText("▸");
            btnResetAll.setText("🔄");
            
            lblSidebarHeader.setVisible(false); lblSidebarHeader.setManaged(false); lblSidebarHeader.setText("");
            lblCategoriesHeader.setVisible(false); lblCategoriesHeader.setManaged(false); lblCategoriesHeader.setText("");
            
            lblMenuSummaryText.setVisible(false); lblMenuSummaryText.setManaged(false); lblMenuSummaryText.setText("");
            lblMenuActivityText.setVisible(false); lblMenuActivityText.setManaged(false); lblMenuActivityText.setText("");
            lblMenuBMIText.setVisible(false); lblMenuBMIText.setManaged(false); lblMenuBMIText.setText("");
            lblMenuWeightText.setVisible(false); lblMenuWeightText.setManaged(false); lblMenuWeightText.setText("");
            lblMenuHeartText.setVisible(false); lblMenuHeartText.setManaged(false); lblMenuHeartText.setText("");
            lblMenuOxygenText.setVisible(false); lblMenuOxygenText.setManaged(false); lblMenuOxygenText.setText("");
            lblMenuSleepText.setVisible(false); lblMenuSleepText.setManaged(false); lblMenuSleepText.setText("");
            lblMenuConditionText.setVisible(false); lblMenuConditionText.setManaged(false); lblMenuConditionText.setText("");
        } else {
            sidebarVBox.setPrefWidth(280.0);
            sidebarVBox.setStyle("-fx-background-color: white; -fx-padding: 24 16 24 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 10, 0, 0, 0);");
            btnToggleSidebar.setText("◂");
            btnResetAll.setText("🔄 Reset All Data");
            
            lblSidebarHeader.setVisible(true); lblSidebarHeader.setManaged(true); lblSidebarHeader.setText("Summary");
            lblCategoriesHeader.setVisible(true); lblCategoriesHeader.setManaged(true); lblCategoriesHeader.setText("Health Categories");
            
            lblMenuSummaryText.setVisible(true); lblMenuSummaryText.setManaged(true); lblMenuSummaryText.setText("Summary");
            lblMenuActivityText.setVisible(true); lblMenuActivityText.setManaged(true); lblMenuActivityText.setText("Activity");
            lblMenuBMIText.setVisible(true); lblMenuBMIText.setManaged(true); lblMenuBMIText.setText("BMI");
            lblMenuWeightText.setVisible(true); lblMenuWeightText.setManaged(true); lblMenuWeightText.setText("Weight");
            lblMenuHeartText.setVisible(true); lblMenuHeartText.setManaged(true); lblMenuHeartText.setText("Heart");
            lblMenuOxygenText.setVisible(true); lblMenuOxygenText.setManaged(true); lblMenuOxygenText.setText("Oxygen");
            lblMenuSleepText.setVisible(true); lblMenuSleepText.setManaged(true); lblMenuSleepText.setText("Sleep");
            lblMenuConditionText.setVisible(true); lblMenuConditionText.setManaged(true); lblMenuConditionText.setText("My Condition");
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

    @FXML
    private void handleResetAllData() {
        MockDatabase.PatientProfile patientProfile = MockDatabase.patientDatabase.get(activePatient.fullName);
        if (patientProfile == null) return;
        
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Data");
        alert.setHeaderText("Reset All Health Metrics?");
        alert.setContentText("This will permanently clear your activity rings, steps, weight history, sleep logs, heart rate, oxygen levels, and clinical logs. This action cannot be undone.");
        
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            patientProfile.heartRate = "--";
            patientProfile.temperature = "--";
            patientProfile.oxygen = "--";
            patientProfile.weight = "0";
            patientProfile.sleepHours = "0";
            patientProfile.sleepMinutes = "0";
            patientProfile.moveCal = "0";
            patientProfile.exerciseMin = "0";
            patientProfile.standHr = "0";
            patientProfile.bmi = "0.0";
            patientProfile.stateOfMind = "Healthy and active";
            patientProfile.stateOfMindBadge = "Calm";
            patientProfile.currentCondition = "Healthy";
            patientProfile.fitPlanGoal = "0";
            patientProfile.fitPlanFocus = "";
            patientProfile.fitPlanDays = "";
            patientProfile.fitPlanTime = "";
            patientProfile.calorieBurnt = "0";
            patientProfile.calorieConsumed = "0";
            patientProfile.weightHistory = "0,0,0";
            patientProfile.mealsCalorieMap = "breakfast=0;lunch=0;dinner=0";
            patientProfile.stepCount = "0";
            patientProfile.stepGoal = "10000";
            patientProfile.stepHistory = "0,0,0,0";
            patientProfile.heartRateMinMaxRange = "60-100";
            patientProfile.heartRateHistory = "0,0,0,0,0";
            patientProfile.oxygenHistory = "0,0,0,0";
            
            patientProfile.clinicalNotes.clear();
            patientProfile.clinicalNotes.add("All activity and data input reset.");
            
            MockDatabase.saveDatabase();
            MockDatabase.logActivity("All health database metrics reset for " + activePatient.fullName);
            
            // Re-initialize the patient overview dashboard immediately
            initializeSession(activePatient);
            
            // Close any open detail views and go back to main overview/Summary
            handleShowSummary();
            
            javafx.scene.control.Alert success = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            success.setTitle("Reset Successful");
            success.setHeaderText(null);
            success.setContentText("All your data has been successfully reset. You can now log new entries.");
            success.showAndWait();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("Login.fxml"));
            javafx.scene.Parent root = loader.load();
             javafx.stage.Stage stage = (javafx.stage.Stage) lblWelcomeName.getScene().getWindow();
             if (stage.getScene() != null) {
                 stage.getScene().setRoot(root);
             } else {
                 stage.setScene(new javafx.scene.Scene(root));
             }
            stage.show();
            MockDatabase.logActivity("User Session terminated. Routed to Login.");
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }
}
