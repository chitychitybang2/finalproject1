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
public class AdminSplashController {
    @FXML private Label lblGreeting;
    private MockDatabase.UserAccount activeAdmin;
    public void startLoadingSequence(MockDatabase.UserAccount account) {
        this.activeAdmin = account;
        
        if (lblGreeting != null && account.fullName != null) {
            lblGreeting.setText("Welcome Back, " + account.fullName);
        }
        // Apple-style background thread transition pause delay duration (2 seconds)
        Timeline loadingDelayTimeline = new Timeline(new KeyFrame(Duration.seconds(2.0), event -> proceedToAdminDashboard()));
        loadingDelayTimeline.play();
    }
    private void proceedToAdminDashboard() {
        try {
            System.out.println("[DIAGNOSTIC] proceedToAdminDashboard starting...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Parent root = loader.load();
            System.out.println("[DIAGNOSTIC] FXML loaded successfully.");
            AdminDashboardController controller = loader.getController();
            if (controller == null) {
                System.err.println("[DIAGNOSTIC] ERROR: AdminDashboardController is null!");
            }
            controller.initializeSession(activeAdmin);
            System.out.println("[DIAGNOSTIC] initializeSession completed.");
            Stage stage = (Stage) lblGreeting.getScene().getWindow();
            if (stage != null) {
                if (stage.getScene() != null) {
                    stage.getScene().setRoot(root);
                } else {
                    stage.setScene(new Scene(root));
                }
                stage.show();
            }
            System.out.println("[DIAGNOSTIC] proceedToAdminDashboard completed successfully.");
        } catch (Throwable t) {
            System.err.println("[DIAGNOSTIC] EXCEPTION CAUGHT in proceedToAdminDashboard:");
            t.printStackTrace();
        }
    }
}