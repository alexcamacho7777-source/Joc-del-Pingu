module Joc_del_Pingu {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens vista       to javafx.fxml, javafx.graphics;
    opens controlador to javafx.fxml;
    opens model       to javafx.base;
}