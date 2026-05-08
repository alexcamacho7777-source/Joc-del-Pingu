package vista;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * CONTROLADOR DE LA PANTALLA DE CÀRREGA.
 * MOSTRA UNA INTERFÍCIE D'ESPERA MENTRE ES PREPARA L'ENTORN DE JOC.
 */
public class PantallaCarga {

    @FXML
    private ProgressIndicator spinner;

    @FXML
    private StackPane rootPane;

    /**
     * INICIALITZA LA PANTALLA, CARREGA EL FONS VISUAL I CONFIGURA EL TEMPS D'ESPERA.
     */
    @FXML
    public void initialize() {
        try {
            // CÀRREGA DE L'IMATGE DE FONS PERSONALITZADA
            java.net.URL imageUrl = getClass().getResource("/resources/fondo_carga.png");
            if (imageUrl != null) {
                String imagePath = imageUrl.toExternalForm();
                rootPane.setStyle("-fx-background-image: url('" + imagePath + "'); -fx-background-size: cover; -fx-background-position: center;");
            }
        } catch (Exception e) {
            System.err.println("ERROR CARREGANT EL FONS DE CÀRREGA.");
        }

        // SIMULACIÓ D'UN TEMPS DE CÀRREGA DE 3 SEGONS PER MILLORAR L'UX
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> enterGame());
        delay.play();
    }

    /**
     * EFECTUA LA TRANSICIÓ FINAL CAP A LA PANTALLA DEL TAULELL DE JOC.
     */
    private void enterGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent partidaRoot = loader.load();
            Scene partidaScene = new Scene(partidaRoot);
            
            // CANVI DE SCENE AL STAGE ACTUAL
            Stage stage = (Stage) spinner.getScene().getWindow();
            stage.setScene(partidaScene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
