package vista;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;

public class PantallaLobby {

    @FXML private Text welcomeText;
    @FXML private ChoiceBox<Integer> choiceHumans;
    @FXML private ChoiceBox<Integer> choiceIA;
    @FXML private Button btnNuevaPartida;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnCerrarSesion;
    @FXML private ChoiceBox<Integer> choicePartidas;

    private String username;

    @FXML
    private void initialize() {
        System.out.println("PantallaLobby initialized");
        choiceHumans.getItems().addAll(1, 2, 3, 4);
        choiceIA.getItems().addAll(0, 1, 2, 3);
        choiceHumans.setValue(1); 
        choiceIA.setValue(1); 

        // Carregar llista de partides de la BBDD
        controlador.GestorBBDD bd = new controlador.GestorBBDD();
        java.util.ArrayList<Integer> partidas = bd.getListaPartidas();
        choicePartidas.getItems().addAll(partidas);
        if (!partidas.isEmpty()) {
            choicePartidas.setValue(partidas.get(0));
        }
    }

    public void setUsuarioLogueado(String username) {
        this.username = username;
        if (welcomeText != null) {
            welcomeText.setText("Benvingut a l'aventura, " + username + "!");
        }
    }

    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        // Validar máximo 4 jugadores en total si se quiere
        int total = choiceHumans.getValue() + choiceIA.getValue();
        if (total > 4) {
             System.out.println("Error: Màxim 4 pingüins totals.");
             // Podríamos mostrar un alert
        }
        lanzarJuego(event, false);
    }

    @FXML
    private void handleCargarPartida(ActionEvent event) {
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
            stage.setTitle("El Joc del Pingüí");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
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
            
            // Pasamos los recuentos elegidos
            controladorJoc.configurarJugadores(choiceHumans.getValue(), choiceIA.getValue());
            
            if (cargarPartida) {
                Integer gameId = choicePartidas.getValue();
                if (gameId != null) {
                    controladorJoc.iniciarCargandoPartida(gameId);
                } else {
                    System.out.println("No s'ha seleccionat cap partida per carregar.");
                    return; // No lancem si no hi ha selecció
                }
            }

            Scene pantallaJuegoScene = new Scene(pantallaJuegoRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(pantallaJuegoScene);
            stage.setTitle("El Joc del Pingüí - Partida");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
