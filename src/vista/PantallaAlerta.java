package vista;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class PantallaAlerta {

    @FXML private StackPane root;
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;

    private Runnable onAceptar;

    @FXML
    private void handleAceptar() {
        if (root.getParent() instanceof StackPane parent) {
            parent.getChildren().remove(root);
        }
        if (onAceptar != null) {
            onAceptar.run();
        }
    }

    public static void mostrar(StackPane rootPane, String titulo, String mensaje, Runnable onAceptar) {
        try {
            FXMLLoader loader = new FXMLLoader(PantallaAlerta.class.getResource("/resources/PantallaAlerta.fxml"));
            Parent alertNode = loader.load();
            PantallaAlerta controller = loader.getController();
            
            controller.lblTitulo.setText(titulo.toUpperCase());
            controller.lblMensaje.setText(mensaje);
            controller.onAceptar = onAceptar;
            
            rootPane.getChildren().add(alertNode);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback a Alert estándar si falla la carga
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
            if (onAceptar != null) onAceptar.run();
        }
    }
}
