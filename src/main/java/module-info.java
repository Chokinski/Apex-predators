module com.jat.ctfxplotsplus {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.base;
    
    opens com.jat.ctfxplotsplus to javafx.fxml;
    exports com.jat.ctfxplotsplus;
}
