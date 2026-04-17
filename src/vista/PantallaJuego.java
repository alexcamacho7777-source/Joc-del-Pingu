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
import java.io.IOException;

import controlador.GestorPartida;
import model.Item;
import model.Pez;
import model.BolaDeNieve;
import model.Casilla;
import model.Dado;
import model.Evento;
import model.Inventario;
import model.Item;
import model.Jugador;
import model.Pinguino;
import model.Tablero;
import model.Foca;

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
    @FXML private Label nomInventari;

    // Game board and player pieces
    @FXML private GridPane tablero;
    @FXML private Group P1;
    @FXML private Group P2;
    @FXML private Group P3;
    @FXML private Group P4;
    @FXML private ImageView bgImage;

    // Containers
    @FXML private StackPane boardStack;

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
        eventos.setText("El joc ha començat!");        // BIND BACKGROUND SIZE (set managed false to prevent infinite loop/zoom)
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

                String displayTipo = switch (tipo) {
                    case "Agujero" -> "FORAT";
                    case "Oso" -> "OS";
                    case "Trineo" -> "TRINEU";
                    case "SueloQuebradizo" -> "TERRA FRÀGIL";
                    case "Evento" -> "SORPRESA";
                    default -> "NORMAL";
                };
                Label label = new Label(displayTipo);
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
    @FXML private void handleSaveGame() { 
        gestorPartida.guardarPartida();
        eventos.setText("Partida guardada a la BBDD.");
    }

    @FXML private void handleQuitGame() { 
        try {
            // Regresar al menú principal (Login)
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) tablero.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("El Joc del Pingüí - Menú");
            stage.setMaximized(false);
        } catch (Exception e) {
            System.out.println("Error al salir: " + e.getMessage());
            System.exit(0);
        }
    }

    public void iniciarCargandoPartida(int id) {
        // Cargamos la partida desde la BD con el ID seleccionado
        gestorPartida.cargarPartida(id);
        syncLoadedJugadores(); // ¡Esencial para que los pingüinos se muevan!
        syncVisualPositions(false);
        actualizarInventarioUI();
        
        Jugador prox = gestorPartida.getPartida().getJugadorActualObj();
        dadoResultText.setText("Torn de: " + (prox != null ? prox.getNombre() : "..."));
        eventos.setText("Partida #" + id + " carregada correctament.");
    }

    // Button actions
    @FXML
    private void handleDado(ActionEvent event) {
        if(gestorPartida.getPartida().isFinalizada()) {
            eventos.setText("El joc ja ha acabat.");
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
            if(!logs.isEmpty()) eventos.setText(logs.get(logs.size()-1));
            
            actualizarInventarioUI();
            gestorPartida.guardarPartida();

            // 4. Siguiente turno (IA o Humano)
            Jugador proxSiguienteTurno = gestorPartida.getPartida().getJugadorActualObj();
            if (proxSiguienteTurno != null && proxSiguienteTurno.isEsIA() && !gestorPartida.getPartida().isFinalizada()) {
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
        eventos.setText("Has fet servir Dau ràpid."); 
        usarObjetoYActualizar("DadoRapido");
    }
    @FXML private void handleLento()  { 
        eventos.setText("Has fet servir Dau lent."); 
        usarObjetoYActualizar("DadoLento");
    }
    @FXML private void handlePeces()  { 
        eventos.setText("Has menjat Peixos (+ vida o energia)."); 
        usarObjetoYActualizar("Peces");
    }
    @FXML private void handleNieve()  { 
        eventos.setText("Has llançat una Bola de neu."); 
        usarObjetoYActualizar("BolaNieve");
    }

    private void usarObjetoYActualizar(String tipo) {
        if(gestorPartida.getPartida().isFinalizada()) return;
        
        Jugador actual = gestorPartida.getPartida().getJugadorActualObj();
        if (!(actual instanceof Pinguino p) || p.isEsIA()) {
            eventos.setText("No pots usar objectes si no és el teu torn.");
            return;
        }

        Inventario inv = p.getInv();
        if (inv == null) return;

        // Caso Dados: Usar e iniciar turno
        if (tipo.equals("DadoRapido") || tipo.equals("DadoLento")) {
            Item item = inv.getItem(Dado.class); // Simplificación: busca el primero de clase Dado que coincida
            // En una implementación más compleja buscaríamos exactamente el tipo.
            // Por ahora, buscaremos el dado que coincida con el nombre.
            Dado dadoElegido = null;
            for (Item i : inv.getLista()) {
                if (i instanceof Dado d) {
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
                eventos.setText("No tens aquest objecte!");
            }
        } 
        // Caso Otros: Usar y no pasar turno inmediatamente? (Depende de la regla, usualmente Peces se usa proactivamente)
        else {
            if (tipo.equals("Peces")) {
                Item it = inv.getItem(model.Pez.class);
                if (it != null && it.getCantidad() > 0) {
                    it.setCantidad(it.getCantidad() - 1);
                    if (it.getCantidad() <= 0) inv.quitarItem(it);
                    eventos.setText(p.getNombre() + " ha menjat peixos!");
                } else {
                    eventos.setText("No tens peixos!");
                }
            } else if (tipo.equals("BolaNieve")) {
                Item it = inv.getItem(model.BolaDeNieve.class);
                if (it != null && it.getCantidad() > 0) {
                    it.setCantidad(it.getCantidad() - 1);
                    if (it.getCantidad() <= 0) inv.quitarItem(it);
                    eventos.setText(p.getNombre() + " ha llançat una bola de neu!");
                } else {
                    eventos.setText("No tens boles de neu!");
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
            if (actual instanceof model.Foca || (actual instanceof Pinguino pin && pin.isEsIA())) {
                for(Jugador j : gestorPartida.getPartida().getJugadores()) {
                    if(j instanceof Pinguino p && !p.isEsIA()) {
                        mostrar = j;
                        break;
                    }
                }
            }
            
            if (mostrar instanceof Pinguino p) {
                nomInventari.setText("PROPIETARI: " + p.getNombre().toUpperCase());
                
                String colorHex = switch(p.getColor().toLowerCase()) {
                    case "azul", "blau" -> "#0072ff";
                    case "rojo", "vermell" -> "#ff4b2b";
                    case "verde", "verd" -> "#11998e";
                    case "amarillo", "groc" -> "#f2c94c";
                    default -> "#00d2ff";
                };
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
    private void syncLoadedJugadores() {
        if (gestorPartida == null || gestorPartida.getPartida() == null) return;
        
        java.util.List<Jugador> jugadores = gestorPartida.getPartida().getJugadores();
        javafx.scene.Node[] pTokens = {P1, P2, P3, P4};
        
        tokenMap.clear();
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            if (j instanceof model.Foca) {
                javafx.scene.Node focaAvatar = tablero.lookup("#FOCA");
                if (focaAvatar != null) tokenMap.put(j, focaAvatar);
            } else if (i < pTokens.length && pTokens[i] != null) {
                pTokens[i].setVisible(true);
                tokenMap.put(j, pTokens[i]);
            }
        }
    }

    /** Permet a la PantallaMenu injectar l'usuari loguejat com Jugador 1 */
    public void setUsuarioLogueado(String username) {
        if (gestorPartida != null && gestorPartida.getPartida() != null) {
            for (Jugador j : gestorPartida.getPartida().getJugadores()) {
                if (j instanceof Pinguino p && !p.isEsIA()) {
                    p.setNombre(username);
                    break;
                }
            }
            syncVisualPositions(false);
            actualizarInventarioUI();
        }
    }
}
