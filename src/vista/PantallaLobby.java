package vista;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

public class PantallaLobby {

    @FXML private Text welcomeText;
    @FXML private Button btnNuevaPartida;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnCerrarSesion;

    private String username;

    @FXML
    private void initialize() {
        System.out.println("PantallaLobby initialized");
    }

    public void setUsuarioLogueado(String username) {
        this.username = username;
        if (welcomeText != null) {
            welcomeText.setText("Benvingut a l'aventura, " + username + "!");
        }
    }

    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        System.out.println("Creando nueva partida para: " + username);
        lanzarJuego(event, false);
    }

    @FXML
    private void handleCargarPartida(ActionEvent event) {
        System.out.println("Cargando partida para: " + username);
        lanzarJuego(event, true);
    }

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void lanzarJuego(ActionEvent event, boolean cargarPartida) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent pantallaJuegoRoot = loader.load();
            
            PantallaJuego controladorJoc = loader.getController();
            controladorJoc.setUsuarioLogueado(username);
            
            if (cargarPartida) {
                // Aquí podrías decirle al controlador que inicie cargando la partida
                controladorJoc.iniciarCargandoPartida();
            }

            Scene pantallaJuegoScene = new Scene(pantallaJuegoRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(pantallaJuegoScene);
            stage.setTitle("El Joc del Pingüí - Partida");
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
