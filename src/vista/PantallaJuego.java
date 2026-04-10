package vista;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
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
    @FXML private Circle P1;
    @FXML private Circle P2;
    @FXML private Circle P3;
    @FXML private Circle P4;

    private GestorPartida gestorPartida;
    private int p1Position = 0;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    private Circle[] tokens;

    @FXML
    private void initialize() {
        eventos.setText("¡El juego ha comenzado!");

        gestorPartida = new GestorPartida();

        // Crear 4 jugadores + CPU Foca
        ArrayList<Jugador> jugadores = new ArrayList<>();
        
        Inventario inv1 = new Inventario();
        inv1.getLista().add(new Dado("normal", 1, 1, 6));
        jugadores.add(new Pinguino("Jugador 1 (Azul)", "Azul", 0, inv1));
        
        Inventario inv2 = new Inventario();
        inv2.getLista().add(new Dado("normal", 1, 1, 6));
        jugadores.add(new Pinguino("Jugador 2 (Rojo)", "Rojo", 0, inv2));

        Inventario inv3 = new Inventario();
        inv3.getLista().add(new Dado("normal", 1, 1, 6));
        jugadores.add(new Pinguino("Jugador 3 (Verde)", "Verde", 0, inv3));

        Inventario inv4 = new Inventario();
        inv4.getLista().add(new Dado("normal", 1, 1, 6));
        jugadores.add(new Pinguino("Jugador 4 (Amari)", "Amarillo", 0, inv4));

        model.Foca focaJugador = new model.Foca();
        jugadores.add(focaJugador);

        gestorPartida.nuevaPartida();
        gestorPartida.getPartida().setJugadores(jugadores);

        // Crear ficha de la foca programáticamente
        Circle focaCircle = new Circle(15);
        focaCircle.setId("FOCA");
        focaCircle.getStyleClass().add("player");
        focaCircle.setStyle("-fx-fill: linear-gradient(to bottom right, #ffffff, #666666); -fx-stroke: black; -fx-stroke-width: 4; -fx-effect: dropshadow(gaussian, red, 15, 0.5, 0, 0);");
        GridPane.setMargin(focaCircle, new Insets(0, 0, 0, 136));
        tablero.getChildren().add(focaCircle);

        tokens = new Circle[]{P1, P2, P3, P4, focaCircle};

        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero());
        syncVisualPositions(false);
    }

    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);

            if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                Text texto = new Text(tipo);
                texto.setUserData(TAG_CASILLA_TEXT);
                texto.getStyleClass().add("cell-type");

                int row = i / COLUMNS;
                int col = i % COLUMNS;
                GridPane.setRowIndex(texto, row);
                GridPane.setColumnIndex(texto, col);
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
    @FXML private void handleSaveGame() { System.out.println("Saved game."); }
    @FXML private void handleLoadGame() { System.out.println("Loaded game.");}
    @FXML private void handleQuitGame() { System.out.println("Exit...");     }

    // Button actions
    @FXML
    private void handleDado(ActionEvent event) {
        if(gestorPartida.getPartida().isFinalizada()) {
            eventos.setText("El juego ya ha terminado.");
            return;
        }

        // Tirada actual (sea humano o CPU dependiendo a quién le toque)
        boolean seguirTurnoCpu = true;
        
        while (seguirTurnoCpu && !gestorPartida.getPartida().isFinalizada()) {
            Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
            dado.setDisable(true);
            
            // Ejecutamos el turno real
            gestorPartida.ejecutarTurnoCompleto();
            
            // Actualizar textos
            dadoResultText.setText("Turno completado por: " + actual.getNombre());
            java.util.List<String> logs = gestorPartida.getPartida().getLogEventos();
            if(!logs.isEmpty()) eventos.setText(logs.get(logs.size()-1));

            // Sincronizar visualmente
            syncVisualPositions(true);
            
            Jugador siguiente = gestorPartida.getPartida().getJugadorActualObj();
            // Si el siguiente es Foca, el while hará su turno automáticamente
            if (siguiente instanceof model.Foca) {
                seguirTurnoCpu = true;
                // Pequeña pausa visual no bloqueante real (simulada)
            } else {
                seguirTurnoCpu = false; 
                dado.setDisable(false); // Es el turno de un pingüino humano
            }
        }
    }

    /**
     * Sincroniza todas las fichas en la vista con sus posiciones lógicas.
     */
    private void syncVisualPositions(boolean animar) {
        ArrayList<Jugador> jugadores = gestorPartida.getPartida().getJugadores();

        for (int i = 0; i < jugadores.size() && i < tokens.length; i++) {
            Jugador j = jugadores.get(i);
            int pos = j.getPosicion();
            
            if (pos >= 50) pos = 49;
            if (pos < 0) pos = 0;

            int newRow = pos / COLUMNS;
            int newCol = pos % COLUMNS;

            if (animar) {
                TranslateTransition slide = new TranslateTransition(Duration.millis(350), tokens[i]);
                tokens[i].setTranslateX(0);
                tokens[i].setTranslateY(0);
                GridPane.setRowIndex(tokens[i], newRow);
                GridPane.setColumnIndex(tokens[i], newCol);
            } else {
                GridPane.setRowIndex(tokens[i], newRow);
                GridPane.setColumnIndex(tokens[i], newCol);
                tokens[i].setTranslateX(0);
                tokens[i].setTranslateY(0);
            }

            if(gestorPartida.getPartida().getJugadorActual() == i && !gestorPartida.getPartida().isFinalizada()) {
                if(!tokens[i].getStyleClass().contains("current-player")) {
                    tokens[i].getStyleClass().add("current-player");
                }
            } else {
                tokens[i].getStyleClass().remove("current-player");
            }
        }
    }

    @FXML private void handleRapido() { System.out.println("Fast.");  /* TODO */ }
    @FXML private void handleLento()  { System.out.println("Slow.");  /* TODO */ }
    @FXML private void handlePeces()  { System.out.println("Fish.");  /* TODO */ }
    @FXML private void handleNieve()  { System.out.println("Snow.");  /* TODO */ }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
    }
}
