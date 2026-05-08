package vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import model.*;
import controlador.*;

import javafx.animation.TranslateTransition;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;


import controlador.GestorPartida;
import model.Pez;
import model.BolaDeNieve;
import model.Casilla;
import model.Oso;
import model.Dado;
import model.Evento;
import model.Foca;
import model.Inventario;
import model.Item;
import model.Jugador;
import model.Pinguino;
import model.Tablero;

/**
 * CONTROLADOR DE LA INTERFÍCIE VISUAL DEL JOC (PANTALLA DE JOC).
 * GESTIONA TOTA LA INTERACCIÓ DE L'USUARI AMB EL TAULELL, LES ANIMACIONS
 * I L'ACTUALITZACIÓ DE L'INVENTARI EN TEMPS REAL.
 */
public class PantallaJuego {

    // ELEMENTS DEL MENÚ SUPERIOR
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // BOTONS D'ACCIÓ PRINCIPAL
    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    // TEXTOS I INDICADORS DE L'INVENTARI I ESTATS
    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private VBox vboxEventos;
    @FXML private ScrollPane scrollEventos;
    @FXML private Label nomInventari;
    @FXML private Label eventos;

    // CONTENIDORS DEL TAULELL I LES FITXES (AVATARS)
    @FXML private GridPane tablero;
    @FXML private Group P1;
    @FXML private Group P2;
    @FXML private Group P3;
    @FXML private Group P4;
    @FXML private Button btnAjustes;
    @FXML private ImageView bgImage;

    // ELEMENTS VISUALS DE CÀRREGA I VICTÒRIA
    @FXML private StackPane loadingOverlay;
    @FXML private ImageView loadingBg;
    @FXML private Rectangle loadingBar;
    @FXML private StackPane winOverlay;
    @FXML private Label winLabel;

    // PANELLS ARREL PER A LA JERARQUIA DE NODES
    @FXML private StackPane boardStack;
    @FXML private StackPane rootPane;

    // INSTÀNCIES DE CONTROL I VARIABLES D'ESTAT LOCAL
    private GestorPartida gestorPartida = new GestorPartida();
    private int p1Position = 0;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    private int numHumanos = 1;
    private int numIA = 1;
    private java.util.Map<Jugador, javafx.scene.Node> tokenMap = new java.util.HashMap<>();

    /**
     * CONFIGURA EL NÚMERO DE JUGADORS I PREPARA LA NOVA PARTIDA.
     */
    public void configurarJugadores(int humanos, int ias) {
        this.numHumanos = humanos;
        this.numIA = ias;
        prepararNuevaPartida();
    }

    /**
     * INICIALITZA UNA PARTIDA PERSONALITZADA AMB UNA LLISTA DE JUGADORS EXISTENTS.
     * CONFIGURA ELS TOKENS VISUALS I ELS ASSIGNA UN COLOR.
     */
    public void iniciarPartidaPersonalizada(String nomPartida, java.util.List<Jugador> jugadores) {
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        // OCULTEM TOTS ELS AVATARS ABANS DE COMENÇAR
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        tokenMap.clear();

        int pIndex = 0;
        for (Jugador j : jugadores) {
            if (j instanceof Pinguino) {
                if (pIndex < pTokens.length) {
                    pTokens[pIndex].setVisible(true);
                    tokenMap.put(j, pTokens[pIndex]);
                    aplicarColorAToken(pTokens[pIndex], j.getColor());
                    pIndex++;
                }
            } else if (j instanceof Foca) {
                javafx.scene.Node focaAvatar = getOrCreateFocaAvatar();
                tokenMap.put(j, focaAvatar);
            }
        }

        gestorPartida.nuevaPartida();
        gestorPartida.getPartida().setNombre(nomPartida);
        gestorPartida.getPartida().setJugadores(new ArrayList<>(jugadores));

        // ACTUALITZACIÓ VISUAL DE L'ESTAT INICIAL
        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero());
        syncVisualPositions(false);
        actualizarInventarioUI();
        actualizarGlowTurno();
        
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("TORN DE: " + (prox != null ? prox.getNombre().toUpperCase() : "..."));
        anadirLog("PARTIDA '" + nomPartida.toUpperCase() + "' COMENÇADA!");
    }

    /**
     * AFEGEIX UN MISSATGE AL PANORAMA D'EVENTS VISUALS (LOG).
     */
    private void anadirLog(String msg) {
        if (vboxEventos == null) return;
        
        Text text = new Text(msg);
        text.getStyleClass().add("log-entry");
        text.setWrappingWidth(300);
        vboxEventos.getChildren().add(text);
        
        // DESPLAÇAMENT AUTOMÀTIC CAP A BAIX PER VEURE L'ÚLTIM EVENT
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(50));
        pause.setOnFinished(e -> scrollEventos.setVvalue(1.0));
        pause.play();
    }

    /**
     * MÈTODE D'INICIALITZACIÓ DE JAVAFX. ESTABLEIX LA MÚSICA I LES PROPIETATS DE LES IMATGES.
     */
    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playGameMusic();
        if (gestorPartida == null) {
            gestorPartida = new GestorPartida();
        }
        gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        anadirLog("--- BENVINGUT AL JOC DEL PINGÜÍ ---");
        
        // AJUST DE LES IMATGES DE FONS PER A QUE SIGUIN RESPONSIVES
        if (bgImage != null) {
            bgImage.setManaged(false);
            bgImage.fitWidthProperty().bind(((StackPane)bgImage.getParent()).widthProperty());
            bgImage.fitHeightProperty().bind(((StackPane)bgImage.getParent()).heightProperty());
        }
        if (loadingBg != null) {
            loadingBg.setManaged(false);
            loadingBg.fitWidthProperty().bind(loadingOverlay.widthProperty());
            loadingBg.fitHeightProperty().bind(loadingOverlay.heightProperty());
        }
        ejecutarPantallaCarga();

        // ANIMACIÓ DE ROTACIÓ PER AL BOTÓ D'AJUSTAMENTS (EN PASSAR EL RATOLÍ)
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
     * EXECUTA L'ANIMACIÓ DE LA BARRA DE CÀRREGA INICIAL.
     */
    private void ejecutarPantallaCarga() {
        if (loadingOverlay == null) return;
        
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(loadingBar.widthProperty(), 0)),
            new javafx.animation.KeyFrame(Duration.seconds(2.5), new javafx.animation.KeyValue(loadingBar.widthProperty(), 300))
        );
        
        timeline.setOnFinished(e -> {
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(0.8), loadingOverlay);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> loadingOverlay.setVisible(false));
            fade.play();
        });
        
        timeline.play();
    }

    /**
     * PREPARA L'ENTORN PER A UNA NOVA PARTIDA DES DE ZERO.
     */
    private void prepararNuevaPartida() {
        gestorPartida = new GestorPartida();
        gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        ArrayList<Jugador> jugadores = new ArrayList<>();
        String[] colores = {"Azul", "Rojo", "Verde", "Amarillo"};
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        tokenMap.clear();

        // CREACIÓ DELS JUGADORS HUMANS
        for (int i = 0; i < numHumanos; i++) {
            Pinguino p = new Pinguino("JUGADOR " + (i + 1), colores[i % 4]);
            p.setEsIA(false);
            jugadores.add(p);
            if(i < pTokens.length) {
                pTokens[i].setVisible(true);
                tokenMap.put(p, pTokens[i]);
                GridPane.setHalignment(pTokens[i], javafx.geometry.HPos.CENTER);
                GridPane.setValignment(pTokens[i], javafx.geometry.VPos.CENTER);
            }
        }
        
        // CREACIÓ DELS JUGADORS CONTROLATS PER LA IA (CPU)
        for (int i = 0; i < numIA; i++) {
            Pinguino p = new Pinguino("CPU " + (i + 1), colores[(numHumanos + i) % 4]);
            p.setEsIA(true);
            jugadores.add(p);
            int idx = numHumanos + i;
            if(idx < pTokens.length) {
                pTokens[idx].setVisible(true);
                tokenMap.put(p, pTokens[idx]);
                GridPane.setHalignment(pTokens[idx], javafx.geometry.HPos.CENTER);
                GridPane.setValignment(pTokens[idx], javafx.geometry.VPos.CENTER);
            }
        }

        // AFEGIM LA FOCA COM A PERSONATGE NO JUGABLE (NPC) ACTIU
        model.Foca focaJugador = new model.Foca();
        jugadores.add(focaJugador);
        
        javafx.scene.Node focaAvatar = getOrCreateFocaAvatar();
        tokenMap.put(focaJugador, focaAvatar);

        gestorPartida.nuevaPartida();
        gestorPartida.getPartida().setJugadores(jugadores);

        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero());
        syncVisualPositions(false);
        actualizarInventarioUI();
    }

    /**
     * DIBUIXA LES CASELLES AL TAULELL I ASSIGNA LES IMATGES CORRESPONENTS A CADA TIPUS.
     */
    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        // NETEJEM EL TAULELL DE TEXTOS PREVIS
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);
            
            // TRACTAMENT ESPECIAL PER A LES CASELLES D'INICI I META
            if (i == 0 || i == 49) {
                VBox box = new VBox(2);
                box.setUserData(TAG_CASILLA_TEXT);
                box.setAlignment(Pos.CENTER);
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.setPadding(new Insets(10));
                box.getStyleClass().add("grid-tile");
                box.getStyleClass().add(i == 0 ? "start-tile" : "end-tile");
                
                String specialIcon = (i == 0) ? "inicio" : "meta";
                java.net.URL url = getClass().getResource("/resources/images/casillas/" + specialIcon + ".png");
                if (url != null) {
                    ImageView iv = new ImageView(new Image(url.toExternalForm()));
                    iv.setFitWidth(80); iv.setFitHeight(80);
                    iv.setPreserveRatio(true);
                    box.getChildren().add(iv);
                } else {
                    Text tStartEnd = new Text(i == 0 ? "INICI" : "META");
                    tStartEnd.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-fill: " + (i == 0 ? "#0072ff" : "#ff8c00") + ";");
                    box.getChildren().add(tStartEnd);
                }
                
                setupTileAnimation(box, i);
                continue;
            }

            // CASELLES INTERMÈDIES AMB OBSTACLES O BONS
            if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                VBox box = new VBox(2);
                box.setUserData(TAG_CASILLA_TEXT);
                box.setAlignment(Pos.CENTER);
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.setPadding(new Insets(5));
                box.getStyleClass().add("grid-tile");

                try {
                    java.net.URL url = getClass().getResource("/resources/images/casillas/" + tipo.toLowerCase() + ".png");
                    if (url == null) url = getClass().getResource("/images/casillas/" + tipo.toLowerCase() + ".png");

                    if (url != null) {
                        Image img = new Image(url.toExternalForm(), true);
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(75); iv.setFitHeight(75);
                        iv.setPreserveRatio(true);
                        box.getChildren().add(iv);
                    } else {
                        // FALLBACK SI NO ES TROBA LA IMATGE
                        String emojiText;
                        switch (tipo) {
                            case "Agujero": emojiText = "FORAT"; break;
                            case "Oso": emojiText = "OS"; break;
                            case "Trineo": emojiText = "TRINEU"; break;
                            case "SueloQuebradizo": emojiText = "FRÀGIL"; break;
                            case "Evento": emojiText = "?"; break;
                            default: emojiText = "?"; break;
                        }
                        Text fallback = new Text(emojiText);
                        fallback.setStyle("-fx-font-size: 32px; -fx-font-weight: 900; -fx-fill: white;");
                        box.getChildren().add(fallback);
                    }
                } catch (Exception e) {
                    box.getChildren().add(new Text("?"));
                }
                
                setupTileAnimation(box, i);
            }
        }
    }

    /**
     * CONFIGURA L'ANIMACIÓ DE FLOTACIÓ I L'APARICIÓ GRADUAL DE LES CASELLES.
     */
    private void setupTileAnimation(VBox box, int i) {
        int row = i / COLUMNS;
        int col = i % COLUMNS;
        
        GridPane.setRowIndex(box, row);
        GridPane.setColumnIndex(box, col);
        GridPane.setHalignment(box, javafx.geometry.HPos.CENTER);
        
        // ROTACIÓ ALEATÒRIA SUBTIL PER A UN ASPECTE MÉS NATURAL
        double randomRot = (Math.random() * 10) - 5;
        box.setRotate(randomRot);
        
        // ANIMACIÓ DE FLOTACIÓ (VERTICAL) PERMANENT
        javafx.animation.TranslateTransition floating = new javafx.animation.TranslateTransition(Duration.seconds(3 + Math.random() * 2), box);
        floating.setByY((Math.random() * 8) - 4);
        floating.setAutoReverse(true);
        floating.setCycleCount(javafx.animation.Animation.INDEFINITE);
        floating.play();
        
        // EFECTE D'APARICIÓ GRADUAL (FADE + SCALE)
        box.setOpacity(0);
        box.setScaleX(0); box.setScaleY(0);
        tablero.getChildren().add(box);
        
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(300), box);
        ft.setDelay(Duration.millis(i * 10));
        ft.setToValue(1.0);
        
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(400), box);
        st.setDelay(Duration.millis(i * 10));
        st.setToX(1); st.setToY(1);
        
     /**
     * GESTIONA L'ACCIÓ DE GUARDAR LA PARTIDA A LA BASE DE DADES.
     */
    @FXML private void handleSaveGame() { 
        boolean ok = gestorPartida.guardarPartida();
        if (ok) {
            anadirLog(">>> PROGRÉS GUARDAT CORRECTAMENT A LA BBDD.");
            mostrarAlert(AlertType.INFORMATION, "ÈXIT", "LA PARTIDA S'HA GUARDAT CORRECTAMENT.");
        } else {
            anadirLog(">>> ERROR EN GUARDAR LA PARTIDA.");
            mostrarAlert(AlertType.ERROR, "ERROR", "NO S'HA POGUT GUARDAR LA PARTIDA.");
        }
    }

    /**
     * OBRE LA FINESTRA D'AJUSTAMENTS COM UN OVERLAY MODAL I TRANSPARENT.
     */
    @FXML
    private void handleAjustes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaAjustes.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            javafx.stage.Window owner = null;
            if (event != null && event.getSource() instanceof Node) {
                owner = ((Node) event.getSource()).getScene().getWindow();
            } else if (tablero != null && tablero.getScene() != null) {
                owner = tablero.getScene().getWindow();
            }
            
            if (owner != null) stage.initOwner(owner);
            
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("ERROR CARREGANT AJUSTAMENTS: " + e.getMessage());
        }
    }

    /**
     * MOSTRA LA GUIA D'AJUDA I LES REGLES DEL JOC EN UNA FINESTRA MODAL.
     */
    @FXML
    private void handleGuia(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaAyuda.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            if (tablero != null && tablero.getScene() != null) {
                stage.initOwner(tablero.getScene().getWindow());
            }

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.showAndWait();
        } catch (Exception e) {
            anadirLog("ERROR CARREGANT LA GUIA DE JOC.");
        }
    }

    /**
     * TANCA LA PARTIDA ACTUAL I RETORNA L'USUARI AL MENÚ PRINCIPAL.
     */
    @FXML private void handleQuitGame() { 
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) tablero.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("EL JOC DEL PINGÜÍ - MENÚ");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); 
        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * INICIALITZA LA PANTALLA RECONSTRUINT UNA PARTIDA EXISTENT DES DE LA BBDD.
     */
    public void iniciarCargandoPartida(int id) {
        if (this.gestorPartida == null) {
            this.gestorPartida = new GestorPartida();
        }
        if (this.gestorPartida.getGestorBBDD() == null) {
            this.gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        }
        
        this.gestorPartida.cargarPartida(id);
        syncLoadedJugadores();
        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero()); 
        syncVisualPositions(false);
        actualizarInventarioUI();
        
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("TORN DE: " + (prox != null ? prox.getNombre().toUpperCase() : "..."));
        anadirLog("PARTIDA #" + id + " CARREGADA CORRECTAMENT.");
    }

    /**
     * ACCIÓ DE TIRAR EL DAU. DESHABILITA EL BOTÓ PER EVITAR DOBLES CLICS DURANT L'ANIMACIÓ.
     */
    @FXML
    private void handleDado(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        if(gestorPartida.getPartida().isFinalizada()) {
            anadirLog("EL JOC JA HA ACABAT.");
        } else {
            dado.setDisable(true);
            procesarSiguienteTurno();
        }
    }

    /**
     * PROCESSA LA LÒGICA DE CANVI DE TORN, CONSIDERANT SI UN JUGADOR PERD EL TORN O ESTÀ BLOQUEJAT.
     */
    private void procesarSiguienteTurno() {
        if (gestorPartida.getPartida().isFinalizada()) {
            dado.setDisable(false);
        } else {
            Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
            if (actual != null) {
                boolean turnoPerdido = false;
                
                // VERIFICACIÓ DE SI EL JUGADOR HA DE PERDRE EL TORN PER EFECTE DE LA RULETA O CASELLA
                if (gestorPartida.getPartida().getJugadorPierdeTurno() != null &&
                    gestorPartida.getPartida().getJugadorPierdeTurno().equals(actual)) {
                    gestorPartida.getPartida().setJugadorPierdeTurno(null);
                    anadirLog(actual.getNombre().toUpperCase() + " PERD AQUEST TORN.");
                    gestorPartida.getPartida().siguienteTurno();
                    finalizarTurnoComplet();
                    turnoPerdido = true;
                }

                if (!turnoPerdido && actual instanceof model.Foca f) {
                    // SI ÉS EL TORN DE LA FOCA I ESTÀ BLOQUEJADA PER UNA BOLA DE NEU
                    if (f.getTurnosBloqueada() > 0) {
                        f.reducirBloqueo();
                        anadirLog("LA FOCA CONTINUA BLOQUEJADA (" + f.getTurnosBloqueada() + " TORNS RESTANTS).");
                        gestorPartida.getPartida().siguienteTurno();
                        finalizarTurnoComplet();
                        turnoPerdido = true;
                    }
                }
                
                if (!turnoPerdido) {
                    ejecutarMovimientoJugador(actual);
                }
            }
        }
    }

    /**
     * EXECUTA EL MOVIMENT FÍSIC I LÒGIC D'UN JUGADOR AL TAULELL.
     */
    private void ejecutarMovimientoJugador(Jugador actual) {
        int posAnterior = actual.getPosicion();

        if (actual.isEsIA()) {
            gestorPartida.realizarAccionesIA(actual);
        }

        // CÀLCUL DEL DESTÍ SEGONS EL DAU
        int pasos = gestorPartida.tirarDadoParaJugador(actual);
        int posIntermedia = posAnterior + pasos;
        int maxPos = gestorPartida.getPartida().getTablero().getTotalCasillas() - 1;
        if (posIntermedia > maxPos) posIntermedia = maxPos;
        
        final int finalPosIntermedia = posIntermedia;
        actual.setPosicion(finalPosIntermedia);

        // ANIMACIÓ DEL MOVIMENT PAS A PAS
        animarMovimiento(actual, posAnterior, finalPosIntermedia, () -> {
            // LÒGICA DE LA FOCA SI PASSA PER SOBRE DE PINGÜINS
            if (actual instanceof Foca foca) {
                java.util.Map<Pinguino, List<String>> robos = gestorPartida.procesarPasoDeFoca(foca, posAnterior, finalPosIntermedia);
                if (!robos.isEmpty()) {
                    actualizarInventarioUI();
                    anadirLog("LA FOCA HA ROBAT ÍTEMS AL SEU PAS.");
                }
            }

            Casilla casilla = gestorPartida.getPartida().getTablero().getCasilla(finalPosIntermedia);
            String tipo = casilla.getClass().getSimpleName();
            
            // SI LA CASELLA TÉ UN EFECTE DE DESPLAÇAMENT (TRINEU, FORAT O OS)
            if ("Trineo".equals(tipo) || "Agujero".equals(tipo) || "Oso".equals(tipo)) {
                String msg;
                String sound;
                if ("Oso".equals(tipo)) {
                    msg = "L'OS T'HA ENVIAT A L'INICI!";
                    sound = "bear";
                } else {
                    msg = "Trineo".equals(tipo) ? "HAS TROBAT UN TRINEU! AVANCES!" : "HAS CAIGUT EN UN FORAT! RETROCEDEIXES!";
                    sound = "Trineo".equals(tipo) ? "sled" : "hole";
                }
                
                showSpecialTileMessage(msg, sound, () -> {
                    int posAntEfecto = actual.getPosicion();
                    if ("Oso".equals(tipo)) {
                        actual.setPosicion(0);
                    } else {
                        gestorPartida.getGestorTablero().ejecutarCasilla(gestorPartida.getPartida(), actual, casilla);
                    }
                    gestorPartida.comprobarInteraccionesEnCasilla(actual);
                    int posFinal = actual.getPosicion();
                    
                    animarMovimiento(actual, posAntEfecto, posFinal, () -> {
                        finalizarLogicaTurno(actual, () -> {});
                    });
                });
            } else {
                // CASELLA NORMAL O D'ESDEVENIMENT
                gestorPartida.getGestorTablero().ejecutarCasilla(gestorPartida.getPartida(), actual, casilla);
                gestorPartida.comprobarInteraccionesEnCasilla(actual);
                finalizarLogicaTurno(actual, () -> {});
            }
        });
    }

    /**
     * FINALITZA LA LÒGICA DEL TORN ACTUAL I PREPARA EL SEGÜENT.
     * VERIFICA SI S'HA D'OBRIR LA RULETA O SI HI HA UN GUANYADOR.
     */
    private void finalizarLogicaTurno(Jugador actual, Runnable onDone) {
        comprobarInteraccionesUI(actual, () -> {
            gestorPartida.getPartida().siguienteTurno();
            
            Jugador proxSiguiente = gestorPartida.getPartida().getJugadorActualObj();
            dadoResultText.setText("TORN DE: " + (proxSiguiente != null ? proxSiguiente.getNombre().toUpperCase() : "..."));
            
            syncVisualPositions(false);
            actualizarInventarioUI();
            gestorPartida.guardarPartida();
            actualizarGlowTurno();
            
            if (gestorPartida.getPartida().isFinalizada()) {
                controlador.SoundManager.getInstance().playSoundOnce("win");
                mostrarVictoria(gestorPartida.getPartida().getGanador());
            } else {
                Casilla casillaActual = gestorPartida.getPartida().getTablero().getCasilla(actual.getPosicion());
                // SI EL JUGADOR CAU EN UN ESDEVENIMENT, S'OBRE LA RULETA (EXCEPTE SI ÉS LA FOCA)
                if (casillaActual instanceof Evento && !(actual instanceof model.Foca)) {
                    mostrarRuleta(actual, this::finalizarTurnoComplet);
                } else {
                    finalizarTurnoComplet();
                }
            }
        });
    }

    /**
     * MOSTRA UN MISSATGE VISUAL GRAN QUAN ES CAU EN UNA CASELLA AMB EFECTE ESPECIAL.
     */
    private void showSpecialTileMessage(String text, String sound, Runnable onFinished) {
        controlador.SoundManager.getInstance().playSound(sound);
        
        Label label = new Label(text);
        label.getStyleClass().add("special-tile-msg");
        label.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        StackPane container = new StackPane(label);
        container.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 30; -fx-background-radius: 20;");
        container.setMaxWidth(800);
        
        boardStack.getChildren().add(container);
        
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.seconds(1.5), container);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.setOnFinished(e -> {
            boardStack.getChildren().remove(container);
            onFinished.run();
        });
        ft.play();
    }

    /**
     * FINALITZA EL TORN COMPLETAMENT I PERMET EL MOVIMENT DEL SEGÜENT JUGADOR (IA O HUMÀ).
     */
    private void finalizarTurnoComplet() {
        Jugador proxSiguienteTurno = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("TORN DE: " + (proxSiguienteTurno != null ? proxSiguienteTurno.getNombre().toUpperCase() : "..."));
        
        syncVisualPositions(true);

        if (proxSiguienteTurno != null && proxSiguienteTurno.isEsIA() && !gestorPartida.getPartida().isFinalizada()) {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> procesarSiguienteTurno());
            pause.play();
        } else {
            dado.setDisable(false);
        }
        actualizarInventarioUI();
    }

    /**
     * CARREGA I MOSTRA LA PANTALLA DE LA RULETA D'ESDEVENIMENTS.
     */
    private void mostrarRuleta(Jugador j, Runnable onFinished) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/PantallaRuleta.fxml"));
            javafx.scene.Parent ruletaRoot = loader.load();
            PantallaRuleta controller = loader.getController();
            
            controller.setGameContext(gestorPartida.getPartida(), j);
            boardStack.getChildren().add(ruletaRoot);
            
            ruletaRoot.parentProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) onFinished.run();
            });

        } catch (Exception e) {
            onFinished.run();
        }
    }

    /**
     * ANIMA EL MOVIMENT D'UNA FITXA PAS A PAS ENTRE DUES CASELLES.
     */
    private void animarMovimiento(Jugador j, int from, int to, Runnable onFinished) {
        javafx.scene.Node token = tokenMap.get(j);
        if (from == to || token == null) {
            onFinished.run();
            return;
        }

        // SI EL SALT ÉS MOLT GRAN, FAEM UN DESPLAÇAMENT DIRECTE SENSE ANIMACIÓ PAS A PAS
        if (Math.abs(to - from) > 20) {
            syncVisualPositions(false);
            onFinished.run();
            return;
        }
       onFinished.run();
            return;
        }

        while (current != to) {
            current += step;
            final int finalCurrent = current;
            
            // Animación de "Salto": sube, cambia la celda, y vuelve a bajar
            javafx.animation.ScaleTransition scaleUp = new javafx.animation.ScaleTransition(Duration.millis(120), token);
            scaleUp.setToX(1.3); scaleUp.setToY(1.3);
            
            javafx.animation.TranslateTransition moveYUp = new javafx.animation.TranslateTransition(Duration.millis(120), token);
            moveYUp.setByY(-15);
            
            javafx.animation.ParallelTransition hopUp = new javafx.animation.ParallelTransition(scaleUp, moveYUp);
            
            javafx.animation.PauseTransition pauseLayout = new javafx.animation.PauseTransition(Duration.millis(10));
            pauseLayout.setOnFinished(e -> {
                int f = finalCurrent;
                if (f < 0) f = 0; if (f >= 50) f = 49;
                GridPane.setRowIndex(token, f / COLUMNS);
                GridPane.setColumnIndex(token, f % COLUMNS);
                token.toFront();
            });

            javafx.animation.ScaleTransition scaleDown = new javafx.animation.ScaleTransition(Duration.millis(120), token);
            scaleDown.setToX(1.0); scaleDown.setToY(1.0);
            
            javafx.animation.TranslateTransition moveYDown = new javafx.animation.TranslateTransition(Duration.millis(120), token);
            moveYDown.setByY(15);
            
            javafx.animation.ParallelTransition hopDown = new javafx.animation.ParallelTransition(scaleDown, moveYDown);

            seq.getChildren().addAll(hopUp, pauseLayout, hopDown);
        }

        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }
    

    /**
     * Sincroniza todas las fichas en la vista con sus posiciones lógicas.
     */
    private void syncVisualPositions(boolean animar) {
        // Agrupar jugadores por posición para distribuirlos correctamente
        java.util.Map<Integer, java.util.List<Jugador>> enCasilla = new java.util.HashMap<>();
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            int pos = j.getPosicion();
            if (pos >= 50) pos = 49;
            if (pos < 0) pos = 0;
            enCasilla.computeIfAbsent(pos, k -> new ArrayList<>()).add(j);
        }

        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            javafx.scene.Node token = tokenMap.get(j);

            // Asegurar que la ficha se dibuje por encima de las casillas en el GridPane
            token.toFront();

            java.util.List<Jugador> listaEnPos = enCasilla.get(pos);
            int indexInCell = listaEnPos.indexOf(j);
            int countInCell = listaEnPos.size();

            // Calculamos un pequeño offset para que los jugadores en la misma casilla no se tapen, manteniendo el centro
            double offsetX = 0;
            double offsetY = 0;

            if (countInCell == 2) {
                offsetX = (indexInCell == 0) ? -15 : 15;
            } else if (countInCell == 3) {
                if (indexInCell == 0) { offsetX = -15; offsetY = -10; }
                else if (indexInCell == 1) { offsetX = 15; offsetY = -10; }
                else { offsetX = 0; offsetY = 15; }
            } else if (countInCell == 4) {
                if (indexInCell == 0) { offsetX = -15; offsetY = -15; }
                else if (indexInCell == 1) { offsetX = 15; offsetY = -15; }
                else if (indexInCell == 2) { offsetX = -15; offsetY = 15; }
                else { offsetX = 15; offsetY = 15; }
            } else if (countInCell > 4) {
                if (indexInCell == 0) { offsetX = -20; offsetY = -20; }
                else if (indexInCell == 1) { offsetX = 20; offsetY = -20; }
                else if (indexInCell == 2) { offsetX = -20; offsetY = 20; }
                else if (indexInCell == 3) { offsetX = 20; offsetY = 20; }
                else { offsetX = 0; offsetY = 0; }
            }

            if (animar) {
                javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(Duration.millis(350), token);
                slide.setToX(offsetX);
                slide.setToY(offsetY);
                GridPane.setRowIndex(token, newRow);
                GridPane.setColumnIndex(token, newCol);
                GridPane.setHalignment(token, javafx.geometry.HPos.CENTER);
                GridPane.setValignment(token, javafx.geometry.VPos.CENTER);
                slide.play();
            } else {
                GridPane.setRowIndex(token, newRow);
                GridPane.setColumnIndex(token, newCol);
                GridPane.setHalignment(token, javafx.geometry.HPos.CENTER);
                GridPane.setValignment(token, javafx.geometry.VPos.CENTER);
                token.setTranslateX(offsetX);
                token.setTranslateY(offsetY);
            }
        }
    }

    private void actualizarGlowTurno() {
        Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            javafx.scene.Node token = tokenMap.get(j);
            if (token != null) {
                if (j.equals(actual)) {
                    token.getStyleClass().add("current-player");
                    // Efecto de pulso
                    javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(Duration.seconds(0.8), token);
                    pulse.setFromX(1); pulse.setFromY(1);
                    pulse.setToX(1.15); pulse.setToY(1.15);
                    pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                    pulse.setAutoReverse(true);
                    pulse.play();
                    token.setUserData(pulse);
                } else {
                    token.getStyleClass().remove("current-player");
                    if (token.getUserData() instanceof javafx.animation.ScaleTransition) {
                        javafx.animation.ScaleTransition st = (javafx.animation.ScaleTransition) token.getUserData();
                        st.stop();
                        token.setScaleX(1); token.setScaleY(1);
                    }
                }
            }
        }
    }

    @FXML private void handleRapido() { 
        controlador.SoundManager.getInstance().playSound("click");
        anadirLog("Has fet servir Dau ràpid."); 
        usarObjetoYActualizar("DadoRapido");
    }
    @FXML private void handleLento()  { 
        controlador.SoundManager.getInstance().playSound("click");
        anadirLog("Has fet servir Dau lent."); 
        usarObjetoYActualizar("DadoLento");
    }
    @FXML private void handlePeces()  { 
        controlador.SoundManager.getInstance().playSound("click");
        anadirLog("Has menjat Peixos (+ vida o energia)."); 
        usarObjetoYActualizar("Peces");
    }
    @FXML private void handleNieve()  { 
        controlador.SoundManager.getInstance().playSound("click");
        anadirLog("Has llançat una Bola de neu."); 
        usarObjetoYActualizar("BolaNieve");
    }

    private void usarObjetoYActualizar(String tipo) {
        if(gestorPartida.getPartida().isFinalizada()) return;
        
        Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
        if (!(actual instanceof Pinguino) || ((Pinguino) actual).isEsIA()) {
            anadirLog("No pots usar objectes si no és el teu torn.");
            return;
        }
        Pinguino p = (Pinguino) actual;

        Inventario inv = p.getInv();
        if (inv == null) return;

        // Caso Dados: Usar e iniciar turno
        if (tipo.equals("DadoRapido") || tipo.equals("DadoLento")) {
            Item item = inv.getItem(Dado.class); // Simplificación: busca el primero de clase Dado que coincida
            // En una implementación más compleja buscaríamos exactamente el tipo.
            // Por ahora, buscaremos el dado que coincida con el nombre.
            boolean trobat = false;
            for (int i = 0; i < inv.getLista().size() && !trobat; i++) {
                Item itemLoop = inv.getLista().get(i);
                if (itemLoop instanceof Dado) {
                    Dado d = (Dado) itemLoop;
                    String name = d.getNombre().toLowerCase();
                    if (tipo.equals("DadoRapido") && (name.contains("rapido") || name.contains("ràpid"))) {
                        dadoElegido = d;
                        trobat = true;
                    }
                    if (!trobat && tipo.equals("DadoLento") && (name.contains("lento") || name.contains("lent"))) {
                        dadoElegido = d;
                        trobat = true;
                    }
                }
            }

            if (dadoElegido != null && dadoElegido.getCantidad() > 0) {
                int posAnterior = p.getPosicion();
                int resultado = gestorPartida.usarDadoEspecial(p, dadoElegido);
                p.moverPosicion(resultado);
                
                // Procesar resto del turno (casilla, etc)
                Casilla casilla = gestorPartida.getPartida().getTablero().getCasilla(p.getPosicion());
                gestorPartida.getGestorTablero().ejecutarCasilla(gestorPartida.getPartida(), p, casilla);
                gestorPartida.getPartida().anadirEvento(p.getNombre() + " usa " + tipo + " i treu un " + resultado);
                
                animarMovimiento(p, posAnterior, p.getPosicion(), () -> {
                    comprobarInteraccionesUI(p, () -> {
                        gestorPartida.getPartida().siguienteTurno();
                        procesarSiguienteTurno();
                    });
                });
            } else {
                anadirLog("No tens aquest objecte!");
            }
        } 
        // Caso Otros: Usar y no pasar turno inmediatamente? (Depende de la regla, usualmente Peces se usa proactivamente)
        else {
            if (tipo.equals("Peces")) {
                Item it = inv.getItem(model.Pez.class);
                if (it != null && it.getCantidad() > 0) {
                    it.setCantidad(it.getCantidad() - 1);
                    if (it.getCantidad() <= 0) inv.quitarItem(it);
                    anadirLog(p.getNombre() + " ha menjat peixos!");
                } else {
                    anadirLog("No tens peixos!");
                }
            } else if (tipo.equals("BolaNieve")) {
                Item it = inv.getItem(model.BolaDeNieve.class);
                if (it != null && it.getCantidad() > 0) {
                    it.setCantidad(it.getCantidad() - 1);
                    if (it.getCantidad() <= 0) inv.quitarItem(it);
                    anadirLog(p.getNombre() + " ha llançat una bola de neu!");
                } else {
                    anadirLog("No tens boles de neu!");
                }
            }
            actualizarInventarioUI();
            gestorPartida.guardarPartida();
        }
    }

    private void actualizarInventarioUI() {
        if(gestorPartida.getPartida().getJugadores().size() > 0) {
            Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
            
            // Si el actual es Foca o IA, mostramos el del primer humano para que no se quede vacío, 
            // pero indicamos de quién es.
            Jugador mostrar = actual;
            boolean trobatJug = false;
            java.util.List<Jugador> jugs = gestorPartida.getPartida().getJugadores();
            for(int k = 0; k < jugs.size() && !trobatJug; k++) {
                Jugador j = jugs.get(k);
                if(j instanceof Pinguino && !((Pinguino) j).isEsIA()) {
                    mostrar = j;
                    trobatJug = true;
                }
            }
            
            if (mostrar instanceof Pinguino) {
                Pinguino p = (Pinguino) mostrar;
                nomInventari.setText("PROPIETARI: " + p.getNombre().toUpperCase());
                
                String colorHex;
                switch(p.getColor().toLowerCase()) {
                    case "azul":
                    case "blau": colorHex = "#0072ff"; break;
                    case "rojo":
                    case "vermell": colorHex = "#ff4b2b"; break;
                    case "verde":
                    case "verd": colorHex = "#11998e"; break;
                    case "amarillo":
                    case "groc": colorHex = "#f2c94c"; break;
                    case "rosa": colorHex = "#ff69b4"; break;
                    case "cian": colorHex = "#00ffff"; break;
                    default: colorHex = "#00d2ff"; break;
                }
                nomInventari.setStyle("-fx-font-weight: bold; -fx-text-fill: " + colorHex + "; -fx-font-size: 14px;");

                Inventario inv = p.getInv();
                if(inv != null) {
                    rapido_t.setText("Dau ràpid: " + inv.contarItems("DadoRapido"));
                    lento_t.setText("Dau lent: " + inv.contarItems("DadoLento"));
                    peces_t.setText("Peixos: " + inv.contarItems("Peces"));
                    nieve_t.setText("Boles neu: " + inv.contarItems("BolaNieve"));
                }
            } else {
                nomInventari.setText("Inventari buit");
                nomInventari.setStyle("-fx-font-weight: bold; -fx-text-fill: #888888;");
                rapido_t.setText("Dau ràpid: 0");
                lento_t.setText("Dau lent: 0");
                peces_t.setText("Peixos: 0");
                nieve_t.setText("Boles neu: 0");
            }
        }
    }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
        if (gestorPartida != null && gestorPartida.getPartida() != null) {
            syncLoadedJugadores();
            syncVisualPositions(false);
            actualizarInventarioUI();
        }
    }

    /**
     * Re-vincula els objectes Jugador carregats de la BBDD amb els nodes visuals (P1, P2...).
     */
    private javafx.scene.Node getOrCreateFocaAvatar() {
        // Cache-busting ID
        javafx.scene.Node focaAvatar = tablero.lookup("#ROBOT_SEAL_V3");
        StackPane sp;
        
        if (focaAvatar == null) {
            sp = new StackPane();
            sp.setId("ROBOT_SEAL_V3");
            sp.getStyleClass().add("player");
            sp.setStyle("-fx-background-color: transparent;"); 
            sp.setMaxSize(50, 50); // Evitar que ocupe toda la pantalla
            tablero.getChildren().add(sp);
        } else {
            sp = (StackPane) focaAvatar;
            sp.getChildren().clear(); 
        }

        try {
            // Path a la imagen proporcionada por el usuario
            String[] paths = {
                "/resources/images/casillas/foca.png",
                "/images/casillas/foca.png",
                "/resources/foca.png"
            };
            Image img = null;
            boolean imgTrobat = false;
            for (int i = 0; i < paths.length && !imgTrobat; i++) {
                String p = paths[i];
                java.net.URL url = getClass().getResource(p);
                if (url != null) {
                    img = new Image(url.toExternalForm());
                    imgTrobat = true;
                }
            }

            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(65);
                iv.setFitHeight(65);
                iv.setPreserveRatio(true);
                sp.getChildren().add(iv);
            } else {
                // Fallback visual muy claro (SIN EMOJIS que puedan fallar)
                Rectangle r = new Rectangle(40, 40, Color.web("#1a2a44"));
                r.setStroke(Color.CYAN);
                r.setStrokeWidth(2);
                Text t = new Text("ROBOT");
                t.setFill(Color.WHITE);
                t.setStyle("-fx-font-size: 9px; -fx-font-weight: bold;");
                StackPane.setAlignment(t, Pos.CENTER);
                sp.getChildren().addAll(r, t);
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: Error loading seal avatar: " + e.getMessage());
        }

        GridPane.setHalignment(sp, javafx.geometry.HPos.CENTER);
        GridPane.setValignment(sp, javafx.geometry.VPos.CENTER);
        
        return sp;
    }

    private void syncLoadedJugadores() {
        if (gestorPartida == null || gestorPartida.getPartida() == null) return;
        
        java.util.List<Jugador> jugadores = gestorPartida.getPartida().getJugadores();
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        tokenMap.clear();
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            if (j instanceof model.Foca) {
                javafx.scene.Node focaAvatar = getOrCreateFocaAvatar();
                tokenMap.put(j, focaAvatar);
                focaAvatar.setVisible(true);
            } else if (i < pTokens.length && pTokens[i] != null) {
                pTokens[i].setVisible(true);
                tokenMap.put(j, pTokens[i]);
                aplicarColorAToken(pTokens[i], j.getColor());
            }
        }
    }
    
    private void aplicarColorAToken(javafx.scene.Node token, String colorStr) {
        if (colorStr == null) return;
        javafx.scene.paint.Color color;
        switch (colorStr.toLowerCase()) {
            case "vermell": color = javafx.scene.paint.Color.RED; break;
            case "verd": color = javafx.scene.paint.Color.GREEN; break;
            case "groc": color = javafx.scene.paint.Color.YELLOW; break;
            case "rosa": color = javafx.scene.paint.Color.PINK; break;
            case "cian": color = javafx.scene.paint.Color.CYAN; break;
            case "blau": color = javafx.scene.paint.Color.DEEPSKYBLUE; break;
            default: color = javafx.scene.paint.Color.DEEPSKYBLUE;
        }

        // Aplicar ColorAdjust para teñir el pingüino
        javafx.scene.effect.ColorAdjust monColor = new javafx.scene.effect.ColorAdjust();
        // El azul original tiene un hue de aprox 0.6. Ajustamos el hue para llegar al color deseado.
        // Forma más simple: usar un DropShadow de color muy fuerte o cambiar el relleno de las partes si fuera SVG.
        // Como es un Group de formas, buscamos la forma principal (cuerpo) y le cambiamos el color.
        javafx.scene.Group targetGroup = null;
        if (token instanceof javafx.scene.Group) {
            targetGroup = (javafx.scene.Group) token;
        } else if (token instanceof StackPane) {
            StackPane sp = (StackPane) token;
            if (!sp.getChildren().isEmpty() && sp.getChildren().get(0) instanceof javafx.scene.Group) {
                targetGroup = (javafx.scene.Group) sp.getChildren().get(0);
            }
        }

        if (targetGroup != null) {
            for (javafx.scene.Node child : targetGroup.getChildren()) {
                if (child instanceof javafx.scene.shape.Shape) {
                    javafx.scene.shape.Shape s = (javafx.scene.shape.Shape) child;
                    // El gorro suele estar compuesto por rectángulos o polígonos de color azul
                    if (s.getFill() instanceof javafx.scene.paint.Color) {
                        javafx.scene.paint.Color fill = (javafx.scene.paint.Color) s.getFill();
                        // Si es el azul original del gorro, lo cambiamos
                        if (fill.equals(javafx.scene.paint.Color.DEEPSKYBLUE) || 
                            fill.equals(javafx.scene.paint.Color.BLUE) ||
                            fill.toString().contains("0x00bfff") || // DeepSkyBlue
                            fill.toString().contains("0xff4b2b") || // Rojo P2
                            fill.toString().contains("0x11998e") || // Verde P3
                            fill.toString().contains("0xf2c94c") || // Amarillo P4
                            (child instanceof javafx.scene.shape.Rectangle)) { 
                            s.setFill(color);
                        }
                    }
                }
            }
        }
    }

    private void mostrarVictoria(Jugador ganador) {
        if (winOverlay != null) {
            if (ganador.isEsIA()) {
                winLabel.setText("DERROTA! " + ganador.getNombre().toUpperCase() + " HA GUANYAT...");
            } else {
                winLabel.setText("VICTÒRIA! " + ganador.getNombre().toUpperCase() + " HA GUANYAT!");
            }
            
            gestorPartida.getPartida().setFinalizada(true);
            gestorPartida.getPartida().setGanador(ganador);
            gestorPartida.guardarPartida();
            winOverlay.setVisible(true);
            winOverlay.setMouseTransparent(false);
            winOverlay.setOpacity(0);
            
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1.5), winOverlay);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
            
            // Efecto de escala
            javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.seconds(1), winOverlay);
            scale.setFromX(0.5); scale.setFromY(0.5);
            scale.setToX(1); scale.setToY(1);
            scale.play();
        }
    }

    private void showToast(String message, String color) {
        Label toast = new Label(message);
        toast.getStyleClass().add("stat");
        toast.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 10 20; -fx-background-radius: 20; -fx-text-fill: " + color + "; -fx-font-size: 18px;");
        
        StackPane container = (StackPane) loadingOverlay.getParent();
        container.getChildren().add(toast);
        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        toast.setTranslateY(100);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(0.5), toast);
        fade.setFromValue(0); fade.setToValue(1);
        
        javafx.animation.TranslateTransition move = new javafx.animation.TranslateTransition(Duration.seconds(2), toast);
        move.setByY(-50);
        
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.seconds(0.5), toast);
        fadeOut.setDelay(Duration.seconds(1.5));
        fadeOut.setFromValue(1); fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> container.getChildren().remove(toast));
        
        fade.play(); move.play(); fadeOut.play();
    }



    public void setUsuarioLogueado(String username) {
        if (gestorPartida != null && gestorPartida.getPartida() != null) {
            boolean userSet = false;
            java.util.List<Jugador> lJ = gestorPartida.getPartida().getJugadores();
            for (int i = 0; i < lJ.size() && !userSet; i++) {
                Jugador j = lJ.get(i);
                if (j instanceof Pinguino) {
                    Pinguino p = (Pinguino) j;
                    if (!p.isEsIA()) {
                        p.setNombre(username);
                        userSet = true;
                    }
                }
            }
            syncVisualPositions(false);
            actualizarInventarioUI();
        }
    }

    /**
     * Comprueba interacciones que requieren decisión del usuario (Peces vs Oso/Foca)
     * o eventos especiales (Guerra de bolas de nieve).
     */
    private void comprobarInteraccionesUI(Jugador j, Runnable onDone) {
        if (gestorPartida != null && gestorPartida.getPartida() != null) {
            // --- CASILLA INICIO: ZONA SEGURA ---
            if (j.getPosicion() != 0) {
                Tablero t = gestorPartida.getPartida().getTablero();
                Casilla c = t.getCasilla(j.getPosicion());
                
                boolean decisionTomada = false;
                // 1. INTERACCIÓN CON OSO
                if (c instanceof Oso && j instanceof Pinguino && !((Pinguino)j).isEsIA()) {
                    // (Lògica de decisió d'os)
                    decisionTomada = true;
                }
                
                if (!decisionTomada) {
                    // 2. INTERACCIÓN CON FOCA
                    // ... (resta de lògica)
                }
            } else {
                onDone.run();
            }
        } else {
            onDone.run();
        }
    }

        // 2. INTERACCIÓN CON FOCA
        if (j instanceof Pinguino) {
            Pinguino p = (Pinguino) j;
            if (!p.isEsIA()) {
                Foca f = findFocaEnPosicion(p.getPosicion());
                if (f != null && !f.isSobornada()) {
                    interactuarConFoca(p, f, onDone);
                    return;
                }
            }
        } else if (j instanceof Foca) {
            Foca f = (Foca) j;
            if (!f.isSobornada()) {
                // Busquem si hi ha algun pingüí humà en aquesta casella
                for (Jugador otro : gestorPartida.getPartida().getJugadores()) {
                    if (otro instanceof Pinguino && !((Pinguino) otro).isEsIA() && otro.getPosicion() == f.getPosicion()) {
                        interactuarConFoca((Pinguino) otro, f, onDone);
                        return;
                    }
                }
            }
        }

        // 3. GUERRA DE BOLAS DE NIEVE
        if (!(j instanceof Pinguino)) {
            onDone.run();
            return;
        }

        Jugador oponente = null;
        boolean opoFound = false;
        java.util.List<Jugador> lJ2 = gestorPartida.getPartida().getJugadores();
        for(int k = 0; k < lJ2.size() && !opoFound; k++) {
            Jugador otro = lJ2.get(k);
            if (otro != j && otro instanceof Pinguino && otro.getPosicion() == j.getPosicion()) {
                oponente = otro;
                opoFound = true;
            }
        }

        if (oponente != null) {
            ejecutarGuerraBolas((Pinguino)j, (Pinguino)oponente, onDone);
            return;
        }

        onDone.run();
    }

    private void mostrarDecision(String titulo, String msg, Runnable onYes, Runnable onNo) {
        Platform.runLater(() -> {
            try {
                if (boardStack == null) {
                    onNo.run();
                    return;
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaDecision.fxml"));
                Parent root = loader.load();
                PantallaDecision controller = loader.getController();
                
                controller.setContent(titulo, msg, onYes, onNo);
                
                // Afegim l'overlay al StackPane del joc
                boardStack.getChildren().add(root);
                
            } catch (Exception e) {
                System.err.println("Error al mostrar decision: " + e.getMessage());
                e.printStackTrace();
                // Fallback a Alert si falla el custom UI
                Alert alert = new Alert(AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
                alert.setTitle(titulo);
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) onYes.run();
                    else onNo.run();
                });
            }
        });
    }

    private void ejecutarGuerraBolas(Pinguino p1, Pinguino p2, Runnable onDone) {
        Item b1it = p1.getInv().getItem(BolaDeNieve.class);
        Item b2it = p2.getInv().getItem(BolaDeNieve.class);
        int b1 = b1it != null ? b1it.getCantidad() : 0;
        int b2 = b2it != null ? b2it.getCantidad() : 0;

        String msg = "GUERRA DE BOLES DE NEU!\n" + p1.getNombre() + " (" + b1 + ") vs " + p2.getNombre() + " (" + b2 + ")";
        
        Platform.runLater(() -> {
            PantallaAlerta.mostrar(rootPane, "GUERRA DE BOLES!", msg, () -> {
                // Ambos gastan sus bolas
                if (b1it != null) p1.getInv().quitarItem(b1it);
                if (b2it != null) p2.getInv().quitarItem(b2it);

                int diff = b1 - b2;
                if (diff > 0) {
                    anadirLog(p1.getNombre() + " guanya la guerra i avança " + diff + "!");
                    int posAnt = p1.getPosicion();
                    p1.moverPosicion(diff);
                    animarMovimiento(p1, posAnt, p1.getPosicion(), onDone);
                } else if (diff < 0) {
                    anadirLog(p2.getNombre() + " guanya la guerra i avança " + (-diff) + "!");
                    int posAnt = p2.getPosicion();
                    p2.moverPosicion(-diff);
                    animarMovimiento(p2, posAnt, p2.getPosicion(), onDone);
                } else {
                    anadirLog("Empat! Cap jugador es mou.");
                    onDone.run();
                }
                actualizarInventarioUI();
            });
        });
    }

    private Foca findFocaEnPosicion(int pos) {
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            if (j instanceof Foca && j.getPosicion() == pos) return (Foca) j;
        }
        return null;
    }

    private void interactuarConFoca(Pinguino p, Foca f, Runnable onDone) {
        Item pez = p.getInv().getItem(Pez.class);
        if (pez != null && pez.getCantidad() > 0) {
            mostrarDecision("FOCA ROBOT!", 
                "La foca t'ha atrapat! Vols donar-li un peix per bloquejar-la 2 torns?", 
                () -> {
                    pez.setCantidad(pez.getCantidad() - 1);
                    if (pez.getCantidad() <= 0) p.getInv().quitarItem(pez);
                    f.activarSoborno();
                    anadirLog(p.getNombre() + " ha sobornat la foca!");
                    actualizarInventarioUI();
                    onDone.run();
                }, () -> {
                    aplicarCastigoFoca(p, onDone);
                });
        } else {
            // No tiene peces
            showSpecialTileMessage("NO TENS PEIXOS! RETROCEDEIXES!", "hole", () -> {
                aplicarCastigoFoca(p, onDone);
            });
        }
    }

    private void aplicarCastigoFoca(Jugador j, Runnable onDone) {
        int posAnterior = j.getPosicion();
        // Usamos el método que ya existe en la clase Tablero
        int posAgujero = gestorPartida.getPartida().getTablero().buscarAgujeroAnterior(posAnterior);
        j.setPosicion(posAgujero);
        anadirLog(j.getNombre() + " no té peixos i retrocedeix fins a la posició " + posAgujero);
        
        animarMovimiento(j, posAnterior, posAgujero, onDone);
    }

    private void mostrarAlert(AlertType tipus, String titol, String missatge) {
        Platform.runLater(() -> {
            Alert alert = new Alert(tipus);
            alert.setTitle(titol);
            alert.setHeaderText(null);
            alert.setContentText(missatge);
            alert.showAndWait();
        });
    }
}
