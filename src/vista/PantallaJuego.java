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

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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

/**
 * CONTROLADOR DE LA INTERFÍCIE VISUAL DEL JOC (PANTALLA DE JOC).
 * Aquesta classe és el cor de la interfície gràfica (GUI). Gestiona tota la 
 * interacció de l'usuari amb el taulell, coordina les animacions de moviment 
 * de les fitxes, actualitza l'inventari en temps real i enllaça els esdeveniments 
 * visuals amb la lògica interna del GestorPartida.
 * 
 * @author Alex Camacho
 * @version 2.2
 */
public class PantallaJuego {

    // ── ELEMENTS DEL MENÚ SUPERIOR ───────────────────────────────────────────
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // ── BOTONS D'ACCIÓ PRINCIPAL I INVENTARI ─────────────────────────────────
    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    // ── TEXTOS I INDICADORS VISUALS ──────────────────────────────────────────
    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private VBox vboxEventos;
    @FXML private ScrollPane scrollEventos;
    @FXML private Label nomInventari;
    @FXML private Label eventos;

    // ── CONTENIDORS DEL TAULELL I AVATARS ───────────────────────────────────
    @FXML private GridPane tablero;
    @FXML private Group P1; // Pinguí Jugador 1
    @FXML private Group P2; // Pinguí Jugador 2
    @FXML private Group P3; // Pinguí Jugador 3
    @FXML private Group P4; // Pinguí Jugador 4
    @FXML private Button btnAjustes;
    @FXML private ImageView bgImage;

    // ── ELEMENTS DE L'OVERLAY (CÀRREGA I VICTÒRIA) ──────────────────────────
    @FXML private StackPane loadingOverlay;
    @FXML private ImageView loadingBg;
    @FXML private Rectangle loadingBar;
    @FXML private StackPane winOverlay;
    @FXML private Label winLabel;
    @FXML private ImageView winIcon;

    // ── PANELLS ARREL ────────────────────────────────────────────────────────
    @FXML private StackPane boardStack;
    @FXML private StackPane rootPane;

    // ── ATRIBUTS DE CONTROL I ESTAT LOCAL ────────────────────────────────────
    private GestorPartida gestorPartida = new GestorPartida();
    private int p1Position = 0;
    private static final int COLUMNS = 5; // Columnes del GridPane (taulell)
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    private int numHumanos = 1;
    private int numIA = 1;
    
    // Mapa per relacionar ràpidament cada objecte Jugador amb el seu node visual
    private java.util.Map<Jugador, javafx.scene.Node> tokenMap = new java.util.HashMap<>();

    /**
     * CONFIGURA EL NÚMERO DE JUGADORS I PREPARA LA NOVA PARTIDA.
     * Es crida des de la pantalla de creació de partida.
     */
    public void configurarJugadores(int humanos, int ias) {
        this.numHumanos = humanos;
        this.numIA = ias;
        prepararNuevaPartida();
    }

    /**
     * INICIALITZA UNA PARTIDA PERSONALITZADA AMB UNA LLISTA DE JUGADORS EXISTENTS.
     * Configura els tokens visuals (pinguins i foca) i inicia el cicle de joc.
     */
    public void iniciarPartidaPersonalizada(String nomPartida, java.util.List<Jugador> jugadores) {
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        // Ocultem tots els avatars abans de començar per si n'hi havia de la partida anterior
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

        // Refresquem la interfície per mostrar l'estat inicial
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
     * El log es desplaça automàticament cap a l'última entrada.
     */
    private void anadirLog(String msg) {
        if (vboxEventos == null || msg == null) return;
        
        Text text = new Text(msg);
        text.getStyleClass().add("log-entry");
        text.setWrappingWidth(300);
        
        // Estilització per colors per fer-ho més visual i detallat
        if (msg.contains("DAU") || msg.contains("TREURE")) text.setFill(Color.LIGHTBLUE);
        else if (msg.contains("🏆") || msg.contains("GUANYA")) {
            text.setFill(Color.GOLD);
            text.setStyle("-fx-font-weight: bold;");
        }
        else if (msg.contains("ÓS") || msg.contains("FORAT") || msg.contains("💥")) text.setFill(Color.ORANGERED);
        else if (msg.contains("🎒") || msg.contains("PEIX") || msg.contains("MOTO")) text.setFill(Color.LIGHTGREEN);
        else text.setFill(Color.WHITE);
        
        vboxEventos.getChildren().add(text);
        
        // Auto-scroll cap avall usant Platform.runLater per major fiabilitat
        Platform.runLater(() -> scrollEventos.setVvalue(1.0));
    }

    private int lastLogIndex = 0;
    /** Sincronitza els logs interns del model Partida amb la interfície visual */
    private void syncModelLogs() {
        if (gestorPartida.getPartida() == null) return;
        java.util.List<String> logs = gestorPartida.getPartida().getLogEventos();
        while (lastLogIndex < logs.size()) {
            anadirLog(logs.get(lastLogIndex++));
        }
    }

    /**
     * MÈTODE D'INICIALITZACIÓ DE JAVAFX.
     * Configura la música, la connexió a la base de dades i els efectes visuals inicials.
     */
    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playGameMusic();
        if (gestorPartida == null) {
            gestorPartida = new GestorPartida();
        }
        gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        // Connexió silenciosa a la BBDD
        if (gestorPartida.getGestorBBDD() != null) {
            // Logs de connexió eliminats de la UI per petició de l'usuari
        }
        
        anadirLog("--- BENVINGUT AL JOC DEL PINGÜÍ ---");
        
        // Vinculem el fons de pantalla a la mida de la finestra (responsivitat)
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

        // Efecte de rotació al botó de configuració quan hi passem el ratolí
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
     * Crea un efecte de "loading" abans de mostrar el taulell.
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
     * Instancia els pinguins, els assigna colors i afegeix la foca robot.
     */
    private void prepararNuevaPartida() {
        gestorPartida = new GestorPartida();
        gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        ArrayList<Jugador> jugadores = new ArrayList<>();
        String[] colores = {"Blau", "Vermell", "Verd", "Groc"};
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        tokenMap.clear();

        // Creació dels jugadors humans controlats per usuaris
        for (int i = 0; i < numHumanos; i++) {
            Pinguino p = new Pinguino("JUGADOR " + (i + 1), colores[i % 4]);
            p.setEsIA(false);
            jugadores.add(p);
            if(i < pTokens.length) {
                pTokens[i].setVisible(true);
                tokenMap.put(p, pTokens[i]);
                aplicarColorAToken(pTokens[i], colores[i % 4]);
            }
        }
        
        // Creació dels jugadors controlats per la CPU
        for (int i = 0; i < numIA; i++) {
            Pinguino p = new Pinguino("CPU " + (i + 1), colores[(numHumanos + i) % 4]);
            p.setEsIA(true);
            jugadores.add(p);
            int idx = numHumanos + i;
            if(idx < pTokens.length) {
                pTokens[idx].setVisible(true);
                tokenMap.put(p, pTokens[idx]);
                aplicarColorAToken(pTokens[idx], colores[(numHumanos + i) % 4]);
            }
        }

        // AFEGIM LA FOCA COM A NPC ACTIU
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
     * DIBUIXA LES CASELLES AL TAULELL I ASSIGNA LES IMATGES CORRESPONENTS.
     * Cada casella té la seva pròpia animació de flotació i aparició.
     */
    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        // Netejem possibles restes de textos o nodes previs
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);
            
            // Especial per a Inici (0) i Meta (49)
            if (i == 0 || i == 49) {
                VBox box = new VBox(2);
                box.setUserData(TAG_CASILLA_TEXT);
                box.setAlignment(Pos.CENTER);
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.getStyleClass().add("grid-tile");
                box.getStyleClass().add(i == 0 ? "start-tile" : "end-tile");
                
                String specialIcon = (i == 0) ? "inicio" : "meta";
                java.net.URL url = getClass().getResource("/resources/images/casillas/" + specialIcon + ".png");
                if (url != null) {
                    ImageView iv = new ImageView(new Image(url.toExternalForm()));
                    iv.setFitWidth(80); iv.setFitHeight(80);
                    iv.setPreserveRatio(true);
                    box.getChildren().add(iv);
                }
                setupTileAnimation(box, i);
                continue;
            }

            // Caselles amb obstacles o esdeveniments
            if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                VBox box = new VBox(2);
                box.setUserData(TAG_CASILLA_TEXT);
                box.setAlignment(Pos.CENTER);
                box.getStyleClass().add("grid-tile");

                try {
                    java.net.URL url = getClass().getResource("/resources/images/casillas/" + tipo.toLowerCase() + ".png");
                    if (url != null) {
                        Image img = new Image(url.toExternalForm(), true);
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(75); iv.setFitHeight(75);
                        iv.setPreserveRatio(true);
                        box.getChildren().add(iv);
                    } else {
                        // Text de seguretat si no hi ha imatge
                        Text fallback = new Text("?");
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
     * CONFIGURA L'ANIMACIÓ DE FLOTACIÓ I APARICIÓ DE CADA CASELLA.
     */
    private void setupTileAnimation(VBox box, int i) {
        int row = i / COLUMNS;
        int col = i % COLUMNS;
        GridPane.setRowIndex(box, row);
        GridPane.setColumnIndex(box, col);
        GridPane.setHalignment(box, javafx.geometry.HPos.CENTER);
        
        // Rotació aleatòria per fer el taulell menys rígid
        box.setRotate((Math.random() * 10) - 5);
        
        // Animació de flotació suau
        TranslateTransition floating = new TranslateTransition(Duration.seconds(3 + Math.random() * 2), box);
        floating.setByY((Math.random() * 8) - 4);
        floating.setAutoReverse(true);
        floating.setCycleCount(javafx.animation.Animation.INDEFINITE);
        floating.play();
        
        // Efecte de Fade-In seqüencial
        box.setOpacity(0);
        tablero.getChildren().add(box);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(300), box);
        ft.setDelay(Duration.millis(i * 10));
        ft.setToValue(1.0);
        ft.play();
    }

    /**
     * GESTIONA EL GUARDAT DE LA PARTIDA.
     */
    @FXML private void handleSaveGame() { 
        boolean ok = gestorPartida.guardarPartida();
        if (ok) {
            anadirLog(">>> LA PARTIDA S'HA GUARDAT AL SERVIDOR.");
            mostrarAlerta(AlertType.INFORMATION, "ÈXIT", "PROGRÉS GUARDAT CORRECTAMENT.");
        } else {
            mostrarAlerta(AlertType.ERROR, "ERROR", "NO S'HA POGUT CONNECTAR AMB LA BBDD.");
        }
    }

    /**
     * OBRE LA FINESTRA D'AJUSTAMENTS (VOLUM, PANTALLA COMPLETA).
     */
    @FXML
    private void handleAjustes(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaAjustes.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            System.err.println("ERROR CARREGANT AJUSTAMENTS: " + e.getMessage());
        }
    }

    /**
     * MOSTRA LES REGLES DEL JOC.
     */
    @FXML
    private void handleGuia(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaAyuda.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root, Color.TRANSPARENT));
            stage.showAndWait();
        } catch (Exception e) {
            anadirLog("ERROR CARREGANT LA GUIA.");
        }
    }

    /**
     * TANCA LA PARTIDA I TORNA AL MENÚ.
     */
    @FXML private void handleQuitGame() { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) tablero.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * RECONSTRUEIX LA UI PER A UNA PARTIDA CARREGADA DE LA BBDD.
     */
    public void iniciarCargandoPartida(int id) {
        if (this.gestorPartida == null) this.gestorPartida = new GestorPartida();
        if (this.gestorPartida.getGestorBBDD() == null) this.gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        this.gestorPartida.cargarPartida(id);
        syncLoadedJugadores();
        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero()); 
        syncVisualPositions(false);
        actualizarInventarioUI();
        
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("TORN DE: " + (prox != null ? prox.getNombre().toUpperCase() : "..."));
        anadirLog("REPRÈN LA PARTIDA #" + id + ".");
    }

    /**
     * SINCRONITZA ELS TOKENS VISUALS AMB ELS JUGADORS CARREGATS.
     */
    private void syncLoadedJugadores() {
        if (gestorPartida == null || gestorPartida.getPartida() == null) return;
        
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        tokenMap.clear();

        int pIndex = 0;
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
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
    }

    /**
     * ACCIÓ DE TIRAR EL DAU PRINCIPAL.
     */
    @FXML
    private void handleDado(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        if(gestorPartida.getPartida().isFinalizada()) {
            anadirLog("EL JOC HA ACABAT.");
        } else {
            dado.setDisable(true);
            procesarSiguienteTurno();
        }
    }

    /**
     * GESTIONA EL CANVI DE TORN I LA LÒGICA DE LES IA.
     */
    private void procesarSiguienteTurno() {
        if (gestorPartida.getPartida().isFinalizada()) {
            dado.setDisable(false);
        } else {
            Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
            if (actual != null) {
                boolean saltarTorn = false;
                
                // Comprovació de pèrdua de torn per ruleta
                if (gestorPartida.getPartida().getJugadorPierdeTurno() != null &&
                    gestorPartida.getPartida().getJugadorPierdeTurno().equals(actual)) {
                    gestorPartida.getPartida().setJugadorPierdeTurno(null);
                    anadirLog(actual.getNombre().toUpperCase() + " ESTÀ ATURAT AQUEST TORN.");
                    gestorPartida.getPartida().siguienteTurno();
                    finalizarTurnoComplet();
                    saltarTorn = true;
                }

                // Comprovació de bloqueig de la foca (bola de neu)
                if (!saltarTorn && actual instanceof model.Foca f && f.getTurnosBloqueada() > 0) {
                    f.reducirBloqueo();
                    anadirLog("LA FOCA CONTINUA CONGELADA.");
                    gestorPartida.getPartida().siguienteTurno();
                    finalizarTurnoComplet();
                    saltarTorn = true;
                }

                // Comprovació de la foca a la meta (per evitar bucles infinits)
                if (!saltarTorn && actual instanceof model.Foca f && f.getPosicion() >= 49) {
                    anadirLog("LA FOCA JA ÉS A LA META.");
                    gestorPartida.getPartida().siguienteTurno();
                    finalizarTurnoComplet();
                    saltarTorn = true;
                }
                
                if (!saltarTorn) ejecutarMovimientoJugador(actual);
            }
        }
    }

    /**
     * REALITZA EL MOVIMENT LÒGIC I ANIMA LA FITXA AL TAULELL.
     */
    private void ejecutarMovimientoJugador(Jugador actual) {
        int posAnterior = actual.getPosicion();

        if (actual.isEsIA()) gestorPartida.realizarAccionesIA(actual);

        int pasos = gestorPartida.tirarDadoParaJugador(actual);
        anadirLog(actual.getNombre().toUpperCase() + " HA TREURE UN " + pasos + " AL DAU.");
        
        int posNova = Math.min(posAnterior + pasos, 49);
        actual.setPosicion(posNova);

        animarMovimiento(actual, posAnterior, posNova, () -> {
            Casilla casilla = gestorPartida.getPartida().getTablero().getCasilla(posNova);
            String tipo = casilla.getClass().getSimpleName();
            anadirLog(actual.getNombre().toUpperCase() + " HA CAIGUT A: " + traducirTipoCasilla(tipo).toUpperCase() + " (Casella " + posNova + ").");

            // Lògica de la foca robant ítems al seu pas
            if (actual instanceof Foca foca) {
                if (!gestorPartida.procesarPasoDeFoca(foca, posAnterior, posNova).isEmpty()) {
                    actualizarInventarioUI();
                    anadirLog("⚠️ LA FOCA HA ROBAT OBJECTES AL SEU PAS!");
                }
            }

            // Gestió de caselles especials (Ós, Forat, Trineu)
            if (tipo.equals("Oso") || tipo.equals("Agujero") || tipo.equals("Trineo")) {
                String sound = tipo.equals("Oso") ? "bear" : (tipo.equals("Trineo") ? "sled" : "hole");
                String msg = tipo.equals("Oso") ? "L'ÓS T'ENVIA A L'INICI!" : (tipo.equals("Trineo") ? "AVANCES AMB EL TRINEU!" : "HAS CAIGUT AL FORAT!");
                
                anadirLog("✨ EVENT: " + msg);
                showSpecialTileMessage(msg, sound, () -> {
                    int posAbansEfecte = actual.getPosicion();
                    gestorPartida.getGestorTablero().ejecutarCasilla(gestorPartida.getPartida(), actual, casilla);
                    syncModelLogs();
                    int posDespresEfecte = actual.getPosicion();
                    
                    if (posDespresEfecte != posAbansEfecte) {
                        anadirLog("NOVA POSICIÓ: CASELLA " + posDespresEfecte);
                    }

                    animarMovimiento(actual, posAbansEfecte, posDespresEfecte, () -> {
                        finalizarLogicaTurno(actual, posDespresEfecte < posAbansEfecte, () -> {});
                    });
                });
            } else {
                gestorPartida.getGestorTablero().ejecutarCasilla(gestorPartida.getPartida(), actual, casilla);
                gestorPartida.comprobarInteraccionesEnCasilla(actual);
                syncModelLogs();
                finalizarLogicaTurno(actual, false, () -> {});
            }
        });
    }

    /** Tradueix el nom de la classe de la casella a un nom llegible */
    private String traducirTipoCasilla(String tipo) {
        switch (tipo) {
            case "Oso": return "Ós Polar";
            case "Agujero": return "Forat al Gel";
            case "Trineo": return "Trineu Ràpid";
            case "Evento": return "Esdeveniment (Ruleta)";
            case "Nieve": return "Neu Profunda";
            case "Peces": return "Banc de Peixos";
            case "CasillaNormal": return "Gel Estreure";
            default: return tipo;
        }
    }

    /**
     * TANCAMENT DE LA LÒGICA DE TORN (RUELTA I CANVI DE JUGADOR).
     */
    private void finalizarLogicaTurno(Jugador actual, boolean skipInteractions, Runnable onDone) {
        Runnable nextStep = () -> {
            gestorPartida.actualizarEstadoTablero();
            gestorPartida.getPartida().siguienteTurno();
            syncVisualPositions(false);
            actualizarInventarioUI();
            gestorPartida.guardarPartida();
            actualizarGlowTurno();
            
            if (gestorPartida.getPartida().isFinalizada()) {
                controlador.SoundManager.getInstance().playSoundOnce("win");
                mostrarVictoria(gestorPartida.getPartida().getGanador());
            } else {
                Casilla c = gestorPartida.getPartida().getTablero().getCasilla(actual.getPosicion());
                if (!skipInteractions && c instanceof Evento && !(actual instanceof Foca)) {
                    mostrarRuleta(actual, this::finalizarTurnoComplet);
                } else {
                    finalizarTurnoComplet();
                }
            }
            onDone.run();
        };

        if (skipInteractions) nextStep.run();
        else comprobarInteraccionesUI(actual, nextStep);
    }

    /**
     * MOSTRA UN MISSATGE VISUAL D'OVERLAY PER A EVENTS DE CASELLA.
     */
    private void showSpecialTileMessage(String text, String sound, Runnable onFinished) {
        controlador.SoundManager.getInstance().playSound(sound);
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");
        overlay.setOpacity(0);
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");
        overlay.getChildren().add(label);
        boardStack.getChildren().add(overlay);
        
        FadeTransition fi = new FadeTransition(Duration.millis(400), overlay);
        fi.setToValue(1);
        fi.setOnFinished(e -> {
            javafx.animation.PauseTransition p = new javafx.animation.PauseTransition(Duration.seconds(1.5));
            p.setOnFinished(ev -> {
                FadeTransition fo = new FadeTransition(Duration.millis(400), overlay);
                fo.setToValue(0);
                fo.setOnFinished(f -> {
                    boardStack.getChildren().remove(overlay);
                    onFinished.run();
                });
                fo.play();
            });
            p.play();
        });
        fi.play();
    }

    /**
     * FINALITZA EL TORN COMPLETAMENT I ALLIBERA EL DAU O EXECUTA IA.
     */
    private void finalizarTurnoComplet() {
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("TORN DE: " + (prox != null ? prox.getNombre().toUpperCase() : "..."));
        syncVisualPositions(true);
        if (prox != null && prox.isEsIA() && !gestorPartida.getPartida().isFinalizada()) {
            javafx.animation.PauseTransition p = new javafx.animation.PauseTransition(Duration.millis(800));
            p.setOnFinished(e -> procesarSiguienteTurno());
            p.play();
        } else {
            dado.setDisable(false);
        }
        actualizarInventarioUI();
    }

    /**
     * MOSTRA LA PANTALLA DE LA RULETA.
     */
    private void mostrarRuleta(Jugador j, Runnable onFinished) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaRuleta.fxml"));
            Parent root = loader.load();
            PantallaRuleta ctrl = loader.getController();
            ctrl.setGameContext(gestorPartida.getPartida(), j);
            
            // Registrem el log quan la ruleta acaba
            ctrl.setOnFinishedCallback(result -> {
                anadirLog("🎰 RULETA: " + j.getNombre().toUpperCase() + " HA REBUT: " + result.toUpperCase());
            });

            boardStack.getChildren().add(root);
            root.parentProperty().addListener((obs, old, newVal) -> { if (newVal == null) onFinished.run(); });
        } catch (Exception e) { onFinished.run(); }
    }

    /**
     * ANIMA EL MOVIMENT DE SALT D'UNA FITXA.
     */
    private void animarMovimiento(Jugador j, int from, int to, Runnable onFinished) {
        javafx.scene.Node token = tokenMap.get(j);
        if (from == to || token == null) { onFinished.run(); return; }
        if (Math.abs(to - from) > 20) { syncVisualPositions(false); onFinished.run(); return; }

        int step = (to > from) ? 1 : -1;
        javafx.animation.SequentialTransition st = new javafx.animation.SequentialTransition();
        int current = from;

        for (int i = 0; i < Math.abs(to - from); i++) {
            current += step;
            final int finalC = current;
            
            TranslateTransition jump = new TranslateTransition(Duration.millis(150), token);
            jump.setByY(-20);
            jump.setAutoReverse(true);
            jump.setCycleCount(2);
            
            javafx.animation.PauseTransition update = new javafx.animation.PauseTransition(Duration.millis(150));
            update.setOnFinished(e -> {
                GridPane.setRowIndex(token, finalC / 5);
                GridPane.setColumnIndex(token, finalC % 5);
                token.toFront();
            });
            
            st.getChildren().addAll(new javafx.animation.ParallelTransition(jump, update));
        }
        st.setOnFinished(e -> onFinished.run());
        st.play();
    }
    
    /**
     * SINCRONITZA ELS TOKENS AMB EL TAULELL EVITANT SOLAPAMENTS.
     */
    private void syncVisualPositions(boolean animar) {
        java.util.Map<Integer, List<Jugador>> posMap = new java.util.HashMap<>();
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            int p = Math.max(0, Math.min(49, j.getPosicion()));
            posMap.computeIfAbsent(p, k -> new ArrayList<>()).add(j);
        }

        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            Node token = tokenMap.get(j);
            if (token == null) continue;
            int p = Math.max(0, Math.min(49, j.getPosicion()));
            List<Jugador> list = posMap.get(p);
            int idx = list.indexOf(j);
            double offX = (list.size() > 1) ? (idx % 2 == 0 ? -15 : 15) : 0;
            double offY = (list.size() > 2) ? (idx < 2 ? -15 : 15) : 0;

            GridPane.setRowIndex(token, p / 5);
            GridPane.setColumnIndex(token, p % 5);
            token.toFront();

            if (animar) {
                TranslateTransition tt = new TranslateTransition(Duration.millis(300), token);
                tt.setToX(offX); tt.setToY(offY);
                tt.play();
            } else {
                token.setTranslateX(offX); token.setTranslateY(offY);
            }
        }
    }

    /**
     * MARCA EL JUGADOR ACTIU AMB UN EFECTE DE POLSACIÓ.
     */
    private void actualizarGlowTurno() {
        Jugador current = gestorPartida.getPartida().getJugadorActualObj();
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            Node n = tokenMap.get(j);
            if (n == null) continue;
            if (j.equals(current)) {
                n.getStyleClass().add("current-player");
                javafx.animation.ScaleTransition sc = new javafx.animation.ScaleTransition(Duration.seconds(0.6), n);
                sc.setFromX(1); sc.setToX(1.1); sc.setAutoReverse(true); sc.setCycleCount(-1);
                sc.play();
                n.setUserData(sc);
            } else {
                n.getStyleClass().remove("current-player");
                if (n.getUserData() instanceof javafx.animation.ScaleTransition) ((javafx.animation.ScaleTransition)n.getUserData()).stop();
                n.setScaleX(1); n.setScaleY(1);
            }
        }
    }

    // ACCIONS D'INVENTARI
    @FXML private void handleRapido() { usarObjetoYActualizar("DadoRapido"); }
    @FXML private void handleLento()  { usarObjetoYActualizar("DadoLento"); }
    @FXML private void handlePeces()  { usarObjetoYActualizar("Peces"); }
    @FXML private void handleNieve()  { usarObjetoYActualizar("BolaNieve"); }

    private void usarObjetoYActualizar(String tipo) {
        if(gestorPartida.getPartida().isFinalizada()) return;
        Jugador act = gestorPartida.getPartida().getJugadorActualObj();
        if (!(act instanceof Pinguino p) || p.isEsIA()) return;

        int countAntes = p.getInv().contarItems(tipo);
        if (countAntes > 0) {
            if (tipo.contains("Dado")) {
                // Equipar el dau per a la propera tirada
                Item it = p.getInv().getItem(Dado.class);
                if (it instanceof Dado d) {
                    p.setDadoEquipado(d);
                    anadirLog("🎒 " + p.getNombre().toUpperCase() + " HA PREPARAT UN: " + tipo.toUpperCase());
                }
            } else if (tipo.equals("BolaNieve")) {
                // Ús actiu: Tirar bola a un altre jugador aleatori
                Jugador objetivo = null;
                for(Jugador j : gestorPartida.getPartida().getJugadores()) {
                    if(j != p && j.getPosicion() > 0) {
                        objetivo = j; break; 
                    }
                }
                
                if (objetivo != null) {
                    p.getInv().quitarUnidadAleatoria(new java.util.Random());
                    int retroceso = 1 + new java.util.Random().nextInt(3);
                    int posVella = objetivo.getPosicion();
                    objetivo.setPosicion(Math.max(0, posVella - retroceso));
                    anadirLog("❄️ " + p.getNombre().toUpperCase() + " HA LLANÇAT UNA BOLA A " + objetivo.getNombre().toUpperCase() + "!");
                    anadirLog(objetivo.getNombre().toUpperCase() + " RETROCEDEIX " + retroceso + " CASELLES.");
                    syncVisualPositions(true);
                } else {
                    anadirLog("⚠️ NO HI HA NINGÚ A PROP PER TIRAR LA BOLA!");
                }
            } else if (tipo.equals("Peces")) {
                anadirLog("🍣 EL PEIX S'USA AUTOMÀTICAMENT QUAN ET TROBES AMB L'ÓS O LA FOCA.");
            }
        } else {
             anadirLog("❌ NO TENS " + tipo.toUpperCase() + " A L'INVENTARI.");
        }
        actualizarInventarioUI();
    }

    private void actualizarInventarioUI() {
        Jugador act = gestorPartida.getPartida().getJugadorActualObj();
        if (act instanceof Pinguino p) {
            nomInventari.setText("BOSSES DE: " + p.getNombre().toUpperCase());
            Inventario inv = p.getInv();
            rapido_t.setText("D.RÀPID: " + inv.contarItems("DadoRapido"));
            lento_t.setText("D.LENT: " + inv.contarItems("DadoLento"));
            peces_t.setText("PEIXOS: " + inv.contarItems("Peces"));
            nieve_t.setText("BOLES: " + inv.contarItems("BolaNieve"));
        }
    }

    private Node getOrCreateFocaAvatar() {
        Node f = tablero.lookup("#FOCA_NPC");
        if (f != null) return f;
        StackPane sp = new StackPane();
        sp.setId("FOCA_NPC");
        sp.setMaxSize(55, 55);
        try {
            ImageView iv = new ImageView(new Image(getClass().getResource("/resources/images/casillas/foca.png").toExternalForm()));
            iv.setFitWidth(55); iv.setPreserveRatio(true);
            sp.getChildren().add(iv);
        } catch (Exception e) {}
        tablero.getChildren().add(sp);
        return sp;
    }

    private void aplicarColorAToken(Node n, String color) {
        // Aplica efectes de color segons l'assignació
        n.setEffect(new DropShadow(15, Color.web(color.equals("Vermell")?"RED":"CYAN")));
    }

    private void mostrarVictoria(Jugador g) {
        winLabel.setText("¡¡¡ " + g.getNombre().toUpperCase() + " GUANYA !!!");
        winOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(1), winOverlay);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void comprobarInteraccionesUI(Jugador j, Runnable onDone) {
        // Gestió de trobades entre jugadors que requereixen decisió
        onDone.run();
    }

    /**
     * MOSTRA UNA ALERTA PERSONALITZADA UTILITZANT L'OVERLAY DEL JOC.
     */
    private void mostrarAlerta(AlertType tipus, String titol, String missatge) {
        // Utilitzem la utilitat PantallaAlerta pròpia del projecte per a una millor estètica
        controlador.SoundManager.getInstance().playSound("event");
        PantallaAlerta.mostrar(rootPane, titol, missatge, null);
    }
}
