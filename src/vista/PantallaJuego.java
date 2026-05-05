package vista;

import java.util.ArrayList;
import java.util.Random;

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


import controlador.GestorPartida;
import model.Pez;
import model.BolaDeNieve;
import model.Casilla;
import model.Dado;
import model.Evento;
import model.Foca;
import model.Inventario;
import model.Item;
import model.Jugador;
import model.Pinguino;
import model.Tablero;

public class PantallaJuego {


    // Menu items
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // Buttons
    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    // Texts
    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private VBox vboxEventos;
    @FXML private ScrollPane scrollEventos;
    @FXML private Label nomInventari;

    // Game board and player pieces
    @FXML private GridPane tablero;
    @FXML private Group P1;
    @FXML private Group P2;
    @FXML private Group P3;
    @FXML private Group P4;
    @FXML private Button btnAjustes;
    @FXML private ImageView bgImage;


    // Premium UI Elements
    @FXML private StackPane loadingOverlay;
    @FXML private ImageView loadingBg;
    @FXML private Rectangle loadingBar;
    @FXML private StackPane winOverlay;
    @FXML private Label winLabel;

    // Containers
    @FXML private StackPane boardStack;

    private GestorPartida gestorPartida = new GestorPartida();
    private int p1Position = 0;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    private int numHumanos = 1;
    private int numIA = 1;
    private java.util.Map<Jugador, javafx.scene.Node> tokenMap = new java.util.HashMap<>();

    public void configurarJugadores(int humanos, int ias) {
        this.numHumanos = humanos;
        this.numIA = ias;
        prepararNuevaPartida();
    }

    public void iniciarPartidaPersonalizada(String nomPartida, java.util.List<Jugador> jugadores) {
        // gestorPartida ya está inicializado como campo o en initialize()
        
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        // Ocultar todos los tokens primero
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        tokenMap.clear();

        int pIndex = 0;
        for (Jugador j : jugadores) {
            if (j instanceof Pinguino) {
                if (pIndex < pTokens.length) {
                    pTokens[pIndex].setVisible(true);
                    tokenMap.put(j, pTokens[pIndex]);
                    
                    // Aplicar color visual al token (opcional, pero mejora la experiencia)
                    // Como el Pinguino tiene un color String, podríamos mapearlo a colores de JavaFX si quisiéramos.
                    // Por ahora mantendremos los tokens originales P1, P2...
                    
                    pIndex++;
                }
            } else if (j instanceof Foca) {
                javafx.scene.Node focaAvatar = getOrCreateFocaAvatar();
                tokenMap.put(j, focaAvatar);
            }
        }

        gestorPartida.nuevaPartida(); // Genera el tablero
        gestorPartida.getPartida().setNombre(nomPartida);
        gestorPartida.getPartida().setJugadores(new ArrayList<>(jugadores));

        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero());
        syncVisualPositions(false);
        actualizarInventarioUI();
        actualizarGlowTurno();
        
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("Torn de: " + (prox != null ? prox.getNombre() : "..."));
        anadirLog("Partida '" + nomPartida + "' començada!");
    }

    private void anadirLog(String msg) {
        if (vboxEventos == null) return;
        
        Text text = new Text(msg);
        text.getStyleClass().add("log-entry");
        text.setWrappingWidth(300);
        vboxEventos.getChildren().add(text);
        
        // Auto-scroll to bottom
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(50));
        pause.setOnFinished(e -> scrollEventos.setVvalue(1.0));
        pause.play();
    }

    @FXML
    private void initialize() {
        controlador.SoundManager.getInstance().playGameMusic();
        if (gestorPartida == null) {
            gestorPartida = new GestorPartida();
        }
        gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        anadirLog("--- Benvingut al Joc del Pingüí ---");
        anadirLog("El joc ha començat!");
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

    private void prepararNuevaPartida() {
        gestorPartida = new GestorPartida();
        gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        
        ArrayList<Jugador> jugadores = new ArrayList<>();
        String[] colores = {"Azul", "Rojo", "Verde", "Amarillo"};
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        // Ocultar todos los tokens primero
        for(javafx.scene.Node n : pTokens) if(n != null) n.setVisible(false);
        
        tokenMap.clear();

        for (int i = 0; i < numHumanos; i++) {
            Pinguino p = new Pinguino("Jugador " + (i + 1), colores[i % 4]);
            p.setEsIA(false);
            jugadores.add(p);
            if(i < pTokens.length) {
                pTokens[i].setVisible(true);
                tokenMap.put(p, pTokens[i]);
                GridPane.setHalignment(pTokens[i], javafx.geometry.HPos.CENTER);
                GridPane.setValignment(pTokens[i], javafx.geometry.VPos.CENTER);
            }
        }
        
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

        // Foca
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

    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);

            if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                String imagePath;
                switch (tipo) {
                    case "Agujero": imagePath = "/resources/images/casillas/agujero.png"; break;
                    case "Oso": imagePath = "/resources/images/casillas/oso.png"; break;
                    case "Trineo": imagePath = "/resources/images/casillas/trineo.png"; break;
                    case "SueloQuebradizo": imagePath = "/resources/images/casillas/suelo_quebradizo.png"; break;
                    case "Evento": imagePath = "/resources/images/casillas/normal.png"; break;
                    default: imagePath = "/resources/images/casillas/normal.png"; break;
                }
                
                VBox box = new VBox(2);
                box.setUserData(TAG_CASILLA_TEXT);
                box.setAlignment(Pos.CENTER);
                // Hacemos que la casilla ocupe todo el espacio disponible en la celda
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.setPadding(new Insets(5));
                // Fondo semi-transparente estilo "Glassmorphism" premium con bordes redondeados
                box.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(255, 255, 255, 0.4), rgba(255, 255, 255, 0.1)); " +
                             "-fx-background-radius: 15; " +
                             "-fx-border-color: rgba(255, 255, 255, 0.7); " +
                             "-fx-border-radius: 15; " +
                             "-fx-border-width: 2; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 8);");

                try {
                    // Intento de carga de imagen (si se encontraran)
                    java.net.URL url = getClass().getResource("/resources/images/casillas/" + tipo.toLowerCase() + ".png");
                    if (url == null) url = getClass().getResource("/images/casillas/" + tipo.toLowerCase() + ".png");

                    if (url != null) {
                        Image img = new Image(url.toExternalForm(), true);
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(50); // Un pelín más contenido para dar margen visual
                        iv.setFitHeight(50);
                        iv.setPreserveRatio(true);
                        
                        if ("Evento".equals(tipo)) {
                            StackPane stack = new StackPane();
                            stack.getChildren().add(iv);
                            Text icon = new Text("?");
                            icon.setStyle("-fx-font-size: 18px; -fx-fill: gold;");
                            stack.getChildren().add(icon);
                            box.getChildren().add(stack);
                        } else if ("Oso".equals(tipo)) {
                            StackPane stack = new StackPane();
                            iv.setStyle("-fx-effect: innershadow(gaussian, red, 10, 0.5, 0, 0);");
                            stack.getChildren().add(iv);
                            box.getChildren().add(stack);
                        } else {
                            box.getChildren().add(iv);
                        }
                    } else {
                        // Text curt si no hi ha imatge
                        String emojiText;
                        switch (tipo) {
                            case "Agujero": emojiText = "FORAT"; break;
                            case "Oso": emojiText = "OS"; break;
                            case "Trineo": emojiText = "TRINEU"; break;
                            case "SueloQuebradizo": emojiText = "FRÀGIL"; break;
                            case "Evento": emojiText = "SORPRESA"; break;
                            default: emojiText = "?"; break;
                        }
                        Text fallback = new Text(emojiText);
                        fallback.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 5, 0, 0, 1);");
                        box.getChildren().add(fallback);
                    }
                } catch (Exception e) {
                    box.getChildren().add(new Text("?"));
                }

                String displayTipo;
                switch (tipo) {
                    case "Agujero": displayTipo = "FORAT"; break;
                    case "Oso": displayTipo = "OS"; break;
                    case "Trineo": displayTipo = "TRINEU"; break;
                    case "SueloQuebradizo": displayTipo = "TERRA FRÀGIL"; break;
                    case "Evento": displayTipo = "SORPRESA"; break;
                    default: displayTipo = "NORMAL"; break;
                }

                // Etiqueta (Label) bonita para la casilla
                Label label = new Label(displayTipo);
                label.getStyleClass().add("cell-type");
                label.setStyle("-fx-background-color: linear-gradient(to right, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0.5)); " + 
                               "-fx-background-radius: 8; " + 
                               "-fx-padding: 3 8 3 8; " + 
                               "-fx-text-fill: white; " + 
                               "-fx-font-size: 11px; " + 
                               "-fx-font-family: 'Segoe UI', sans-serif; " +
                               "-fx-font-weight: 800; " +
                               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 2, 0, 0, 1);");
                box.getChildren().add(label);

                int row = i / COLUMNS;
                int col = i % COLUMNS;
                GridPane.setRowIndex(box, row);
                GridPane.setColumnIndex(box, col);
                GridPane.setHalignment(box, javafx.geometry.HPos.CENTER);
                
                // Animación de entrada para las casillas
                box.setOpacity(0);
                box.setScaleX(0); box.setScaleY(0);
                tablero.getChildren().add(box);
                
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(Duration.millis(300), box);
                ft.setDelay(Duration.millis(i * 10));
                ft.setToValue(1.0);
                
                javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(Duration.millis(400), box);
                st.setDelay(Duration.millis(i * 10));
                st.setToX(1); st.setToY(1);
                
                ft.play(); st.play();
            }
        }
    }

    // Menu actions
    @FXML private void handleSaveGame() { 
        gestorPartida.guardarPartida();
        anadirLog("Partida guardada a la BBDD.");
    }

    @FXML
    private void handleAjustes(ActionEvent event) {
        System.out.println("DEBUG: Obriu ajustes com overlay dende Joc...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaAjustes.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            
            // Intentar obtener la ventana desde el evento o desde el tablero
            javafx.stage.Window owner = null;
            if (event != null && event.getSource() instanceof Node) {
                Node node = (Node) event.getSource();
                stage.initOwner(node.getScene().getWindow());
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
            System.err.println("ERROR carregant Ajustes dende Joc: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleQuitGame() { 
        try {
            // Regresar al menú principal (Login)
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) tablero.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("El Joc del Pingüí - Menú");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint(""); 
        } catch (Exception e) {
            System.out.println("Error al salir: " + e.getMessage());
            System.exit(0);
        }
    }

    public void iniciarCargandoPartida(int id) {
        System.out.println("DEBUG: Iniciant càrrega de partida " + id);
        if (this.gestorPartida == null) {
            System.out.println("DEBUG: gestorPartida era null, inicialitzant...");
            this.gestorPartida = new GestorPartida();
        }
        if (this.gestorPartida.getGestorBBDD() == null) {
            this.gestorPartida.setGestorBBDD(new controlador.GestorBBDD());
        }
        
        // Cargamos la partida desde la BD con el ID seleccionado
        this.gestorPartida.cargarPartida(id);
        syncLoadedJugadores();
        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero()); // Redibuixar caselles
        syncVisualPositions(false);
        actualizarInventarioUI();
        
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("Torn de: " + (prox != null ? prox.getNombre() : "..."));
        anadirLog("Partida #" + id + " carregada correctament.");
    }

    // Button actions
    @FXML
    private void handleDado(ActionEvent event) {
        controlador.SoundManager.getInstance().playSound("click");
        if(gestorPartida.getPartida().isFinalizada()) {
            anadirLog("El joc ja ha acabat.");
            return;
        }

        dado.setDisable(true);
        procesarSiguienteTurno();
    }

    private void procesarSiguienteTurno() {
        if (gestorPartida.getPartida().isFinalizada()) {
            dado.setDisable(false);
            return;
        }

        Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
        if (actual == null) {
            System.out.println("Error: No hi ha jugador actual definit.");
            dado.setDisable(false);
            return;
        }
        int posAnterior = actual.getPosicion();

        // 1. Acciones IA
        if (actual.isEsIA()) {
            gestorPartida.realizarAccionesIA(actual);
        }

        // 2. Lógica
        gestorPartida.ejecutarTurnoCompleto();
        int posNueva = actual.getPosicion();

        // 3. Animación paso a paso
        animarMovimiento(actual, posAnterior, posNueva, () -> {
            Jugador proxSiguiente = gestorPartida.getPartida().getJugadorActualObj();
            dadoResultText.setText("Torn de: " + (proxSiguiente != null ? proxSiguiente.getNombre() : "..."));
            
            java.util.List<String> logs = gestorPartida.getPartida().getLogEventos();
            if(!logs.isEmpty()) {
                String lastMsg = logs.get(logs.size()-1);
                anadirLog(lastMsg);
                showToast(lastMsg, "#00d2ff");
            }
            
            syncVisualPositions(true);
            actualizarInventarioUI();
            gestorPartida.guardarPartida();
            actualizarGlowTurno();
            
            // Comprobar victoria
            if (gestorPartida.getPartida().isFinalizada()) {
                controlador.SoundManager.getInstance().playSoundOnce("win");
                mostrarVictoria(gestorPartida.getPartida().getGanador());
                return;
            }

            // 4. Sonidos de aterrizaje
            Casilla casillaActual = gestorPartida.getPartida().getTablero().getCasilla(actual.getPosicion());
            String tipo = casillaActual.getClass().getSimpleName();
            switch (tipo) {
                case "Oso": controlador.SoundManager.getInstance().playSound("bear"); break;
                case "Agujero": controlador.SoundManager.getInstance().playSound("hole"); break;
                case "Trineo": controlador.SoundManager.getInstance().playSound("sled"); break;
                case "SueloQuebradizo": controlador.SoundManager.getInstance().playSound("ice"); break;
                case "Evento": controlador.SoundManager.getInstance().playSound("event"); break;
            }

            // 5. Comprovar si ha caigut en casella sorpresa per mostrar la ruleta
            if (casillaActual instanceof Evento) {
                mostrarRuleta(actual, this::finalizarTurnoComplet);
            } else {
                finalizarTurnoComplet();
            }
        });
    }

    private void finalizarTurnoComplet() {
        Jugador proxSiguienteTurno = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("Torn de: " + (proxSiguienteTurno != null ? proxSiguienteTurno.getNombre() : "..."));
        
        java.util.List<String> logs = gestorPartida.getPartida().getLogEventos();
        if(!logs.isEmpty()) anadirLog(logs.get(logs.size()-1));

        if (proxSiguienteTurno != null && proxSiguienteTurno.isEsIA() && !gestorPartida.getPartida().isFinalizada()) {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> procesarSiguienteTurno());
            pause.play();
        } else {
            dado.setDisable(false);
        }
        actualizarInventarioUI();
    }

    private void mostrarRuleta(Jugador j, Runnable onFinished) {
        try {
            if (boardStack == null) {
                System.err.println("Error: boardStack es null. Revisa la FXML.");
                onFinished.run();
                return;
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/PantallaRuleta.fxml"));
            javafx.scene.Parent ruletaRoot = loader.load();
            PantallaRuleta controller = loader.getController();
            
            controller.setGameContext(gestorPartida.getPartida(), j);
            
            // Afegim la ruleta al StackPane principal per sobre del taulell
            boardStack.getChildren().add(ruletaRoot);
            
            // Per detectar que s'ha tancat, podem mirar si ruletaRoot segueix sent fill
            ruletaRoot.parentProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    onFinished.run();
                }
            });

        } catch (Exception e) {
            System.err.println("Error al carregar la ruleta: " + e.getMessage());
            e.printStackTrace();
            onFinished.run();
        }
    }

    private void animarMovimiento(Jugador j, int from, int to, Runnable onFinished) {
        javafx.scene.Node token = tokenMap.get(j);
        if (from == to || token == null) {
            onFinished.run();
            return;
        }

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition();
        int current = from;
        int step = (to > from) ? 1 : -1;

        // Si es un salto brusco (Oso, Trineo...), sincronizamos directamente al final
        if (Math.abs(to - from) > 10) {
            syncVisualPositions(true);
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
            if (token == null) continue;

            int pos = j.getPosicion();
            if (pos >= 50) pos = 49;
            if (pos < 0) pos = 0;

            int newRow = pos / COLUMNS;
            int newCol = pos % COLUMNS;

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
            Dado dadoElegido = null;
            for (Item i : inv.getLista()) {
                if (i instanceof Dado) {
                    Dado d = (Dado) i;
                    if (tipo.equals("DadoRapido") && d.getNombre().toLowerCase().contains("rapido")) {
                        dadoElegido = d; break;
                    }
                    if (tipo.equals("DadoLento") && d.getNombre().toLowerCase().contains("lento")) {
                        dadoElegido = d; break;
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
                    gestorPartida.getPartida().siguienteTurno();
                    procesarSiguienteTurno();
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
            if (actual instanceof model.Foca || (actual instanceof Pinguino && ((Pinguino) actual).isEsIA())) {
                for(Jugador j : gestorPartida.getPartida().getJugadores()) {
                    if(j instanceof Pinguino && !((Pinguino) j).isEsIA()) {
                        mostrar = j;
                        break;
                    }
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
            for (String p : paths) {
                java.net.URL url = getClass().getResource(p);
                if (url != null) {
                    img = new Image(url.toExternalForm());
                    break;
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
            }
        }
    }

    private void mostrarVictoria(Jugador ganador) {
        if (winOverlay == null) return;
        winLabel.setText(ganador.getNombre().toUpperCase() + " HA GUANYAT!");
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

    /** Permet a la PantallaMenu injectar l'usuari loguejat com Jugador 1 */
    public void setUsuarioLogueado(String username) {
        if (gestorPartida != null && gestorPartida.getPartida() != null) {
            for (Jugador j : gestorPartida.getPartida().getJugadores()) {
                if (j instanceof Pinguino) {
                    Pinguino p = (Pinguino) j;
                    if (!p.isEsIA()) {
                        p.setNombre(username);
                        break;
                    }
                }
            }
            syncVisualPositions(false);
            actualizarInventarioUI();
        }
    }

    
}
