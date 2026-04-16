package vista;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import controlador.GestorPartida;
import model.Casilla;
import model.Dado;
import model.Inventario;
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
    @FXML private Text eventos;

    // Game board and player pieces
    @FXML private GridPane tablero;
    @FXML private Group P1;
    @FXML private Group P2;
    @FXML private Group P3;
    @FXML private Group P4;
    @FXML private ImageView bgImage;

    private GestorPartida gestorPartida;
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

    @FXML
    private void initialize() {
        eventos.setText("¡El juego ha comenzado!");        // BIND BACKGROUND SIZE (set managed false to prevent infinite loop/zoom)
        if (bgImage != null) {
            bgImage.setManaged(false);
            bgImage.fitWidthProperty().bind(((StackPane)bgImage.getParent()).widthProperty());
            bgImage.fitHeightProperty().bind(((StackPane)bgImage.getParent()).heightProperty());
        }
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
        
        // Crear círculo de foca si no existe o reutilizar
        javafx.scene.Node focaAvatar = tablero.lookup("#FOCA");
        if (focaAvatar == null) {
            StackPane sp = new StackPane();
            sp.setId("FOCA");
            sp.getStyleClass().add("player");
            
            Circle bg = new Circle(18);
            bg.setStyle("-fx-fill: linear-gradient(to bottom right, #ffffff, #808080); -fx-stroke: #333333; -fx-stroke-width: 2;");
            
            Text emoji = new Text("🦭");
            emoji.setStyle("-fx-font-size: 20px;");
            
            sp.getChildren().addAll(bg, emoji);
            // El margen anterior de 136px posiblemente desplazaba la foca fuera de la casilla
            GridPane.setHalignment(sp, javafx.geometry.HPos.CENTER);
            GridPane.setValignment(sp, javafx.geometry.VPos.CENTER);
            tablero.getChildren().add(sp);
            focaAvatar = sp;
        }
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
                String imagePath = switch (tipo) {
                    case "Agujero" -> "/resources/images/casillas/agujero.png";
                    case "Oso" -> "/resources/images/casillas/oso.png";
                    case "Trineo" -> "/resources/images/casillas/trineo.png";
                    case "SueloQuebradizo" -> "/resources/images/casillas/suelo_quebradizo.png";
                    case "Evento" -> "/resources/images/casillas/normal.png"; // Usamos normal como base si no hay imagen propia
                    default -> "/resources/images/casillas/normal.png";
                };
                
                VBox box = new VBox(2);
                box.setUserData(TAG_CASILLA_TEXT);
                box.setAlignment(Pos.CENTER);
                // Hacemos que la casilla ocupe todo el espacio disponible en la celda
                box.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                box.setPadding(new Insets(2));
                
                // Fondo totalmente transparente sin color sólido, solo un borde sutil opcional
                box.setStyle("-fx-background-color: transparent; " +
                             "-fx-border-color: rgba(255, 255, 255, 0.15); " +
                             "-fx-border-width: 0.5;");

                try {
                    // Intento de carga de imagen (si se encontraran)
                    java.net.URL url = getClass().getResource("/resources/images/casillas/" + tipo.toLowerCase() + ".png");
                    if (url == null) url = getClass().getResource("/images/casillas/" + tipo.toLowerCase() + ".png");

                    if (url != null) {
                        Image img = new Image(url.toExternalForm(), true);
                        ImageView iv = new ImageView(img);
                        iv.setFitWidth(45); // Un poco más grandes
                        iv.setFitHeight(45);
                        iv.setPreserveRatio(true);
                        
                        if ("Evento".equals(tipo)) {
                            StackPane stack = new StackPane();
                            stack.getChildren().add(iv);
                            Text icon = new Text("✨");
                            icon.setStyle("-fx-font-size: 18px; -fx-fill: gold;");
                            stack.getChildren().add(icon);
                            box.getChildren().add(stack);
                        } else if ("Oso".equals(tipo)) {
                            StackPane stack = new StackPane();
                            iv.setStyle("-fx-effect: innershadow(gaussian, red, 10, 0.5, 0, 0);");
                            stack.getChildren().add(iv);
                            Text angry = new Text("💢");
                            angry.setStyle("-fx-font-size: 16px;");
                            StackPane.setAlignment(angry, Pos.TOP_RIGHT);
                            stack.getChildren().add(angry);
                            box.getChildren().add(stack);
                        } else {
                            box.getChildren().add(iv);
                        }
                    } else {
                        // Emoji grande y claro si no hay imagen
                        String emojiText = switch (tipo) {
                            case "Agujero" -> "🕳️";
                            case "Oso" -> "🐻";
                            case "Trineo" -> "🛷";
                            case "SueloQuebradizo" -> "⛸️";
                            case "Evento" -> "🎁";
                            default -> "🧊";
                        };
                        Text fallback = new Text(emojiText);
                        fallback.setStyle("-fx-font-size: 32px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 5, 0, 0, 1);");
                        box.getChildren().add(fallback);
                    }
                } catch (Exception e) {
                    box.getChildren().add(new Text("?"));
                }

                Label label = new Label(tipo.toUpperCase());
                label.getStyleClass().add("cell-type");
                label.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; -fx-effect: dropshadow(one-pass-box, black, 2, 0, 0, 1);");
                box.getChildren().add(label);

                int row = i / COLUMNS;
                int col = i % COLUMNS;
                GridPane.setRowIndex(box, row);
                GridPane.setColumnIndex(box, col);
                GridPane.setHalignment(box, javafx.geometry.HPos.CENTER);
                tablero.getChildren().add(box);
            }
        }
    }

    // Menu actions
    @FXML private void handleNewGame()  { 
        gestorPartida.nuevaPartida(); 
        syncVisualPositions(false);
        eventos.setText("Nueva partida iniciada.");
        dadoResultText.setText("-");
    }
    @FXML private void handleSaveGame() { 
        gestorPartida.guardarPartida();
        eventos.setText("Partida guardada en BBDD manual.");
    }
    @FXML private void handleLoadGame() { 
        gestorPartida.cargarPartida(1);
        syncVisualPositions(false);
        actualizarInventarioUI();
        eventos.setText("Partida cargada desde BBDD.");
    }
    @FXML private void handleQuitGame() { System.exit(0); }

    public void iniciarCargandoPartida(int id) {
        // Cargamos la partida desde la BD con el ID seleccionado
        gestorPartida.cargarPartida(id);
        syncVisualPositions(false);
        actualizarInventarioUI();
        eventos.setText("Partida #" + id + " carregada exitosament.");
    }

    // Button actions
    @FXML
    private void handleDado(ActionEvent event) {
        if(gestorPartida.getPartida().isFinalizada()) {
            eventos.setText("El juego ya ha terminado.");
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
            System.out.println("Error: No hay jugador actual definido.");
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
            dadoResultText.setText("Turno de: " + actual.getNombre());
            java.util.List<String> logs = gestorPartida.getPartida().getLogEventos();
            if(!logs.isEmpty()) eventos.setText(logs.get(logs.size()-1));
            
            actualizarInventarioUI();
            gestorPartida.guardarPartida();

            // 4. Siguiente turno (IA o Humano)
            Jugador siguiente = gestorPartida.getPartida().getJugadorActualObj();
            if (siguiente != null && siguiente.isEsIA() && !gestorPartida.getPartida().isFinalizada()) {
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.millis(800));
                pause.setOnFinished(e -> procesarSiguienteTurno());
                pause.play();
            } else {
                dado.setDisable(false);
            }
        });
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
            javafx.animation.PauseTransition stepPause = new javafx.animation.PauseTransition(Duration.millis(200));
            stepPause.setOnFinished(e -> {
                int f = finalCurrent;
                if (f < 0) f = 0; if (f >= 50) f = 49;
                GridPane.setRowIndex(token, f / COLUMNS);
                GridPane.setColumnIndex(token, f % COLUMNS);
            });
            seq.getChildren().add(stepPause);
        }

        seq.setOnFinished(e -> onFinished.run());
        seq.play();
    }
    

    /**
     * Sincroniza todas las fichas en la vista con sus posiciones lógicas.
     */
    private void syncVisualPositions(boolean animar) {
        for (Jugador j : gestorPartida.getPartida().getJugadores()) {
            javafx.scene.Node token = tokenMap.get(j);
            if (token == null) continue;

            int pos = j.getPosicion();
            if (pos >= 50) pos = 49;
            if (pos < 0) pos = 0;

            int newRow = pos / COLUMNS;
            int newCol = pos % COLUMNS;

            if (animar) {
                javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(Duration.millis(350), token);
                token.setTranslateX(0);
                token.setTranslateY(0);
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
            }
        }
    }

    @FXML private void handleRapido() { 
        eventos.setText("Has usado Dado rápido."); 
        usarObjetoYActualizar("DadoRapido");
    }
    @FXML private void handleLento()  { 
        eventos.setText("Has usado Dado lento."); 
        usarObjetoYActualizar("DadoLento");
    }
    @FXML private void handlePeces()  { 
        eventos.setText("Has comido Peces (+ vida o energía)."); 
        usarObjetoYActualizar("Peces");
    }
    @FXML private void handleNieve()  { 
        eventos.setText("Has lanzado una Bola de nieve."); 
        usarObjetoYActualizar("BolaNieve");
    }

    private void usarObjetoYActualizar(String tipo) {
        // Lógica simplificada: en un futuro interactuará con gestorPartida.usarItem(...)
        actualizarInventarioUI();
    }

    private void actualizarInventarioUI() {
        // Actualiza los textos de la UI leyendo el inventario del jugador humano
        if(gestorPartida.getPartida().getJugadores().size() > 0) {
           Jugador j0 = gestorPartida.getPartida().getJugadores().get(0);
           if (j0 instanceof Pinguino) {
               Inventario inv = ((Pinguino) j0).getInv();
               if(inv != null) {
                   // Aquí deberíamos contar cuántos items de cada tipo tiene
                   // Por simplicidad en la UI:
                   rapido_t.setText("Dado rápido: " + inv.contarItems("DadoRapido"));
                   lento_t.setText("Dado lento: " + inv.contarItems("DadoLento"));
                   peces_t.setText("Peces: " + inv.contarItems("Peces"));
                   nieve_t.setText("Bolas nieve: " + inv.contarItems("BolaNieve"));
               }
           }
        }
    }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
    }

    /** Permite a la PantallaMenu inyectar el usuario logueado como Jugador 1 */
    public void setUsuarioLogueado(String username) {
        if (gestorPartida != null && gestorPartida.getPartida() != null) {
            Jugador j1 = gestorPartida.getPartida().getJugadores().get(0);
            if (j1 != null) {
                j1.setNombre(username);
            }
        }
    }
}
