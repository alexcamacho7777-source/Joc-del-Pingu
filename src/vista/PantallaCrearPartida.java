package vista;

import controlador.GestorBBDD;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Foca;
import model.Jugador;
import model.Pinguino;

import java.util.ArrayList;
import java.util.List;

/**
 * CONTROLADOR PER A LA CONFIGURACIÓ DE NOVES PARTIDES.
 * PERMET DEFINIR EL NOM DE LA PARTIDA I AFEGIR FINS A 4 JUGADORS (HUMANS, IA O FOCA).
 */
public class PantallaCrearPartida {

    @FXML private TextField txtNomPartida;
    @FXML private FlowPane flowJugadores;
    @FXML private Button btnAddPlayer;
    @FXML private StackPane rootPane;

    // LLISTA PER GESTIONAR LES TARGETES DE CONFIGURACIÓ DE CADA JUGADOR
    private List<PlayerCard> activeCards = new ArrayList<>();
    private static final int MAX_JUGADORES = 4;

    /**
     * INICIALITZA LA PANTALLA I AFEGEIX AUTOMÀTICAMENT EL JUGADOR LOGUEJAT COM A PRIMER INTEGRANT.
     */
    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playMenuMusic();
        handleAddPlayer();
        if (!activeCards.isEmpty()) {
            PlayerCard firstCard = activeCards.get(0);
            String loggedIn = PantallaMenu.getLoggedInUser();
            if (loggedIn != null) {
                firstCard.txtUsername.setText(loggedIn);
                firstCard.comboTipo.setValue("Pinguí");
            }
        }
    }

    /**
     * AFEGEIX UNA NOVA TARGETA DE JUGADOR AL CONTENIDOR VISUAL.
     */
    @FXML
    private void handleAddPlayer() {
        if (activeCards.size() >= MAX_JUGADORES) {
            mostrarAlert(Alert.AlertType.WARNING, "LÍMIT ASSOLIT", "EL MÀXIM DE JUGADORS ÉS " + MAX_JUGADORES + ".");
        } else {
            PlayerCard card = new PlayerCard();
            activeCards.add(card);
            flowJugadores.getChildren().add(card.getPane());

            if (activeCards.size() >= MAX_JUGADORES) {
                btnAddPlayer.setDisable(true);
            }
        }
    }

    /**
     * TORNA A LA PANTALLA DEL LOBBY.
     */
    @FXML
    private void handleTornar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaLobby.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * VALIDA TOTA LA CONFIGURACIÓ I INICIA LA PARTIDA SI NO HI HA ERRORS.
     */
    @FXML
    private void handleComencar(ActionEvent event) {
        String nomPartida = txtNomPartida.getText().trim();
        
        // VALIDACIÓ ESTRUCTURADA SENSE RETURNS PREMATURS
        if (nomPartida.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "NOM BUIT", "SI US PLAU, POSA UN NOM A LA PARTIDA.");
        } else if (activeCards.size() < 2) {
            mostrarAlert(Alert.AlertType.WARNING, "FALTEN JUGADORS", "CALEN ALMENYS 2 JUGADORS PER JUGAR.");
        } else {
            List<Jugador> jugadoresFinales = new ArrayList<>();
            GestorBBDD db = new GestorBBDD();
            boolean errorValidacion = false;

            // RECORREGUT PER CADA TARGETA PER VALIDAR USUARIS I CONTRASENYES
            for (int i = 0; i < activeCards.size() && !errorValidacion; i++) {
                PlayerCard card = activeCards.get(i);
                String tipo = card.comboTipo.getValue();
                
                if (tipo.equals("Pinguí")) {
                    String user = card.txtUsername.getText().trim();
                    String pass = card.txtPassword.getText();
                    boolean esIA = card.comboMode.getValue().equals("IA (Bot)");
                    
                    if (user.isEmpty()) {
                        mostrarAlert(Alert.AlertType.ERROR, "NOM REQUERIT", "CADA PINGÜÍ HA DE TENIR UN NOM.");
                        errorValidacion = true;
                    } else {
                        if (!esIA) {
                            // VALIDACIÓ D'USUARI REAL A LA BBDD
                            if (db.getIDJugador(user) == -1) {
                                mostrarAlert(Alert.AlertType.ERROR, "USUARI INEXISTENT", "L'USUARI '" + user + "' NO ESTÀ REGISTRAT.");
                                errorValidacion = true;
                            } else if (!db.loginUsuario(user, pass)) {
                                mostrarAlert(Alert.AlertType.ERROR, "CONTRASENYA INCORRECTA", "LA CONTRASENYA PER A '" + user + "' NO ÉS VÀLIDA.");
                                errorValidacion = true;
                            }
                        }

                        if (!errorValidacion) {
                            Pinguino p = new Pinguino(user, card.comboColor.getValue(), 0, new model.Inventario());
                            p.setEsIA(esIA);
                            jugadoresFinales.add(p);
                        }
                    }
                } else {
                    Foca f = new Foca();
                    jugadoresFinales.add(f);
                }
            }

            if (!errorValidacion) {
                lanzarJuego(event, nomPartida, jugadoresFinales);
            }
        }
    }

    /**
     * TRANSMET LA CONFIGURACIÓ AL TAULELL I INICIA LA PANTALLA DE JOC.
     */
    private void lanzarJuego(ActionEvent event, String nomPartida, List<Jugador> jugadores) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent root = loader.load();
            PantallaJuego controladorJoc = loader.getController();
            controladorJoc.iniciarPartidaPersonalizada(nomPartida, jugadores);

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            mostrarAlert(Alert.AlertType.ERROR, "ERROR", "NO S'HA POGUT INICIAR EL JOC.");
        }
    }

    /**
     * MOSTRA UNA ALERTA D'ERROR O ADVERTIMENT.
     */
    private void mostrarAlert(Alert.AlertType tipus, String titol, String missatge) {
        PantallaAlerta.mostrar(rootPane, titol, missatge, null);
    }

    /**
     * CLASSE INTERNA PER GESTIONAR LA INTERFÍCIE VISUAL DE CADA TARGETA DE JUGADOR.
     */
    private class PlayerCard {
        private VBox pane;
        private ChoiceBox<String> comboTipo;
        private TextField txtUsername;
        private PasswordField txtPassword;
        private ChoiceBox<String> comboColor;
        private ChoiceBox<String> comboMode;
        private VBox pinguinoOptions;
        private Label lblPass;

        /**
         * CONSTRUCTOR QUE DIBUIXA LA TARGETA I CONFIGURA ELS EVENTS DE VALIDACIÓ.
         */
        public PlayerCard() {
            pane = new VBox(10);
            pane.getStyleClass().add("ice-card");
            pane.setPrefWidth(220);
            pane.setPadding(new Insets(15));
            pane.setAlignment(Pos.TOP_CENTER);

            Label lblTitle = new Label("JUGADOR " + (activeCards.size() + 1));
            lblTitle.getStyleClass().add("field-label");

            comboTipo = new ChoiceBox<>();
            comboTipo.getItems().addAll("Pinguí", "Foca");
            comboTipo.setValue("Pinguí");
            comboTipo.setPrefWidth(180);

            pinguinoOptions = new VBox(8);
            txtUsername = new TextField();
            txtUsername.setPromptText("USUARI...");
            
            txtPassword = new PasswordField();
            txtPassword.setPromptText("CONTRASENYA...");
            txtPassword.setManaged(false);
            txtPassword.setVisible(false);

            lblPass = new Label("CONTRASENYA");
            lblPass.setManaged(false);
            lblPass.setVisible(false);
            lblPass.setStyle("-fx-font-size: 10px; -fx-text-fill: #a2c1ff;");

            // VALIDACIÓ EN TEMPS REAL: CANVIA EL COLOR DE LA TARGETA SI L'USUARI EXISTEIX
            txtUsername.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean isIA = comboMode.getValue().equals("IA (Bot)");
                if (isIA) {
                    txtUsername.setStyle("-fx-border-color: #3498db; -fx-border-width: 2;");
                    ocultarPassword();
                } else if (newVal.isEmpty()) {
                    txtUsername.setStyle("-fx-border-color: rgba(255,255,255,0.2);");
                    ocultarPassword();
                } else {
                    GestorBBDD db = new GestorBBDD();
                    boolean exists = db.getIDJugador(newVal.trim()) != -1;
                    if (exists) {
                        txtUsername.setStyle("-fx-border-color: #2ecc71; -fx-border-width: 2;");
                        mostrarPassword(lblPass);
                    } else {
                        txtUsername.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
                        ocultarPassword();
                    }
                }
            });

            comboColor = new ChoiceBox<>();
            comboColor.getItems().addAll("Blau", "Vermell", "Verd", "Groc", "Rosa", "Cian");
            comboColor.setValue("Blau");
            comboColor.setPrefWidth(180);

            comboMode = new ChoiceBox<>();
            comboMode.getItems().addAll("Jugador Real", "IA (Bot)");
            comboMode.setValue("Jugador Real");
            comboMode.setPrefWidth(180);


            // CANVI DE MODE (HUMÀ VS IA)
            comboMode.setOnAction(e -> {
                boolean isIA = comboMode.getValue().equals("IA (Bot)");
                txtPassword.setVisible(!isIA);
                txtPassword.setManaged(!isIA);
                lblPass.setVisible(!isIA);
                lblPass.setManaged(!isIA);
                if (isIA) {
                    txtUsername.setText("BOT " + (activeCards.indexOf(this) + 1));
                    txtUsername.setStyle("-fx-border-color: #3498db; -fx-border-width: 2;");
                } else {
                    txtUsername.setText("");
                    txtUsername.setStyle("-fx-border-color: rgba(255,255,255,0.2);");
                }
            });

            pinguinoOptions.getChildren().addAll(new Label("JUGADOR"), comboMode, new Label("NOM"), txtUsername, lblPass, txtPassword, new Label("COLOR"), comboColor);

            // ACCIÓ DE CANVI DE TIPUS (PINGÜÍ O FOCA)
            comboTipo.setOnAction(e -> {
                pinguinoOptions.setVisible(comboTipo.getValue().equals("Pinguí"));
                pinguinoOptions.setManaged(comboTipo.getValue().equals("Pinguí"));
            });

            Button btnDelete = new Button("ELIMINAR");
            btnDelete.getStyleClass().add("button-secondary");
            btnDelete.setOnAction(e -> {
                activeCards.remove(this);
                flowJugadores.getChildren().remove(pane);
                btnAddPlayer.setDisable(false);
                actualizarTitulos();
            });

            pane.getChildren().addAll(lblTitle, comboTipo, pinguinoOptions, btnDelete);
        }

        private void mostrarPassword(Label lblPass) {
            txtPassword.setManaged(true);
            txtPassword.setVisible(true);
            lblPass.setManaged(true);
            lblPass.setVisible(true);
        }

        private void ocultarPassword() {
            txtPassword.setManaged(false);
            txtPassword.setVisible(false);
            if (lblPass != null) {
                lblPass.setManaged(false);
                lblPass.setVisible(false);
            }
        }

        public VBox getPane() { return pane; }

        /**
         * REENUMERA ELS TITOLS DELS JUGADORS QUAN S'ELIMINA UNA TARGETA.
         */
        private void actualizarTitulos() {
            for (int i = 0; i < activeCards.size(); i++) {
                PlayerCard card = activeCards.get(i);
                ((Label)card.pane.getChildren().get(0)).setText("JUGADOR " + (i + 1));
                
                // SI ÉS UN BOT, ACTUALITZEM EL SEU NÚMERO PER ORDRE
                if (card.comboMode.getValue().equals("IA (Bot)")) {
                    card.txtUsername.setText("BOT " + (i + 1));
                }
            }
        }
    }
}
