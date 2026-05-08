package vista;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * UTILITAT PER MOSTRAR MISSATGES D'ALERTA PERSONALITZATS DINS DEL JOC.
 * UTILITZA UN SISTEMA DE SOBREPOSICIÓ (OVERLAY) SOBRE EL CONTENIDOR PRINCIPAL.
 */
public class PantallaAlerta {

    @FXML private StackPane root;
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;

    // ACCIÓ A EXECUTAR UN COP ES TANCA L'ALERTA
    private Runnable onAceptar;

    /**
     * GESTIONA L'ACCIÓ DE TANCAR L'ALERTA I EXECUTA EL CALLBACK SI EXISTEIX.
     */
    @FXML
    private void handleAceptar() {
        // ELIMINA EL NODE DE L'ALERTA DEL SEU PARE (STACKPANE)
        if (root.getParent() instanceof StackPane parent) {
            parent.getChildren().remove(root);
        }
        
        if (onAceptar != null) {
            onAceptar.run();
        }
    }

    /**
     * MÈTODE ESTÀTIC PER INVOCAR UNA ALERTA DES DE QUALSEVOL CONTROLADOR.
     * @param rootPane EL CONTENIDOR ON S'AFEGIRÀ L'OVERLAY.
     * @param titulo EL TÍTOL DE L'ALERTA.
     * @param mensaje EL COS DEL MISSATGE.
     * @param onAceptar L'ACCIÓ OPCIONAL EN CLICAR 'D'ACORD'.
     */
    public static void mostrar(StackPane rootPane, String titulo, String mensaje, Runnable onAceptar) {
        try {
            FXMLLoader loader = new FXMLLoader(PantallaAlerta.class.getResource("/resources/PantallaAlerta.fxml"));
            Parent alertNode = loader.load();
            PantallaAlerta controller = loader.getController();
            
            // CONFIGURACIÓ DE CONTINGUT (TÍTOL SEMPRE EN MAJÚSCULES)
            controller.lblTitulo.setText(titulo.toUpperCase());
            controller.lblMensaje.setText(mensaje);
            controller.onAceptar = onAceptar;
            
            rootPane.getChildren().add(alertNode);
        } catch (Exception e) {
            // SISTEMA DE SEGURETAT: SI EL FXML FALLA, ES MOSTRA UNA ALERTA ESTÀNDARD
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
            
            if (onAceptar != null) {
                onAceptar.run();
            }
        }
    }
}
