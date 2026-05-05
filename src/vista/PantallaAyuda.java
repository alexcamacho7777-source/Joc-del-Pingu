package vista;

import javafx.fxml.FXML;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PantallaAyuda {

    @FXML private VBox rootContainer;

    @FXML
    public void initialize() {
        // Animació d'entrada
        rootContainer.setOpacity(0);
        rootContainer.setScaleX(0.7);
        rootContainer.setScaleY(0.7);

        FadeTransition fade = new FadeTransition(Duration.millis(300), rootContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), rootContainer);
        scale.setFromX(0.7);
        scale.setFromY(0.7);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    @FXML
    private void handleCerrar() {
        controlador.SoundManager.getInstance().playSound("click");
        
        FadeTransition fade = new FadeTransition(Duration.millis(200), rootContainer);
        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), rootContainer);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setByX(-0.2);
        scale.setByY(-0.2);

        fade.setOnFinished(e -> {
            Stage stage = (Stage) rootContainer.getScene().getWindow();
            stage.close();
        });

        fade.play();
        scale.play();
    }
}
