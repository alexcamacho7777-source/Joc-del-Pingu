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
import javafx.util.Duration;
import java.util.Random;
import java.util.function.Consumer;
import model.*;

/**
 * CONTROLADOR PER A LA RULETA D'ESDEVENIMENTS ALEATORIS.
 * GESTIONA L'ANIMACIÓ VISUAL I L'ASSIGNACIÓ DE PREMIS O CÀSTIGS ALS JUGADORS.
 */
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

    // CONSTANTS PER A LA CONFIGURACIÓ DE L'ANIMACIÓ
    private static final double ITEM_WIDTH = 140.0;
    private static final int STRIP_SIZE = 60; 
    private static final int WINNING_INDEX = 50; 

    // LLISTA D'ESDEVENIMENTS POSSIBLES
    private final String[] possibleEvents = {
        "DAU LENT",
        "BOLES DE NEU",
        "PEIX",
        "DAU RÀPID",
        "PERDRE UN TORN",
        "MOTO DE NEU",
        "PERDRE OBJECTE"
    };

    // PROBABILITATS ASSOCIADES (SUMA TOTAL = 100)
    private final int[] probabilities = {25, 20, 15, 15, 10, 10, 5};

    /**
     * CONFIGURA LA RULETA AMB ELS ÍTEMS INICIALS ALEATORIS.
     */
    @FXML
    public void initialize() {
        setupInitialSlider();
    }

    /**
     * RELLENA LA TIRA DE LA RULETA AMB ELEMENTS VISUALS.
     */
    private void setupInitialSlider() {
        sliderHBox.getChildren().clear();
        for (int i = 0; i < STRIP_SIZE; i++) {
            String event = possibleEvents[random.nextInt(possibleEvents.length)];
            sliderHBox.getChildren().add(createItemNode(event));
        }
        sliderHBox.setTranslateX(0);
    }

    /**
     * DETERMINA EL FITXER D'IMATGE CORRESPONENT A L'ESDEVENIMENT.
     */
    private String getFileNameForEvent(String eventName) {
        String res = null;
        switch (eventName.toUpperCase()) {
            case "DAU LENT": res = "event_dau_lent.png"; break;
            case "BOLES DE NEU": res = "event_boles.png"; break;
            case "PEIX": res = "event_peix.png"; break;
            case "DAU RÀPID": res = "event_dau_rapid.png"; break;
            case "PERDRE UN TORN": res = "event_perdre_torn.png"; break;
            case "MOTO DE NEU": res = "event_moto.png"; break;
            case "PERDRE OBJECTE": res = "event_perdre_objecte.png"; break;
        }
        return res;
    }

    /**
     * CREA EL NODE VISUAL (ICONO + TEXT) PER A UN ELEMENT DE LA RULETA.
     */
    private VBox createItemNode(String eventName) {
        VBox item = new VBox(5);
        item.getStyleClass().add("slider-item");
        item.setPrefWidth(ITEM_WIDTH);
        item.setMinWidth(ITEM_WIDTH);
        item.setMaxWidth(ITEM_WIDTH);
        
        StackPane iconContainer = new StackPane();
        iconContainer.getStyleClass().add("item-icon-container");
        iconContainer.setPrefSize(120, 120);
        
        try {
            String fileName = getFileNameForEvent(eventName);
            if (fileName != null) {
                java.net.URL url = getClass().getResource("/resources/" + fileName);
                if (url != null) {
                    Image img = new Image(url.toExternalForm());
                    ImageView iv = new ImageView(img);
                    
                    // APLICACIÓ DE RECORTE (CROP) PER EVITAR DISTORSIÓ
                    double side = Math.min(img.getWidth(), img.getHeight());
                    double x = (img.getWidth() - side) / 2;
                    double y = (img.getHeight() - side) / 2;
                    iv.setViewport(new javafx.geometry.Rectangle2D(x, y, side, side));
                    
                    iv.setFitWidth(120);
                    iv.setFitHeight(120);
                    iv.setPreserveRatio(true);
                    iconContainer.getChildren().add(iv);
                }
            }
        } catch (Exception e) {
            Label fallback = new Label("?");
            iconContainer.getChildren().add(fallback);
        }
        
        Label label = new Label(eventName.toUpperCase());
        label.getStyleClass().add("item-label");
        item.getChildren().addAll(iconContainer, label);
        return item;
    }

    /**
     * ASSIGNA EL CONTEXT DE LA PARTIDA I AUTOMATITZA EL GIR SI EL JUGADOR ÉS IA.
     */
    public void setGameContext(Partida partida, Jugador jugador) {
        this.partida = partida;
        this.jugador = jugador;
        
        if (jugador != null && jugador.isEsIA()) {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> handleGirar());
            pause.play();
        }
    }

    /**
     * INICIA L'ANIMACIÓ DE GIR I CALCULA EL RESULTAT SEGONS PROBABILITATS.
     */
    @FXML
    private void handleGirar() {
        if (!isSpinning) {
            controlador.SoundManager.getInstance().playSound("event");
            isSpinning = true;
            spinButton.setDisable(true);
            resultLabel.setText("GIRANT...!");

            // CÀLCUL DEL RESULTAT BASAT EN PESOS
            int r = random.nextInt(100);
            int sum = 0;
            int resultIndex = 0;
            boolean foundR = false;
            for (int i = 0; i < probabilities.length && !foundR; i++) {
                sum += probabilities[i];
                if (r < sum) {
                    resultIndex = i;
                    foundR = true;
                }
            }
            String resultEvent = possibleEvents[resultIndex];

            // INSERCIÓ DE L'ÍTEM GUANYADOR A LA POSICIÓ DE DESTÍ
            sliderHBox.getChildren().set(WINNING_INDEX, createItemNode(resultEvent));

            // CONFIGURACIÓ DE LA TRANSICIÓ DE MOVIMENT
            double pointerPos = 350.0; 
            double targetX = pointerPos - (WINNING_INDEX * ITEM_WIDTH + ITEM_WIDTH / 2.0);

            TranslateTransition tt = new TranslateTransition(Duration.seconds(6), sliderHBox);
            tt.setFromX(0);
            tt.setToX(targetX);
            tt.setInterpolator(Interpolator.SPLINE(0.1, 0, 0.1, 1));
            tt.setOnFinished(e -> {
                isSpinning = false;
                controlador.SoundManager.getInstance().stopSound("event");
                controlador.SoundManager.getInstance().playSound("click");
                
                resultLabel.setText("¡T'HA TOCAT: " + resultEvent + "!");
                applyResult(resultEvent);
                
                if (jugador != null && jugador.isEsIA()) {
                    // SI ÉS IA, TANQUEM AUTOMÀTICAMENT DESPRÉS DE 2 SEGONS
                    javafx.animation.PauseTransition autoClose = new javafx.animation.PauseTransition(Duration.seconds(2));
                    autoClose.setOnFinished(ev -> handleClose());
                    autoClose.play();
                } else {
                    closeButton.setVisible(true);
                    closeButton.setManaged(true);
                }

                if (onFinishedCallback != null) {
                    onFinishedCallback.accept(resultEvent);
                }
            });
            tt.play();
        }
    }

    /**
     * APLICA L'EFECTE DE L'ESDEVENIMENT AL JUGADOR (ÍTEMS, MOVIMENT O TORN).
     */
    private void applyResult(String result) {
        if (jugador != null) {
            String logMsg = "";
            switch (result.toUpperCase()) {
                case "BOLES DE NEU":
                    int qty = 1 + random.nextInt(2);
                    jugador.getInv().anadirItem(new BolaDeNieve(qty));
                    logMsg = jugador.getNombre().toUpperCase() + " OBTÉ " + qty + " BOLES.";
                    break;
                case "PEIX":
                    jugador.getInv().anadirItem(new Pez(1));
                    logMsg = jugador.getNombre().toUpperCase() + " OBTÉ UN PEIX.";
                    break;
                case "DAU LENT":
                    jugador.getInv().anadirItem(new Dado("DAU LENT", 1, 1, 3));
                    logMsg = jugador.getNombre().toUpperCase() + " OBTÉ UN DAU LENT.";
                    break;
                case "DAU RÀPID":
                    jugador.getInv().anadirItem(new Dado("DAU RÀPID", 1, 5, 6));
                    logMsg = jugador.getNombre().toUpperCase() + " OBTÉ UN DAU RÀPID.";
                    break;
                case "PERDRE UN TORN":
                    if (partida != null) {
                        partida.setJugadorPierdeTurno(jugador);
                        logMsg = jugador.getNombre().toUpperCase() + " PERD UN TORN.";
                    }
                    break;
                case "MOTO DE NEU":
                    int nextSled = -1;
                    if (partida != null && partida.getTablero() != null) {
                        java.util.List<Casilla> casillas = partida.getTablero().getCasillas();
                        for (int i = jugador.getPosicion() + 1; i < casillas.size() && nextSled == -1; i++) {
                            if (casillas.get(i) instanceof Trineo) nextSled = i;
                        }
                    }
                    if (nextSled != -1) {
                        jugador.setPosicion(nextSled);
                        logMsg = jugador.getNombre().toUpperCase() + " PUJA A UNA MOTO FINS AL TRINEU.";
                    } else {
                        jugador.setPosicion(Math.min(jugador.getPosicion() + 10, 49));
                        logMsg = jugador.getNombre().toUpperCase() + " AVANÇA 10 CASELLES AMB MOTO.";
                    }
                    break;
                case "PERDRE OBJECTE":
                    Item eliminado = jugador.getInv().quitarUnidadAleatoria(random);
                    if (eliminado != null) logMsg = jugador.getNombre().toUpperCase() + " PERD UN/A " + eliminado.getNombre().toUpperCase() + ".";
                    else logMsg = jugador.getNombre().toUpperCase() + " NO TENIA OBJECTES PER PERDRE.";
                    break;
            }
            if (partida != null && !logMsg.isEmpty()) partida.anadirEvento(logMsg);
        }
    }

    /**
     * TANCAMENT DE LA RULETA I RETORN AL TAULELL.
     */
    @FXML
    private void handleClose() {
        controlador.SoundManager.getInstance().playSound("click");
        if (root.getParent() instanceof StackPane mainStack) {
            mainStack.getChildren().remove(root);
        }
    }

    public void setOnFinishedCallback(Consumer<String> callback) {
        this.onFinishedCallback = callback;
    }
}
