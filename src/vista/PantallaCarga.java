package vista;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.util.Duration;

public class PantallaCarga {

    @FXML private Label spinnerIcon;
    @FXML private Label statusText;
    @FXML private ProgressBar progressBar;

    @FXML
    public void initialize() {
        // Animacion del icono dando vueltas
        RotateTransition rt = new RotateTransition(Duration.seconds(2), spinnerIcon);
        rt.setByAngle(360);
        rt.setCycleCount(Animation.INDEFINITE);
        rt.setInterpolator(Interpolator.LINEAR);
        rt.play();

        // Puedes simular carga en un hilo aparte aquí:
        /*
        new Thread(() -> {
            for(int i = 0; i <= 100; i++) {
                try { Thread.sleep(30); } catch(Exception e) {}
                double p = i / 100.0;
                javafx.application.Platform.runLater(() -> progressBar.setProgress(p));
            }
        }).start();
        */
    }
}
