package vista;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class PantallaLogin {

    @FXML private VBox loginBox;
    @FXML private VBox registerBox;

    @FXML private TextField logUser;
    @FXML private PasswordField logPass;

    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private PasswordField regPassConf;

    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();
        showLogin();
    }

    @FXML
    private void showLogin() {
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        registerBox.setVisible(false);
        registerBox.setManaged(false);
    }

    @FXML
    private void showRegister() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        registerBox.setVisible(true);
        registerBox.setManaged(true);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String u = logUser.getText().trim();
        String p = logPass.getText();

        if (u.isEmpty() || p.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "Camps buits", "Introdueix usuari i contrasenya.");
            return;
        }

        controlador.GestorBBDD bd = new controlador.GestorBBDD();
        if (bd.loginUsuario(u, p)) {
            PantallaMenu.setLoggedInUser(u);
            handleTornar(event);
        } else {
            mostrarAlert(Alert.AlertType.ERROR, "Credencials incorrectes", "Usuari o contrasenya no vàlids.");
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String u = regUser.getText().trim();
        String p = regPass.getText();
        String p2 = regPassConf.getText();

        if (u.isEmpty() || p.isEmpty() || p2.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "Camps buits", "Tots els camps són obligatoris.");
            return;
        }

        if (!p.equals(p2)) {
            mostrarAlert(Alert.AlertType.WARNING, "Error de contrasenya", "Les contrasenyes no coincideixen.");
            return;
        }

        if (p.length() < 4) {
            mostrarAlert(Alert.AlertType.WARNING, "Contrasenya massa curta", "Mínim 4 caràcters.");
            return;
        }

        controlador.GestorBBDD bd = new controlador.GestorBBDD();
        if (bd.registrarUsuario(u, p)) {
            mostrarAlert(Alert.AlertType.INFORMATION, "Registre complet", "Usuari creat. Ara pots iniciar sessió.");
            showLogin();
            logUser.setText(u);
            logPass.setText("");
        } else {
            mostrarAlert(Alert.AlertType.ERROR, "Error de registre", "No s'ha pogut registrar. Potser l'usuari ja existeix.");
        }
    }

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

    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        Alert alert = new Alert(tipus);
        alert.setTitle(titol);
        alert.setHeaderText(null);
        alert.setContentText(missatge);
        alert.showAndWait();
    }
}
