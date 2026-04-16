package vista;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.IOException;

import controlador.GestorPartida;
import model.Casilla;
import model.Dado;
import model.Evento;
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
    @FXML private Text eventos;

    // Game board and player pieces
    @FXML private GridPane tablero;
    @FXML private Group P1;
    @FXML private Group P2;
    @FXML private Group P3;
    @FXML private Group P4;

    // Containers
    @FXML private StackPane boardStack;

    private GestorPartida gestorPartida;
    private int p1Position = 0;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";
    private final Random rand = new Random();

    @FXML
    private void initialize() {
        eventos.setText("¡El juego ha comenzado!");

        gestorPartida = new GestorPartida();

        ArrayList<Jugador> jugadores = new ArrayList<>();
        Inventario inventario = new Inventario();
        Dado dadoItem = new Dado("normal", 1, 1, 6);
        inventario.getLista().add(dadoItem);

        jugadores.add(new Pinguino("Jugador1", "Blau", 0, inventario));

        gestorPartida.nuevaPartida();
        gestorPartida.getPartida().setJugadores(jugadores);

        mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero());
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
    @FXML private void handleNewGame()  { System.out.println("New game.");    }
    @FXML private void handleSaveGame() { System.out.println("Saved game."); }
    @FXML private void handleLoadGame() { System.out.println("Loaded game.");}
    @FXML private void handleQuitGame() { System.out.println("Exit...");     }

    // Button actions
    @FXML
    private void handleDado(ActionEvent event) {
        Pinguino pingu = (Pinguino) gestorPartida.getPartida().getJugadores().get(0);
        Dado d = (Dado) pingu.getInv().getLista().get(0);

        System.out.println("Pos pingu prèvia: " + pingu.getPosicion());

        int resultado = gestorPartida.tirarDado(pingu, d);

        System.out.println("Pos pingu actual: " + pingu.getPosicion());

        dadoResultText.setText("Ha sortit: " + resultado);
        moveP1(resultado);
    }

    private void moveP1(int steps) {
        dado.setDisable(true);

        int oldPosition = p1Position;
        p1Position += steps;
        if (p1Position >= 50) p1Position = 49;
        if (p1Position < 0)   p1Position = 0;

        int oldRow = oldPosition / COLUMNS;
        int oldCol = oldPosition % COLUMNS;
        int newRow = p1Position / COLUMNS;
        int newCol = p1Position % COLUMNS;

        double cellWidth  = tablero.getWidth()  / COLUMNS;
        double cellHeight = tablero.getHeight() / 10;

        double dx = (newCol - oldCol) * cellWidth;
        double dy = (newRow - oldRow) * cellHeight;

        TranslateTransition slide = new TranslateTransition(Duration.millis(350), P1);
        slide.setByX(dx);
        slide.setByY(dy);
        slide.setOnFinished(e -> {
            P1.setTranslateX(0);
            P1.setTranslateY(0);
            GridPane.setRowIndex(P1, newRow);
            GridPane.setColumnIndex(P1, newCol);
            dado.setDisable(false);

            // COMPROBAR CASILLA
            Casilla c = gestorPartida.getPartida().getTablero().getCasilla(p1Position);
            if (c instanceof Evento) {
                mostrarRuleta();
            } else {
                c.realizarAccion(gestorPartida.getPartida(), (Pinguino) gestorPartida.getPartida().getJugadores().get(0));
            }
            refreshUI();
        });
        slide.play();
    }

    private void mostrarRuleta() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaRuleta.fxml"));
            StackPane ruletaOverlay = loader.load();
            PantallaRuleta controller = loader.getController();

            // Configurar el callback cuando termine el giro
            controller.setOnFinishedCallback(premio -> {
                aplicarPremio(premio);
            });

            boardStack.getChildren().add(ruletaOverlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void aplicarPremio(String premio) {
        Pinguino p = (Pinguino) gestorPartida.getPartida().getJugadores().get(0);
        String mensaje = "";

        switch (premio) {
            case "Peix":
                p.getInv().anadirItem(new model.Pez(1));
                mensaje = "¡Has guanyat un peix!";
                break;
            case "Dau ràpid":
                p.getInv().anadirItem(new model.Dado("Dado Ràpid", 1, 5, 10));
                mensaje = "¡Has guanyat un dau ràpid (5-10)!";
                break;
            case "Dau lent":
                p.getInv().anadirItem(new model.Dado("Dado Lent", 1, 1, 3));
                mensaje = "¡Has guanyat un dau lent (1-3)!";
                break;
            case "Boles de neu":
                int numBolas = new Random().nextInt(3) + 1;
                p.getInv().anadirItem(new model.BolaDeNieve(numBolas));
                mensaje = "¡Has guanyat " + numBolas + " boles de neu!";
                break;
        }

        eventos.setText(mensaje);
        refreshUI();
    }

    private void refreshUI() {
        Pinguino p = (Pinguino) gestorPartida.getPartida().getJugadores().get(0);
        Inventario inv = p.getInv();

        // Contar ítems
        int numPeces = 0;
        int numNieve = 0;
        int numRapido = 0;
        int numLento = 0;

        for (Item item : inv.getLista()) {
            if (item instanceof model.Pez) numPeces += item.getCantidad();
            else if (item instanceof model.BolaDeNieve) numNieve += item.getCantidad();
            else if (item instanceof model.Dado d) {
                if (d.getNombre().toLowerCase().contains("ràpid")) numRapido++;
                else if (d.getNombre().toLowerCase().contains("lent")) numLento++;
            }
        }

        peces_t.setText("Peces: " + numPeces);
        nieve_t.setText("Bolas de nieve: " + numNieve);
        rapido_t.setText("Dado rápido: " + numRapido);
        lento_t.setText("Dado lento: " + numLento);
    }

    @FXML private void handleRapido() { System.out.println("Fast.");  /* TODO */ }
    @FXML private void handleLento()  { System.out.println("Slow.");  /* TODO */ }
    @FXML private void handlePeces()  { System.out.println("Fish.");  /* TODO */ }
    @FXML private void handleNieve()  { System.out.println("Snow.");  /* TODO */ }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
    }
}
