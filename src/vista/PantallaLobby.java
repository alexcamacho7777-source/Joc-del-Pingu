package vista;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.animation.RotateTransition;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PantallaLobby {

    @FXML private Text welcomeText;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnAjustes;


    @FXML private TableView<Map<String, String>> tablaPartidas;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colNom;
    @FXML private TableColumn<Map<String, String>, String> colData;
    @FXML private TableColumn<Map<String, String>, String> colJugadors;
    @FXML private TableColumn<Map<String, String>, String> colTorn;
    @FXML private TableColumn<Map<String, String>, String> colFinalitzada;

    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();
        System.out.println("PantallaLobby initialized");

        if (PantallaMenu.getLoggedInUser() != null) {
            welcomeText.setText("Benvingut a l'aventura, " + PantallaMenu.getLoggedInUser() + "!");
        }

        configurarTabla();
        cargarDatosTabla();

        // Animació de rotació per al botó d'ajustes
        if (btnAjustes != null) {
            RotateTransition rt = new RotateTransition(Duration.millis(1000), btnAjustes);
            rt.setByAngle(360);
            rt.setCycleCount(RotateTransition.INDEFINITE);
            rt.setInterpolator(javafx.animation.Interpolator.LINEAR);

            btnAjustes.setOnMouseEntered(e -> rt.play());
            btnAjustes.setOnMouseExited(e -> {
                rt.stop();
                btnAjustes.setRotate(0);
            });
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("ID_PARTIDA")));
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("NOM_PARTIDA")));
        colData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("DATA_CREACIO")));
        colJugadors.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("JUGADORS")));
        colTorn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("TORN_ACTUAL")));
        colFinalitzada.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("FINALITZADA")));
    }

    private void cargarDatosTabla() {
        controlador.GestorBBDD bd = new controlador.GestorBBDD();
        ArrayList<LinkedHashMap<String, String>> data = bd.getListaPartidasDetalladas();
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList(data);
        tablaPartidas.setItems(items);
    }

    @FXML
    private void handleAjustes(ActionEvent event) {
        System.out.println("DEBUG: Obriu ajustes com overlay dende Lobby...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaAjustes.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            if (event.getSource() instanceof Node node) {
                stage.initOwner(node.getScene().getWindow());
            }

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("ERROR carregant Ajustes dende Lobby: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar el menú d'ajustes: " + e.getMessage());
        }
    }

    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        if (PantallaMenu.getLoggedInUser() == null) {
            mostrarAlert(Alert.AlertType.WARNING, "Sessió no iniciada", "Has d'iniciar sessió per crear una nova partida.");
            redirigirALogin(event);
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaCrearPartida.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut obrir el menú de creació.");
        }
    }

    @FXML
    private void handleCargarPartida(ActionEvent event) {
        if (PantallaMenu.getLoggedInUser() == null) {
            mostrarAlert(Alert.AlertType.WARNING, "Sessió no iniciada", "Has d'iniciar sessió per carregar una partida.");
            redirigirALogin(event);
            return;
        }
        Map<String, String> seleccionada = tablaPartidas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlert(Alert.AlertType.WARNING, "Cap partida seleccionada", "Si us plau, selecciona una partida de la taula per carregar.");
            return;
        }
        Integer gameId = Integer.parseInt(seleccionada.get("ID_PARTIDA"));
        lanzarJuego(event, gameId);
    }

    @FXML
    private void handleTornar(ActionEvent event) {
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

    private void lanzarJuego(ActionEvent event, Integer gameIdToLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent pantallaJuegoRoot = loader.load();
            
            PantallaJuego controladorJoc = loader.getController();
            controladorJoc.setUsuarioLogueado(PantallaMenu.getLoggedInUser());
            
            if (gameIdToLoad != null) {
                controladorJoc.iniciarCargandoPartida(gameIdToLoad);
            }

            Scene pantallaJuegoScene = new Scene(pantallaJuegoRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(pantallaJuegoScene);
            stage.setTitle("El Joc del Pingüí - Partida");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut iniciar el joc.");
        }
    }

    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        Alert alert = new Alert(tipus);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }

    public void setUsuarioLogueado(String username) {
        // Left for backwards compatibility if needed, but we now use static PantallaMenu.getLoggedInUser()
        if (username != null && welcomeText != null) {
            welcomeText.setText("Benvingut a l'aventura, " + username + "!");
        }
    }

    private void redirigirALogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaLogin.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
