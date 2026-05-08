package vista;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

/**
 * CONTROLADOR DE LA PANTALLA D'ACCÉS (LOGIN) I REGISTRE D'USUARIS.
 * GESTIONA L'ENTRADA AL JOC I LA CREACIÓ DE NOUS COMPTES A LA BBDD.
 */
public class PantallaLogin {

    // CONTENIDORS VISUALS PER COMMUTAR ENTRE LOGIN I REGISTRE
    @FXML private VBox loginBox;
    @FXML private VBox registerBox;

    // CAMPS D'ENTRADA PER A L'INICI DE SESSIÓ
    @FXML private TextField logUser;
    @FXML private PasswordField logPass;

    // CAMPS D'ENTRADA PER AL REGISTRE DE NOUS USUARIS
    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private PasswordField regPassConf;
    @FXML private StackPane rootPane;

    /**
     * INICIALITZA LA PANTALLA, ACTIVA LA MÚSICA I MOSTRA EL FORMULARI DE LOGIN.
     */
    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();
        showLogin();
    }

    /**
     * FA VISIBLE EL BLOC DE LOGIN I OCULTA EL DE REGISTRE.
     */
    @FXML
    private void showLogin() {
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        registerBox.setVisible(false);
        registerBox.setManaged(false);
    }

    /**
     * FA VISIBLE EL BLOC DE REGISTRE I OCULTA EL DE LOGIN.
     */
    @FXML
    private void showRegister() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        registerBox.setVisible(true);
        registerBox.setManaged(true);
    }

    /**
     * GESTIONA L'INTENT D'INICI DE SESSIÓ.
     * COMPROVA QUE ELS CAMPS NO ESTIGUIN BUITS I VALIDA LES CREDENCIALS AMB LA BBDD.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String u = logUser.getText().trim();
        String p = logPass.getText();

        if (!u.isEmpty() && !p.isEmpty()) {
            controlador.GestorBBDD bd = new controlador.GestorBBDD();
            // VALIDACIÓ CONTRA LA BASE DE DADES
            if (bd.loginUsuario(u, p)) {
                PantallaMenu.setLoggedInUser(u);
                handleTornar(event);
            } else {
                mostrarAlert(Alert.AlertType.ERROR, "ERROR D'ACCÉS", "USUARI O CONTRASENYA INCORRECTES.");
            }
        } else {
            mostrarAlert(Alert.AlertType.WARNING, "CAMPS INCOMPLETS", "SI US PLAU, OMPLE TOTS ELS CAMPS.");
        }
    }

    /**
     * GESTIONA LA CREACIÓ D'UN NOU USUARI.
     * VALIDA LONGITUD, COINCIDÈNCIA DE CONTRASENYES I EXISTÈNCIA A LA BBDD.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        String u = regUser.getText().trim();
        String p = regPass.getText();
        String p2 = regPassConf.getText();

        // VALIDACIÓ ESTRUCTURADA DE FORMULARI
        if (u.isEmpty() || p.isEmpty() || p2.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "CAMPS BUITS", "TOTS ELS CAMPS SÓN OBLIGATORIS.");
        } else if (!p.equals(p2)) {
            mostrarAlert(Alert.AlertType.WARNING, "CONTRASENYES DIFERENTS", "LES CONTRASENYES NO COINCIDEIXEN.");
        } else if (p.length() < 4) {
            mostrarAlert(Alert.AlertType.WARNING, "CONTRASENYA DÈBIL", "LA CONTRASENYA HA DE TENIR ALMENYS 4 CARÀCTERS.");
        } else {
            controlador.GestorBBDD bd = new controlador.GestorBBDD();
            // INTENT D'INSERCIÓ A LA BASE DE DADES
            if (bd.registrarUsuario(u, p)) {
                mostrarAlert(Alert.AlertType.INFORMATION, "ÈXIT", "COMPTE CREAT CORRECTAMENT. JA POTS INICIAR SESSIÓ.");
                showLogin();
                logUser.setText(u);
                logPass.setText("");
            } else {
                mostrarAlert(Alert.AlertType.ERROR, "ERROR DE REGISTRE", "AQUEST NOM D'USUARI JA ESTÀ EN ÚS.");
            }
        }
    }

    /**
     * CARREGA LA PANTALLA DEL MENÚ PRINCIPAL I RETORNA L'USUARI ALLÀ.
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
     * MÈTODE AUXILIAR PER MOSTRAR ALERTES PERSONALITZADES A L'USUARI.
     */
    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        PantallaAlerta.mostrar(rootPane, titol, missatge, null);
    }
}
