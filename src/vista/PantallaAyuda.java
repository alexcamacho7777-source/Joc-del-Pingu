package vista;

import javafx.fxml.FXML;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * CONTROLADOR DE LA PANTALLA D'AJUDA I INSTRUCCIONS.
 * MOSTRA EL MANUAL DE JOC AMB ANIMACIONS DE TRANSICIÓ SUAUS.
 */
public class PantallaAyuda {

    @FXML private VBox rootContainer;

    /**
     * INICIALITZA LA PANTALLA AMB UNA ANIMACIÓ D'ENTRADA (EFECTE POP-UP).
     */
    @FXML
    public void initialize() {
        // CONFIGURACIÓ DE L'ESTAT INICIAL PER A L'ANIMACIÓ
        rootContainer.setOpacity(0);
        rootContainer.setScaleX(0.7);
        rootContainer.setScaleY(0.7);

        FadeTransition fade = new FadeTransition(Duration.millis(300), rootContainer);
        fade.setFromValue(0); fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), rootContainer);
        scale.setFromX(0.7); scale.setFromY(0.7);
        scale.setToX(1.0); scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    /**
     * TANCA LA GUIA D'AJUDA APLICANT UNA ANIMACIÓ DE SORTIDA I ALLIBERANT LA FINESTRA.
     */
    @FXML
    private void handleCerrar() {
        controlador.SoundManager.getInstance().playSound("click");
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), rootContainer);
        fade.setFromValue(1); fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), rootContainer);
        scale.setFromX(1.0); scale.setFromY(1.0);
        scale.setToX(0.8); scale.setToY(0.8);

        // TANCAMENT DE LA FINESTRA MODAL UN COP ACABADA L'ANIMACIÓ
        fade.setOnFinished(e -> {
            Stage stage = (Stage) rootContainer.getScene().getWindow();
            stage.close();
        });

        fade.play();
        scale.play();
    }
}
