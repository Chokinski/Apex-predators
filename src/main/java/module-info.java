module com.jat.ctfxplotsplus {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    opens com.jat to javafx.fxml;
    exports com.jat;
}
