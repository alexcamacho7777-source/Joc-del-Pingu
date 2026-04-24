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

public class PantallaMenu {

    @FXML private Button btnJugar;
    @FXML private Button btnUsuari;
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
        if (loggedInUser != null) {
            lblSessio.setText("Sessió iniciada com: " + loggedInUser);
            btnUsuari.setText("Tancar Sessió");
        } else {
            lblSessio.setText("");
            btnUsuari.setText("Usuari");
        }
    }

    @FXML
    private void handleJugar(ActionEvent event) {
        // Ja no és necessari estar loguejat al menú principal

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaLobby.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar la pantalla de lobby.");
        }
    }

    @FXML
    private void handleUsuari(ActionEvent event) {
        if (loggedInUser != null) {
            // Logout
            loggedInUser = null;
            initialize();
            mostrarAlert(Alert.AlertType.INFORMATION, "Sessió tancada", "Has tancat la sessió correctament.");
            return;
        }

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
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar la pantalla de login.");
        }
    }

    @FXML
    private void handleStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaEstadistiques.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlert(Alert.AlertType.ERROR, "Error", "No s'ha pogut carregar la pantalla d'estadístiques.");
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
