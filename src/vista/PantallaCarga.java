package vista;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PantallaCarga {

    @FXML
    private ProgressIndicator spinner;

    @FXML
    public void initialize() {
        System.out.println("Pantalla de Carga activada... simulando carga");
        
        // Simular un tiempo de carga de 3 segundos antes de iniciar el juego
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> enterGame());
        delay.play();
    }

    private void enterGame() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent partidaRoot = loader.load();
            Scene partidaScene = new Scene(partidaRoot);
            
            Stage stage = (Stage) spinner.getScene().getWindow();
            stage.setScene(partidaScene);
            stage.setMaximized(true);
        } catch (Exception e) {
            System.err.println("Error al cargar la pantalla del juego desde la pantalla de carga:");
            e.printStackTrace();
        }
    }
}
