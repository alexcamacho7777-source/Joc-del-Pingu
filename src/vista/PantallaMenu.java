package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.animation.RotateTransition;
import javafx.util.Duration;
import javafx.scene.layout.StackPane;

/**
 * CONTROLADOR DEL MENÚ PRINCIPAL DEL JOC.
 * PERMET L'ACCÉS AL LOBBY, ESTADÍSTIQUES, AJUSTAMENTS I GESTIÓ D'USUARI.
 */
public class PantallaMenu {

    // BOTONS PRINCIPALS DE LA INTERFÍCIE
    @FXML private Button btnJugar;
    @FXML private Button btnUsuari;
    @FXML private Button btnAjustes;
    @FXML private Label lblSessio;
    @FXML private javafx.scene.layout.StackPane rootPane;

    // VARIABLE ESTÀTICA PER MANTENIR L'USUARI LOGUEJAT DURANT LA SESSIÓ
    private static String loggedInUser = null;

    /**
     * ESTABLEIX L'USUARI QUE HA INICIAT SESSIÓ.
     */
    public static void setLoggedInUser(String user) {
        loggedInUser = user;
    }

    /**
     * RETORNA EL NOM DE L'USUARI ACTUAL.
     */
    public static String getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * INICIALITZA EL MENÚ, MOSTRA L'ESTAT DE LA SESSIÓ I CONFIGURA LES ANIMACIONS.
     */
    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();
        
        // ACTUALITZACIÓ DEL TEXT DE BENVINGUDA SEGONS L'USUARI
        if (loggedInUser != null) {
            lblSessio.setText("SESSIÓ INICIADA COM: " + loggedInUser.toUpperCase());
            btnUsuari.setText("TANCAR SESSIÓ");
        } else {
            lblSessio.setText("USUARI NO REGISTRAT");
            btnUsuari.setText("INICIAR SESSIÓ");
        }

        // ANIMACIÓ DE ROTACIÓ PER AL BOTÓ D'AJUSTAMENTS (AL PASSAR EL RATOLÍ)
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
     * CANVIA A LA PANTALLA DEL LOBBY PER COMENÇAR O CARREGAR UNA PARTIDA.
     */
    @FXML
    private void handleJugar(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaLobby.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            mostrarAlert(Alert.AlertType.ERROR, "ERROR DE NAVEGACIÓ", "NO S'HA POGUT CARREGAR EL LOBBY.");
        }
    }

    /**
     * OBRE EL PANNELL D'AJUSTAMENTS (SÓ, MÚSICA, ETC.) COM UNA FINESTRA MODAL.
     */
    @FXML
    private void handleAjustes(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
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
     * GESTIONA EL LOGIN O LOGOUT DE L'USUARI SEGONS L'ESTAT ACTUAL.
     */
    @FXML
    private void handleUsuari(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        if (loggedInUser != null) {
            // TANCAMENT DE SESSIÓ
            loggedInUser = null;
            initialize();
            mostrarAlert(Alert.AlertType.INFORMATION, "SESSIÓ TANCADA", "HAS SORTIT CORRECTAMENT.");
        } else {
            // NAVEGACIÓ AL LOGIN
            try {
                java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaLogin.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setFullScreen(true);
            } catch (Exception e) {
                mostrarAlert(Alert.AlertType.ERROR, "ERROR", "NO S'HA POGUT CARREGAR EL LOGIN.");
            }
        }
    }

    /**
     * CANVIA A LA PANTALLA D'ESTADÍSTIQUES PER VEURE EL RÀNQUING I LES DADES GLOBALS.
     */
    @FXML
    private void handleStats(ActionEvent event) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaEstadistiques.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace(); // Imprimeix l'error a la consola per a diagnòstic
            mostrarAlert(Alert.AlertType.ERROR, "ERROR", "NO S'HA POGUT CARREGAR LES ESTADÍSTIQUES.");
        }
    }

    /**
     * MÈTODE PER MOSTRAR ALERTES PERSONALITZADES USANT EL SISTEMA DE LA PANTALLA D'ALERTA.
     */
    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        PantallaAlerta.mostrar(rootPane, titol, missatge, null);
    }
}
