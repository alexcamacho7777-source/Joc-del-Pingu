package vista;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

/**
 * CONTROLADOR DE LA PANTALLA D'INTRODUCCIÓ.
 * GESTIONA LA REPRODUCCIÓ DEL VÍDEO INICIAL I LA TRANSICIÓ AL MENÚ PRINCIPAL.
 */
public class PantallaIntro {

    @FXML
    private StackPane rootPane;

    /**
     * INICIALITZA LA REPRODUCCIÓ DEL VÍDEO D'INTRODUCCIÓ.
     * SI EL VÍDEO NO ES TROBA, SALTA DIRECTAMENT AL MENÚ DE FORMA SEGURA.
     */
    @FXML
    public void initialize() {
        try {
            java.net.URL videoUrl = getClass().getResource("/resources/intro.mp4");
            
            // VALIDACIÓ DE L'EXISTÈNCIA DEL FITXER MULTIMÈDIA
            if (videoUrl != null) {
                configurarReproductor(videoUrl);
            } else {
                jumpToMenuSafe();
            }
        } catch (Exception e) {
            jumpToMenuSafe();
        }
    }

    /**
     * CONFIGURA EL REPRODUCTOR DE MITJANS I ELS EVENTS DE FINALITZACIÓ I SALT.
     */
    private void configurarReproductor(java.net.URL videoUrl) {
        Media media = new Media(videoUrl.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        // AJUST DEL VÍDEO A LES DIMENSIONS DE LA FINESTRA
        mediaView.fitWidthProperty().bind(rootPane.widthProperty());
        mediaView.fitHeightProperty().bind(rootPane.heightProperty());
        mediaView.setPreserveRatio(false); 

        rootPane.getChildren().add(mediaView);

        // OPCIÓ PER SALTAR EL VÍDEO FENT CLIC AMB EL RATOLÍ
        rootPane.setOnMouseClicked(e -> {
            mediaPlayer.stop();
            goToMenu();
        });

        // TRANSICIÓ AUTOMÀTICA EN ACABAR EL VÍDEO
        mediaPlayer.setOnEndOfMedia(() -> goToMenu());

        mediaPlayer.play();
    }

    /**
     * SALTA AL MENÚ AMB UN PETIT RETARD PER ASSEGURAR QUE LA SCENE ESTÀ PONT PER AL STAGE.
     */
    private void jumpToMenuSafe() {
        Platform.runLater(() -> {
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            pt.setOnFinished(e -> goToMenu());
            pt.play();
        });
    }

    /**
     * CARREGA LA PANTALLA DEL MENÚ PRINCIPAL.
     */
    private void goToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent menuRoot = loader.load();
            Scene menuScene = new Scene(menuRoot);
            
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("EL JOC DEL PINGÜÍ");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
