package com.mycompany.projectbuang;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class PatientSplashController {

    @FXML private Label lblPatientGreeting;
    private MockDatabase.UserAccount activePatient;

    public void startLoadingSequence(MockDatabase.UserAccount account) {
        this.activePatient = account;
        lblPatientGreeting.setText("Hello, " + account.fullName);

        Timeline loadingDelayTimeline = new Timeline(new KeyFrame(Duration.seconds(2.0), event -> proceedToPatientDashboard()));
        loadingDelayTimeline.play();
    }

    private void proceedToPatientDashboard() {
        try {
            System.out.println("[DIAGNOSTIC] proceedToPatientDashboard starting...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PatientDashboard.fxml"));
            Parent root = loader.load();
            System.out.println("[DIAGNOSTIC] FXML loaded successfully.");
            PatientDashboardController controller = loader.getController();
            if (controller == null) {
                System.err.println("[DIAGNOSTIC] ERROR: PatientDashboardController is null!");
            }
            controller.initializeSession(activePatient);
            System.out.println("[DIAGNOSTIC] initializeSession completed.");
            Stage stage = (Stage) lblPatientGreeting.getScene().getWindow();
            if (stage != null) {
                if (stage.getScene() != null) {
                    stage.getScene().setRoot(root);
                } else {
                    stage.setScene(new Scene(root));
                }
                stage.show();
            }
            System.out.println("[DIAGNOSTIC] proceedToPatientDashboard completed successfully.");
        } catch (Throwable t) {
            System.err.println("[DIAGNOSTIC] EXCEPTION CAUGHT in proceedToPatientDashboard:");
            t.printStackTrace();
        }
    }
}