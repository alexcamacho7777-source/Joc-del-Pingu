package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
        String username = userField.getText().trim();
        String password = passField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "Camps buits",
                    "Introdueix l'usuari i la contrasenya per entrar.");
            return;
        }

        controlador.GestorBBDD bd = new controlador.GestorBBDD();

        if (bd.loginUsuario(username, password)) {
            System.out.println("Login correcte: " + username);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaLobby.fxml"));
                Parent pantallaLobbyRoot = loader.load();

                PantallaLobby controladorLobby = loader.getController();
                controladorLobby.setUsuarioLogueado(username);

                Scene pantallaLobbyScene = new Scene(pantallaLobbyRoot);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(pantallaLobbyScene);
                stage.setTitle("El Joc del Pingüí - Menú Principal");
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
            } catch (Exception e) {
                e.printStackTrace();
                mostrarAlert(Alert.AlertType.ERROR, "Error intern",
                        "No s'ha pogut carregar la pantalla de lobby.");
            }
        } else {
            mostrarAlert(Alert.AlertType.ERROR, "Credencials incorrectes",
                    "L'usuari o la contrasenya no és correcta. Torna-ho a intentar.");
        }
    }

    @FXML
    private void handleRegister() {
        String username = userField.getText().trim();
        String password = passField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "Camps buits",
                    "Introdueix un nom d'usuari i una contrasenya per registrar-te.");
            return;
        }

        if (password.length() < 4) {
            mostrarAlert(Alert.AlertType.WARNING, "Contrasenya massa curta",
                    "La contrasenya ha de tenir almenys 4 caràcters.");
            return;
        }

        controlador.GestorBBDD bd = new controlador.GestorBBDD();
        if (bd.registrarUsuario(username, password)) {
            mostrarAlert(Alert.AlertType.INFORMATION, "Compte creat",
                    "L'usuari '" + username + "' s'ha registrat correctament. Ja pots iniciar sessió!");
        } else {
            mostrarAlert(Alert.AlertType.ERROR, "Error de registre",
                    "L'usuari '" + username + "' ja existeix o no hi ha connexió a la base de dades.");
        }
    }

    /** Mostra un diàleg d'alerta amb el missatge indicat. */
    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        Alert alert = new Alert(tipus);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}
