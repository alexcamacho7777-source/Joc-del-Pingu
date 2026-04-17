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

public class PantallaIntro {

    @FXML
    private StackPane rootPane;

    @FXML
    public void initialize() {
        System.out.println("PantallaIntro activada, comprobando intro.mp4...");
        try {
            java.net.URL videoUrl = getClass().getResource("/resources/intro.mp4");
            if (videoUrl != null) {
                Media media = new Media(videoUrl.toExternalForm());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                MediaView mediaView = new MediaView(mediaPlayer);

                mediaView.fitWidthProperty().bind(rootPane.widthProperty());
                mediaView.fitHeightProperty().bind(rootPane.heightProperty());
                mediaView.setPreserveRatio(false); // Estirar para llenar la pantalla

                rootPane.getChildren().add(mediaView);

                // Al tocar la pantalla se salta la intro
                rootPane.setOnMouseClicked(e -> {
                    mediaPlayer.stop();
                    goToMenu();
                });

                // Al finalizar el video se va al menú
                mediaPlayer.setOnEndOfMedia(() -> goToMenu());

                mediaPlayer.play();
            } else {
                System.out.println("No se ha encontrado /resources/intro.mp4, saltando directo al menú...");
                jumpToMenuSafe();
            }
        } catch (Exception e) {
            System.err.println("Error al reproducir el video de intro: " + e.getMessage());
            jumpToMenuSafe();
        }
    }

    private void jumpToMenuSafe() {
        // Necesario un pequeño retardo controlado porque la Scene puede no estar asignada al Stage todavía durante el init
        Platform.runLater(() -> {
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            pt.setOnFinished(e -> goToMenu());
            pt.play();
        });
    }

    private void goToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent menuRoot = loader.load();
            Scene menuScene = new Scene(menuRoot);
            
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(menuScene);
            stage.setTitle("El Juego del Pingüino");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            System.err.println("Error al cargar PantallaMenu desde intro:");
            e.printStackTrace();
        }
    }
}
