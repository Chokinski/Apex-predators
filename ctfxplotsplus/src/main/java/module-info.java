module com.jat.ctfxplotsplus {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.base;
    opens com.jat to javafx.fxml;
    exports com.jat;
}
