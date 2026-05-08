/**
 * DEFINICIÓ DEL MÒDUL DEL PROJECTE 'EL JOC DEL PINGÜÍ'.
 * ESPECIFICA LES DEPENDÈNCIES DE JAVAFX I JDBC NECESSÀRIES PER A L'EXECUCIÓ.
 */
module Joc_del_Pingu {
    // REQUERIMENTS DE LES LLIBRERIES DE JAVAFX I SQL
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.sql;

    // OBERTURA DE PAQUETS PER A PERMETRE L'ACCÉS DEL MOTOR JAVAFX
    opens vista       to javafx.fxml, javafx.graphics;
    opens controlador to javafx.fxml, javafx.graphics;
    opens model       to javafx.base;
    opens resources   to javafx.fxml;
}