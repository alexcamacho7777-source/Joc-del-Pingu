package vista;

import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Rectangle2D;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import javafx.scene.shape.Rectangle;
import model.*;

public class PantallaRuleta {

    @FXML private StackPane root;
    @FXML private HBox sliderHBox;
    @FXML private Label resultLabel;
    @FXML private Button spinButton;
    @FXML private Button closeButton;

    private final Random random = new Random();
    private Consumer<String> onFinishedCallback;
    private boolean isSpinning = false;
    private Partida partida;
    private Jugador jugador;

    private static final double ITEM_WIDTH = 140.0;
    private static final int STRIP_SIZE = 60; // Total items in the strip
    private static final int WINNING_INDEX = 50; // Index of the winning item in the strip

    private final String[] possibleEvents = {
        "Dau lent",
        "Boles de neu",
        "Peix",
        "Dau ràpid",
        "Perdre un torn",
        "Moto de neu"
    };

    // Probabilities (total 100)
    // Dau lent: 30, Boles: 20, Peix: 15, Dau ràpid: 15, Perdre torn: 10, Moto: 10
    private final int[] probabilities = {30, 20, 15, 15, 10, 10};

    @FXML
    public void initialize() {
        setupInitialSlider();
    }

    private void setupInitialSlider() {
        sliderHBox.getChildren().clear();
        for (int i = 0; i < STRIP_SIZE; i++) {
            String event = possibleEvents[random.nextInt(possibleEvents.length)];
            sliderHBox.getChildren().add(createItemNode(event));
        }
        sliderHBox.setTranslateX(0);
    }

    private String getFileNameForEvent(String eventName) {
        return switch (eventName) {
            case "Dau lent" -> "event_dau_lent.png";
            case "Boles de neu" -> "event_boles.png";
            case "Peix" -> "event_peix.png";
            case "Dau ràpid" -> "event_dau_rapid.png";
            case "Perdre un torn" -> "event_perdre_torn.png";
            case "Moto de neu" -> "event_moto.png";
            default -> null;
        };
    }

    private VBox createItemNode(String eventName) {
        VBox item = new VBox();
        item.getStyleClass().add("slider-item");
        
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("item-icon-container");
        
        try {
            String fileName = getFileNameForEvent(eventName);
            if (fileName != null) {
                java.net.URL url = getClass().getResource("/resources/" + fileName);
                if (url != null) {
                    Image img = new Image(url.toExternalForm());
                    
                    // Usem la imatge com a fons per poder aplicar "cover" (omplir tot el recuadro)
                    javafx.scene.layout.BackgroundSize backgroundSize = new javafx.scene.layout.BackgroundSize(
                        100, 100, true, true, false, true
                    );
                    javafx.scene.layout.BackgroundImage backgroundImage = new javafx.scene.layout.BackgroundImage(
                        img, 
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT, 
                        javafx.scene.layout.BackgroundRepeat.NO_REPEAT, 
                        javafx.scene.layout.BackgroundPosition.CENTER, 
                        backgroundSize
                    );
                    iconContainer.setBackground(new javafx.scene.layout.Background(backgroundImage));
                }
            }
        } catch (Exception e) {
            Label fallback = new Label("?");
            fallback.setStyle("-fx-font-size: 40; -fx-text-fill: #00d2ff;");
            iconContainer.getChildren().add(fallback);
        }
        
        if (iconContainer.getChildren().isEmpty()) {
            Label fallback = new Label("?");
            fallback.setStyle("-fx-font-size: 40; -fx-text-fill: #00d2ff;");
            iconContainer.getChildren().add(fallback);
        }
        
        Label label = new Label(eventName.toUpperCase());
        label.getStyleClass().add("item-label");
        
        item.getChildren().addAll(iconContainer, label);
        return item;
    }

    public void setOnFinishedCallback(Consumer<String> callback) {
        this.onFinishedCallback = callback;
    }

    public void setGameContext(Partida partida, Jugador jugador) {
        this.partida = partida;
        this.jugador = jugador;
        
        if (jugador != null && jugador.isEsIA()) {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> handleGirar());
            pause.play();
        }
    }

    @FXML
    private void handleGirar() {
        if (isSpinning) return;
        controlador.SoundManager.getInstance().playSound("click");
        controlador.SoundManager.getInstance().playSound("event");
        isSpinning = true;
        spinButton.setDisable(true);
        resultLabel.setText("Girant...!");

        // Determine result based on probabilities
        int r = random.nextInt(100);
        int resultIndex = 0;
        int sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (r < sum) {
                resultIndex = i;
                break;
            }
        }
        String resultEvent = possibleEvents[resultIndex];

        // Replace the item at WINNING_INDEX with the actual result
        sliderHBox.getChildren().set(WINNING_INDEX, createItemNode(resultEvent));

        // Calculate final translation
        double pointerPos = 350.0;
        double internalOffset = (random.nextDouble() * 80) - 40; // -40 to 40 per no quedar sempre exactament al mig
        double targetX = pointerPos - (WINNING_INDEX * ITEM_WIDTH + ITEM_WIDTH / 2.0) + internalOffset;

        TranslateTransition tt = new TranslateTransition(Duration.seconds(6), sliderHBox);
        tt.setFromX(0);
        tt.setToX(targetX);
        tt.setInterpolator(Interpolator.SPLINE(0.1, 0, 0.1, 1)); // Custom slowdown (Starts fast, slows down very smoothly)
        tt.setOnFinished(e -> {
            isSpinning = false;
            controlador.SoundManager.getInstance().stopSound("event");
            controlador.SoundManager.getInstance().playSound("click"); // So final
            
            // Efecte de glow per a l'ítem guanyador
            javafx.scene.Node winningNode = sliderHBox.getChildren().get(WINNING_INDEX);
            winningNode.setStyle("-fx-effect: dropshadow(gaussian, #00d2ff, 40, 0.5, 0, 0); -fx-background-color: rgba(0, 210, 255, 0.2);");
            
            resultLabel.setText("¡T'ha tocat: " + resultEvent + "!");
            applyResult(resultEvent);
            closeButton.setVisible(true);
            if (onFinishedCallback != null) {
                onFinishedCallback.accept(resultEvent);
            }
        });
        tt.play();
    }

    private void applyResult(String result) {
        if (jugador == null) return;
        
        String logMsg = "";
        switch (result) {
            case "Boles de neu" -> {
                int qty = 1 + random.nextInt(2);
                jugador.getInv().anadirItem(new BolaDeNieve(qty));
                logMsg = jugador.getNombre() + " ha obtingut " + qty + " boles de neu!";
            }
            case "Peix" -> {
                jugador.getInv().anadirItem(new Pez(1));
                logMsg = jugador.getNombre() + " ha obtingut un peix!";
            }
            case "Dau lent" -> {
                jugador.getInv().anadirItem(new Dado("Dau Lent", 1, 1, 3));
                logMsg = jugador.getNombre() + " ha obtingut un Dau Lent!";
            }
            case "Dau ràpid" -> {
                jugador.getInv().anadirItem(new Dado("Dau Ràpid", 1, 5, 6));
                logMsg = jugador.getNombre() + " ha obtingut un Dau Ràpid!";
            }
            case "Perdre un torn" -> {
                if (partida != null) {
                    partida.setJugadorPierdeTurno(jugador);
                    logMsg = jugador.getNombre() + " ha perdut el següent torn!";
                }
            }
            case "Moto de neu" -> {
                int pos = jugador.getPosicion();
                int nextSled = -1;
                if (partida != null && partida.getTablero() != null) {
                    java.util.List<Casilla> casillas = partida.getTablero().getCasillas();
                    for (int i = pos + 1; i < casillas.size(); i++) {
                        if (casillas.get(i) instanceof Trineo) {
                            nextSled = i;
                            break;
                        }
                    }
                }
                
                if (nextSled != -1) {
                    jugador.setPosicion(nextSled);
                    logMsg = jugador.getNombre() + " ha agafat una moto de neu fins al següent trineu (casella " + nextSled + ")!";
                } else {
                    // Fallback si no hi ha més trineus: 10 caselles cap endavant o fins al final
                    int max = partida.getTablero().getTotalCasillas() - 1;
                    int novaPos = Math.min(pos + 10, max);
                    jugador.setPosicion(novaPos);
                    logMsg = jugador.getNombre() + " ha agafat una moto de neu i avança 10 caselles!";
                }
            }
        }
        
        if (partida != null && !logMsg.isEmpty()) {
            partida.anadirEvento(logMsg);
        }
    }

    @FXML
    private void handleClose() {
        controlador.SoundManager.getInstance().playSound("click");
        if (root.getParent() instanceof StackPane mainStack) {
            mainStack.getChildren().remove(root);
        }
    }
}
