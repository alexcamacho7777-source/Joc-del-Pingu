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
import javafx.stage.Stage;
import model.Foca;
import model.Jugador;
import model.Pinguino;

import java.util.ArrayList;
import java.util.List;

public class PantallaCrearPartida {

    @FXML private TextField txtNomPartida;
    @FXML private FlowPane flowJugadores;
    @FXML private Button btnAddPlayer;

    private List<PlayerCard> activeCards = new ArrayList<>();
    private static final int MAX_JUGADORES = 4;

    @FXML
    private void initialize() {
        // Añadir el primer jugador por defecto (el usuario logueado)
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

    @FXML
    private void handleAddPlayer() {
        if (activeCards.size() >= MAX_JUGADORES) {
            mostrarAlert(Alert.AlertType.WARNING, "Límit de jugadors", "El màxim de jugadors és " + MAX_JUGADORES + ".");
            return;
        }

        PlayerCard card = new PlayerCard();
        activeCards.add(card);
        flowJugadores.getChildren().add(card.getPane());

        if (activeCards.size() >= MAX_JUGADORES) {
            btnAddPlayer.setDisable(true);
        }
    }

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

    @FXML
    private void handleComencar(ActionEvent event) {
        String nomPartida = txtNomPartida.getText().trim();
        if (nomPartida.isEmpty()) {
            mostrarAlert(Alert.AlertType.WARNING, "Dades incompletes", "Si us plau, posa un nom a la partida.");
            return;
        }

        if (activeCards.size() < 2) {
            mostrarAlert(Alert.AlertType.WARNING, "Pocs jugadors", "Has d'afegir almenys 2 jugadors per poder jugar.");
            return;
        }

        List<Jugador> jugadoresFinales = new ArrayList<>();
        GestorBBDD db = new GestorBBDD();

        for (PlayerCard card : activeCards) {
            String tipo = card.comboTipo.getValue();
            if (tipo.equals("Pinguí")) {
                String user = card.txtUsername.getText().trim();
                String color = card.comboColor.getValue();
                String pass = card.txtPassword.getText();
                
                if (user.isEmpty()) {
                    mostrarAlert(Alert.AlertType.ERROR, "Error Jugador", "El pinguí ha de tenir un nom d'usuari.");
                    return;
                }

                // Si es CPU no comprobamos contraseña (usamos el convenio de nombre)
                boolean esIA = user.toLowerCase().contains("cpu");
                
                if (!esIA) {
                    if (db.getIDJugador(user) == -1) {
                        mostrarAlert(Alert.AlertType.ERROR, "Usuari no trobat", "L'usuari '" + user + "' no està registrat.");
                        return;
                    }

                    if (!db.loginUsuario(user, pass)) {
                        mostrarAlert(Alert.AlertType.ERROR, "Contrasenya incorrecta", "La contrasenya per a l'usuari '" + user + "' no és correcta.");
                        return;
                    }
                }

                Pinguino p = new Pinguino(user, color, 0, new model.Inventario());
                p.setEsIA(esIA);
                jugadoresFinales.add(p);
            } else {
                Foca f = new Foca();
                f.setColor("Gris");
                jugadoresFinales.add(f);
            }
        }

        lanzarJuego(event, nomPartida, jugadoresFinales);
    }

    private void lanzarJuego(ActionEvent event, String nomPartida, List<Jugador> jugadores) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaJuego.fxml"));
            Parent root = loader.load();
            
            PantallaJuego controladorJoc = loader.getController();
            controladorJoc.setUsuarioLogueado(PantallaMenu.getLoggedInUser());
            
            // Pasamos los jugadores configurados
            controladorJoc.iniciarPartidaPersonalizada(nomPartida, jugadores);

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
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

    // Inner class to handle each player UI card
    private class PlayerCard {
        private VBox pane;
        private ChoiceBox<String> comboTipo;
        private TextField txtUsername;
        private PasswordField txtPassword;
        private ChoiceBox<String> comboColor;
        private VBox pinguinoOptions;

        public PlayerCard() {
            pane = new VBox(10);
            pane.getStyleClass().add("ice-card");
            pane.setPrefWidth(220);
            pane.setPadding(new Insets(15));
            pane.setAlignment(Pos.TOP_CENTER);
            pane.setOpacity(0);
            pane.setScaleX(0.8);
            pane.setScaleY(0.8);

            Label lblTitle = new Label("JUGADOR " + (activeCards.size() + 1));
            lblTitle.getStyleClass().add("field-label");

            comboTipo = new ChoiceBox<>();
            comboTipo.getItems().addAll("Pinguí", "Foca");
            comboTipo.setValue("Pinguí");
            comboTipo.setPrefWidth(180);

            pinguinoOptions = new VBox(8);
            txtUsername = new TextField();
            txtUsername.setPromptText("Usuari...");
            txtUsername.getStyleClass().add("styled-input");
            txtUsername.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 5; -fx-padding: 5;");
            
            txtPassword = new PasswordField();
            txtPassword.setPromptText("Contrasenya...");
            txtPassword.getStyleClass().add("styled-input");
            txtPassword.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 5; -fx-padding: 5;");
            txtPassword.setManaged(false);
            txtPassword.setVisible(false);

            Label lblPass = new Label("CONTRASENYA");
            lblPass.setManaged(false);
            lblPass.setVisible(false);
            lblPass.setStyle("-fx-font-size: 10px; -fx-text-fill: #a2c1ff;");

            // Real-time validation
            txtUsername.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.isEmpty()) {
                    txtUsername.setStyle("-fx-border-color: rgba(255,255,255,0.2); -fx-border-radius: 5; -fx-padding: 5;");
                    txtPassword.setManaged(false);
                    txtPassword.setVisible(false);
                    lblPass.setManaged(false);
                    lblPass.setVisible(false);
                } else {
                    GestorBBDD db = new GestorBBDD();
                    boolean exists = db.getIDJugador(newVal.trim()) != -1;
                    boolean esIA = newVal.toLowerCase().contains("cpu");

                    if (exists) {
                        txtUsername.setStyle("-fx-border-color: #2ecc71; -fx-border-radius: 5; -fx-padding: 5; -fx-border-width: 2;");
                        if (!esIA) {
                            txtPassword.setManaged(true);
                            txtPassword.setVisible(true);
                            lblPass.setManaged(true);
                            lblPass.setVisible(true);
                        } else {
                            txtPassword.setManaged(false);
                            txtPassword.setVisible(false);
                            lblPass.setManaged(false);
                            lblPass.setVisible(false);
                        }
                    } else {
                        txtUsername.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5; -fx-padding: 5; -fx-border-width: 2;");
                        txtPassword.setManaged(false);
                        txtPassword.setVisible(false);
                        lblPass.setManaged(false);
                        lblPass.setVisible(false);
                    }
                }
            });

            comboColor = new ChoiceBox<>();
            comboColor.getItems().addAll("Blau", "Vermell", "Verd", "Groc", "Rosa", "Cian");
            comboColor.setValue("Blau");
            comboColor.setPrefWidth(180);

            pinguinoOptions.getChildren().addAll(new Label("NOM"), txtUsername, lblPass, txtPassword, new Label("COLOR"), comboColor);

            comboTipo.setOnAction(e -> {
                pinguinoOptions.setVisible(comboTipo.getValue().equals("Pinguí"));
                pinguinoOptions.setManaged(comboTipo.getValue().equals("Pinguí"));
            });

            Button btnDelete = new Button("Eliminar");
            btnDelete.getStyleClass().add("button-secondary");
            btnDelete.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff8e8e; -fx-border-color: rgba(255,142,142,0.3); -fx-background-color: rgba(255,0,0,0.1);");
            btnDelete.setOnAction(e -> {
                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), pane);
                fade.setToValue(0);
                fade.setOnFinished(ev -> {
                    activeCards.remove(this);
                    flowJugadores.getChildren().remove(pane);
                    btnAddPlayer.setDisable(false);
                    actualizarTitulos();
                });
                fade.play();
            });

            pane.getChildren().addAll(lblTitle, comboTipo, pinguinoOptions, btnDelete);

            // Entry animation
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), pane);
            ft.setToValue(1);
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), pane);
            st.setToX(1); st.setToY(1);
            new javafx.animation.ParallelTransition(ft, st).play();
        }

        public VBox getPane() { return pane; }

        private void actualizarTitulos() {
            for (int i = 0; i < activeCards.size(); i++) {
                ((Label)activeCards.get(i).pane.getChildren().get(0)).setText("JUGADOR " + (i + 1));
            }
        }
    }
}
