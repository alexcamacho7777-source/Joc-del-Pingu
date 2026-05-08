package vista;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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

/**
 * CONTROLADOR DEL LOBBY DE PARTIDES.
 * PERMET VEURE LES PARTIDES GUARDADES A LA BBDD I TRIAR ENTRE
 * CONTINUAR-NE UNA O CREAR-NE UNA DE NOVA.
 */
public class PantallaLobby {

    @FXML private Text welcomeText;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnAjustes;
    @FXML private StackPane rootPane;

    // TAULA VISUAL PER MOSTRAR LES PARTIDES GUARDADES
    @FXML private TableView<Map<String, String>> tablaPartidas;
    @FXML private TableColumn<Map<String, String>, String> colId;
    @FXML private TableColumn<Map<String, String>, String> colNom;
    @FXML private TableColumn<Map<String, String>, String> colData;
    @FXML private TableColumn<Map<String, String>, String> colJugadors;
    @FXML private TableColumn<Map<String, String>, String> colTorn;
    @FXML private TableColumn<Map<String, String>, String> colFinalitzada;

    /**
     * INICIALITZA EL LOBBY, CONFIGURA LA TAULA I CARREGA LES DADES DE L'USUARI.
     */
    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();

        // SALUTACIÓ PERSONALITZADA SI L'USUARI ESTÀ LOGUEJAT
        if (PantallaMenu.getLoggedInUser() != null) {
            welcomeText.setText("BENVINGUT A L'AVENTURA, " + PantallaMenu.getLoggedInUser().toUpperCase() + "!");
        }

        configurarTabla();
        cargarDatosTabla();

        // ANIMACIÓ DE ROTACIÓ PER AL BOTÓ D'AJUSTAMENTS
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

    /**
     * DEFINEIX COM ES MOSTRARAN LES COLUMNES DE LA TAULA SEGONS EL MAPA DE DADES.
     */
    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("ID_PARTIDA")));
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("NOM_PARTIDA")));
        colData.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("DATA_CREACIO")));
        colJugadors.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("JUGADORS")));
        colTorn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("TORN_ACTUAL")));
        colFinalitzada.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get("FINALITZADA")));
    }

    /**
     * RECUPERA LA LLISTA DE PARTIDES DES DE LA BBDD I LES INSEREIX A LA TAULA.
     */
    private void cargarDatosTabla() {
        controlador.GestorBBDD bd = new controlador.GestorBBDD();
        ArrayList<LinkedHashMap<String, String>> data = bd.getListaPartidasDetalladas();
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList(data);
        tablaPartidas.setItems(items);
    }

    /**
     * OBRE EL PANNELL D'AJUSTAMENTS COM UNA FINESTRA MODAL.
     */
    @FXML
    private void handleAjustes(ActionEvent event) {
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
            mostrarAlert(Alert.AlertType.ERROR, "ERROR", "NO S'HA POGUT CARREGAR ELS AJUSTAMENTS.");
        }
    }

    /**
     * REDIRIGEIX A LA PANTALLA DE CREACIÓ DE NOVA PARTIDA.
     * REQUEREIX QUE L'USUARI ESTIGUI LOGUEJAT.
     */
    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        if (PantallaMenu.getLoggedInUser() == null) {
            PantallaAlerta.mostrar(rootPane, "SESSIÓ NO INICIADA", "HAS D'INICIAR SESSIÓ PER CREAR UNA PARTIDA.", () -> {
                redirigirALogin(event);
            });
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaCrearPartida.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setFullScreen(true);
            } catch (Exception e) {
                mostrarAlert(Alert.AlertType.ERROR, "ERROR", "NO S'HA POGUT OBRIR EL MENÚ DE CREACIÓ.");
            }
        }
    }

    /**
     * CARREGA LA PARTIDA SELECCIONADA A LA TAULA.
     * REQUEREIX SESSIÓ INICIADA I UNA SELECCIÓ VÀLIDA.
     */
    @FXML
    private void handleCargarPartida(ActionEvent event) {
        if (PantallaMenu.getLoggedInUser() == null) {
            PantallaAlerta.mostrar(rootPane, "SESSIÓ NO INICIADA", "HAS D'INICIAR SESSIÓ PER CARREGAR UNA PARTIDA.", () -> {
                redirigirALogin(event);
            });
        } else {
            Map<String, String> seleccionada = tablaPartidas.getSelectionModel().getSelectedItem();
            if (seleccionada == null) {
                mostrarAlert(Alert.AlertType.WARNING, "SELECCIÓ BUIDA", "SI US PLAU, TRIA UNA PARTIDA DE LA TAULA.");
            } else {
                Integer gameId = Integer.parseInt(seleccionada.get("ID_PARTIDA"));
                lanzarJuego(event, gameId);
            }
        }
    }

    /**
     * RETORNA L'USUARI AL MENÚ PRINCIPAL.
     */
    @FXML
    private void handleTornar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
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

    /**
     * INICIALITZA EL TAULELL DE JOC AMB LES DADES DE LA PARTIDA CARREGADA.
     */
    private void lanzarJuego(ActionEvent event, Integer gameIdToLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent pantallaJuegoRoot = loader.load();
            
            PantallaJuego controladorJoc = loader.getController();
            
            if (gameIdToLoad != null) {
                controladorJoc.iniciarCargandoPartida(gameIdToLoad);
            }

            Scene pantallaJuegoScene = new Scene(pantallaJuegoRoot);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(pantallaJuegoScene);
            stage.setTitle("EL JOC DEL PINGÜÍ - PARTIDA");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            mostrarAlert(Alert.AlertType.ERROR, "ERROR", "NO S'HA POGUT INICIAR EL JOC.");
        }
    }

    /**
     * MÈTODE PER MOSTRAR ALERTES PERSONALITZADES.
     */
    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        PantallaAlerta.mostrar(rootPane, titol, missatge, null);
    }

    /**
     * REDIRIGEIX L'USUARI A LA PANTALLA DE LOGIN.
     */
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
