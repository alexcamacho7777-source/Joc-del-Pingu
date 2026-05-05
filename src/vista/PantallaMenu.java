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

public class PantallaMenu {

    @FXML private Button btnJugar;
    @FXML private Button btnUsuari;
    @FXML private Button btnAjustes;
    @FXML private Label lblSessio;

    private static String loggedInUser = null;

    public static void setLoggedInUser(String user) {
        loggedInUser = user;
    }

    public static String getLoggedInUser() {
        return loggedInUser;
    }

    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();
        if (loggedInUser != null) {
            lblSessio.setText("Sessió iniciada com: " + loggedInUser);
            btnUsuari.setText("Tancar Sessió");
        } else {
            lblSessio.setText("");
            btnUsuari.setText("Usuari");
        }

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

    @FXML
    private void handleJugar(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        // Ja no és necessari estar loguejat al menú principal

        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaLobby.fxml");
            if (fxmlUrl == null) {
                throw new java.io.IOException("No s'ha trobat el fitxer FXML: /resources/PantallaLobby.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar la pantalla de lobby: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjustes(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        System.out.println("DEBUG: Obriu ajustes com overlay...");
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
            System.err.println("ERROR carregant Ajustes: " + e.getMessage());
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar el menú d'ajustes: " + e.getMessage());
        }
    }

    @FXML
    private void handleUsuari(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        if (loggedInUser != null) {
            // Logout
            loggedInUser = null;
            initialize();
            mostrarAlert(Alert.AlertType.INFORMATION, "Sessió tancada", "Has tancat la sessió correctament.");
            return;
        }

        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaLogin.fxml");
            if (fxmlUrl == null) {
                throw new java.io.IOException("No s'ha trobat el fitxer FXML: /resources/PantallaLogin.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar la pantalla de login: " + e.getMessage());
        }
    }

    @FXML
    private void handleStats(ActionEvent event) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaEstadistiques.fxml");
            if (fxmlUrl == null) {
                // Intentar sense la barra inicial si falla, o amb el ClassLoader
                fxmlUrl = PantallaMenu.class.getClassLoader().getResource("resources/PantallaEstadistiques.fxml");
            }
            
            if (fxmlUrl == null) {
                throw new java.io.IOException("No s'ha trobat el fitxer FXML: resources/PantallaEstadistiques.fxml");
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar la pantalla d'estadístiques: " + e.getMessage());
        }
    }

    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        Alert alert = new Alert(tipus);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}
