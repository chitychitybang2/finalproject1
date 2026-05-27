module com.mycompany.projectbuang {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.projectbuang to javafx.fxml;
    exports com.mycompany.projectbuang;
}
