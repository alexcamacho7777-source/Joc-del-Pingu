package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;

/**
 * CONTROLADOR PER A LA PANTALLA DE DECISIÓ (DIÀLEG SÍ/NO).
 * UTILITZAT PER A INTERACCIONS QUE REQUEREIXEN L'ELECCIÓ DE L'USUARI DURANT LA PARTIDA.
 */
public class PantallaDecision {

    @FXML private StackPane root;
    @FXML private Label lblTitulo;
    @FXML private Label lblMensaje;

    // ACCIONS A EXECUTAR SEGONS LA RESPOSTA DE L'USUARI
    private Runnable onYes;
    private Runnable onNo;

    /**
     * CONFIGURA EL CONTINGUT DEL DIÀLEG I LES ACCIONS ASSOCIADES.
     * @param titulo EL TÍTOL DE LA DECISIÓ.
     * @param mensaje EL TEXT EXPLICATIU.
     * @param onYes ACCIÓ PER A LA RESPOSTA AFIRMATIVA.
     * @param onNo ACCIÓ PER A LA RESPOSTA NEGATIVA.
     */
    public void setContent(String titulo, String mensaje, Runnable onYes, Runnable onNo) {
        lblTitulo.setText(titulo.toUpperCase());
        lblMensaje.setText(mensaje);
        this.onYes = onYes;
        this.onNo = onNo;
    }

    /**
     * GESTIONA EL CLIC EN EL BOTÓ 'SÍ'.
     */
    @FXML
    private void handleSi() {
        controlador.SoundManager.getInstance().playSound("click");
        close();
        if (onYes != null) {
            onYes.run();
        }
    }

    /**
     * GESTIONA EL CLIC EN EL BOTÓ 'NO'.
     */
    @FXML
    private void handleNo() {
        controlador.SoundManager.getInstance().playSound("click");
        close();
        if (onNo != null) {
            onNo.run();
        }
    }

    /**
     * TANCA L'OVERLAY DE DECISIÓ ELIMINANT EL NODE DEL SEU CONTENIDOR PARE.
     */
    private void close() {
        if (root.getParent() instanceof Pane) {
            ((Pane) root.getParent()).getChildren().remove(root);
        }
    }
}
