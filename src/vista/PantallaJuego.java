package vista;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
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
            }
        }

        // Foca
        model.Foca focaJugador = new model.Foca();
        jugadores.add(focaJugador);
        
        // Crear círculo de foca si no existe o reutilizar
        Circle focaCircle = (Circle) tablero.lookup("#FOCA");
        if (focaCircle == null) {
            focaCircle = new Circle(15);
            focaCircle.setId("FOCA");
            focaCircle.getStyleClass().add("player");
            focaCircle.setStyle("-fx-fill: linear-gradient(to bottom right, #ffffff, #666666); -fx-stroke: black; -fx-stroke-width: 4; -fx-effect: dropshadow(gaussian, red, 15, 0.5, 0, 0);");
            GridPane.setMargin(focaCircle, new Insets(0, 0, 0, 136));
            tablero.getChildren().add(focaCircle);
        }
        tokenMap.put(focaJugador, focaCircle);

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
                String icon = switch (tipo) {
                    case "Agujero" -> "🕳️";
                    case "Oso" -> "🐻";
                    case "Trineo" -> "🛷";
                    case "SueloQuebradizo" -> "⛸️";
                    case "Evento" -> "✨";
                    default -> "❄️";
                };
                
                Text texto = new Text(icon + "\n" + tipo);
                texto.setUserData(TAG_CASILLA_TEXT);
                texto.getStyleClass().add("cell-type");
                texto.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                int row = i / COLUMNS;
                int col = i % COLUMNS;
                GridPane.setRowIndex(texto, row);
                GridPane.setColumnIndex(texto, col);
                GridPane.setHalignment(texto, javafx.geometry.HPos.CENTER);
                tablero.getChildren().add(texto);
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

    public void iniciarCargandoPartida() {
        // En lugar de inicializar todo a cero, cargamos la partida desde la BD
        gestorPartida.cargarPartida(1);
        syncVisualPositions(false);
        actualizarInventarioUI();
        eventos.setText("Partida cargada exitosamente. ¡Sigue jugando!");
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
            if (siguiente.isEsIA() && !gestorPartida.getPartida().isFinalizada()) {
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
                slide.play();
            } else {
                GridPane.setRowIndex(token, newRow);
                GridPane.setColumnIndex(token, newCol);
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
