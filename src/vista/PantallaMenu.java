package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class PantallaMenu {

    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    @FXML private TextField userField;
    @FXML private PasswordField passField;

    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    private void initialize() {
        System.out.println("PantallaMenu initialized");
    }

    @FXML
    private void handleNewGame() {
        System.out.println("New Game clicked");
        // TODO
    }

    @FXML
    private void handleSaveGame() {
        System.out.println("Save Game clicked");
        // TODO
    }

    @FXML
    private void handleLoadGame() {
        System.out.println("Load Game clicked");
        // TODO
    }

    @FXML
    private void handleQuitGame() {
        System.out.println("Quit Game clicked");
        System.exit(0);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText();
        String password = passField.getText();

        System.out.println("Login: " + username + " / " + password);

        if (!username.isEmpty() && !password.isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaCarga.fxml"));
                Parent pantallaCargaRoot = loader.load();
                Scene pantallaCargaScene = new Scene(pantallaCargaRoot);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(pantallaCargaScene);
                stage.setTitle("El Joc del Pingüí - Cargando...");
                // No lo maximizamos aquí directamente, dejamos que lo haga la carga si quieres, o podemos maximizar
                stage.setMaximized(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Introdueix usuari i contrasenya.");
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("Register pressed");
        // TODO
    }
}
