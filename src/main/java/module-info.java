module com.ipes {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.ipes to javafx.fxml;
    opens com.ipes.controller to javafx.fxml;
    exports com.ipes;
    exports com.ipes.controller;
}