package com.mycompany.projectbuang;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
public class LoginController {
    @FXML private Label lblTitle, lblStatusMessage;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword, txtConfirmPassword;
    @FXML private Button btnPatientAction, btnSecondaryToggle;
    private boolean isLoginMode = true;
    @FXML
    private void handleToggleMode() {
        isLoginMode = !isLoginMode;
        lblStatusMessage.setText("");
        txtConfirmPassword.clear();
        txtConfirmPassword.setVisible(!isLoginMode);
        txtConfirmPassword.setManaged(!isLoginMode);
        
        if (isLoginMode) {
            lblTitle.setText("Smart Healthcare Monitoring System");
            btnPatientAction.setText("Login");
            btnSecondaryToggle.setText("Create an account");
        } else {
            lblTitle.setText("User Registration");
            btnPatientAction.setText("Register Account");
            btnSecondaryToggle.setText("Back to Login");
        }
    }
    @FXML
    private void handlePatientSignIn() {
        if (isLoginMode) {
            processGateUnified();
        } else {
            processRegistration();
        }
    }
    private void processGateUnified() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            lblStatusMessage.setText("Fields cannot be empty.");
            return;
        }
        MockDatabase.UserAccount account = MockDatabase.userDatabase.get(user);
        if (account != null && account.password.equals(pass)) {
            MockDatabase.logActivity(account.roleType + " authenticated. Routing to separate splash gate: " + user);
            routeToSplashTransition(account);
        } else {
            lblStatusMessage.setText("Errauwrrr!! Please create an account first");
        }
    }
    private void processRegistration() {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText();
        if (user.isEmpty() || pass.isEmpty() || !pass.equals(txtConfirmPassword.getText())) {
            lblStatusMessage.setText("Invalid entry or passwords mismatch.");
            return;
        }
        if (MockDatabase.userDatabase.containsKey(user)) {
            lblStatusMessage.setText("Username already taken.");
            return;
        }
        String formalName = user.substring(0, 1).toUpperCase() + user.substring(1).toLowerCase();
        MockDatabase.userDatabase.put(user, new MockDatabase.UserAccount(user, pass, "PATIENT", formalName, "User"));
        MockDatabase.patientDatabase.put(formalName, new MockDatabase.PatientProfile(formalName, "", "0", "0", "0"));
        MockDatabase.saveDatabase();
        
        txtUsername.clear(); txtPassword.clear(); handleToggleMode();
        lblStatusMessage.setStyle("-fx-text-fill: #34C759;");
        lblStatusMessage.setText("Account registered! Please sign in.");
    }
    private void routeToSplashTransition(MockDatabase.UserAccount account) {
        try {
            String splashFile = account.roleType.equals("ADMIN") ? "AdminSplash.fxml" : "PatientSplash.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(splashFile));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }
            
            if (account.roleType.equals("ADMIN")) {
                AdminSplashController ctrl = loader.getController();
                ctrl.startLoadingSequence(account);
            } else {
                PatientSplashController ctrl = loader.getController();
                ctrl.startLoadingSequence(account);
            }
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
