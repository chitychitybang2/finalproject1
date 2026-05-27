package com.mycompany.projectbuang;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
public class AtikatikController {
    @FXML private Label lblRosterTitle, lblPatientName, lblPatientMeta, lblHeartRate, lblTemperature, lblOxygen, lblHeartRateStatus;
    @FXML private Label lblDoctorName, lblDoctorRole;
    @FXML private ListView<String> patientListView;
    @FXML private VBox vboxNotesContainer, sidePanel;
    @FXML private TextArea txtNewNoteInput;
    private MockDatabase.UserAccount currentUser;
    private MockDatabase.PatientProfile selectedPatientProfile;
    public void setSessionContext(MockDatabase.UserAccount user) {
        this.currentUser = user;
        
        lblDoctorName.setText(user.fullName);
        lblDoctorRole.setText(user.roleTitle + " (" + user.roleType + ")");
        
        populateNavigationRoster();
    }
    private void populateNavigationRoster() {
        ObservableList<String> visibleItems = FXCollections.observableArrayList();
        if (currentUser.roleType.equals("ADMIN")) {
            lblRosterTitle.setText("GLOBAL SYSTEMS CONTROL");
            // Admin sees every patient inside tracking structures
            visibleItems.addAll(MockDatabase.patientDatabase.keySet());
            visibleItems.add("SYSTEM AUDIT SYSTEM LOGS"); 
        } else {
            lblRosterTitle.setText("MY USER PROFILE LOG");
            // Patient view limits list to their own tracking index row entry line
            visibleItems.add(currentUser.fullName);
        }
        patientListView.setItems(visibleItems);
        patientListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleRosterSelection(newValue)
        );
        // Auto-select initial record element context row target automatically
        if (!visibleItems.isEmpty()) {
            patientListView.getSelectionModel().selectFirst();
        }
    }
    private void handleRosterSelection(String selection) {
        if (selection == null) return;
        vboxNotesContainer.getChildren().clear();
        if (selection.equals("SYSTEM AUDIT SYSTEM LOGS")) {
            loadGlobalSystemDiagnosticConsole();
            return;
        }
        selectedPatientProfile = MockDatabase.patientDatabase.get(selection);
        
        // Disable note creation if the context identity token belongs to a Patient
        txtNewNoteInput.setDisable(currentUser.roleType.equals("PATIENT"));
        if (selectedPatientProfile != null) {
            lblPatientName.setText(selectedPatientProfile.name);
            lblPatientMeta.setText(selectedPatientProfile.meta + " | Encrypted Core File Record Target Verified");
            
            lblHeartRate.setText(selectedPatientProfile.heartRate);
            lblTemperature.setText(selectedPatientProfile.temperature);
            lblOxygen.setText(selectedPatientProfile.oxygen);
            lblHeartRateStatus.setText("Real-time metric telemetry verification active.");
            for (String note : selectedPatientProfile.clinicalNotes) {
                vboxNotesContainer.getChildren().add(createNoteBubble(note, "#E5E5EA", "#1D1D1F"));
            }
        }
    }
    private void loadGlobalSystemDiagnosticConsole() {
        selectedPatientProfile = null; 
        lblPatientName.setText("System Operations Audit Log");
        lblPatientMeta.setText("Root operational dashboard showing raw terminal interaction matrices.");
        
        lblHeartRate.setText("OK");
        lblTemperature.setText("SEC");
        lblOxygen.setText("100");
        lblHeartRateStatus.setText("All tracking systems normal.");
        txtNewNoteInput.setDisable(true); 
        for (String log : MockDatabase.globalAuditLogs) {
            vboxNotesContainer.getChildren().add(createNoteBubble(log, "#E0F2FE", "#0369A1"));
        }
    }
    @FXML
    private void handleAddNewNote() {
        String input = txtNewNoteInput.getText().trim();
        if (input.isEmpty() || currentUser.roleType.equals("PATIENT")) return;
        if (selectedPatientProfile != null) {
            String formattedMessage = "[ADMIN OVERRIDE - " + currentUser.fullName + "]: " + input;
            
            selectedPatientProfile.clinicalNotes.add(formattedMessage);
            MockDatabase.logActivity("Override note entry pinned to " + selectedPatientProfile.name + " by manager key: " + currentUser.username);
            MockDatabase.saveDatabase();
            
            vboxNotesContainer.getChildren().add(createNoteBubble(formattedMessage, "#E5E5EA", "#1D1D1F"));
            txtNewNoteInput.clear();
        }
    }
    private Label createNoteBubble(String message, String bgColor, String textColor) {
        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor + "; -fx-background-radius: 8; -fx-font-size: 13px;");
        label.setPadding(new Insets(8, 12, 8, 12));
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }
    @FXML 
    private void handleToggleSidebar() {
        sidePanel.setVisible(!sidePanel.isVisible());
        sidePanel.setManaged(sidePanel.isVisible());
    }
}
