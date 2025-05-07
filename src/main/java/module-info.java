module com.ipes {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ipes to javafx.fxml;
    exports com.ipes;
}
