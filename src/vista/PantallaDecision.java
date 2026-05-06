package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;

public class PantallaDecision {

    @FXML private StackPane root;
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;

    private Runnable onYes;
    private Runnable onNo;

    public void setContent(String titulo, String mensaje, Runnable onYes, Runnable onNo) {
        lblTitulo.setText(titulo);
        lblMensaje.setText(mensaje);
        this.onYes = onYes;
        this.onNo = onNo;
    }

    @FXML
    private void handleSi() {
        controlador.SoundManager.getInstance().playSound("click");
        close();
        if (onYes != null) onYes.run();
    }

    @FXML
    private void handleNo() {
        controlador.SoundManager.getInstance().playSound("click");
        close();
        if (onNo != null) onNo.run();
    }

    private void close() {
        if (root.getParent() instanceof Pane) {
            ((Pane) root.getParent()).getChildren().remove(root);
        }
    }
}
