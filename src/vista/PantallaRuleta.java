package vista;

import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.Group;
import javafx.util.Duration;
import java.util.Random;
import java.util.function.Consumer;
import model.*;

public class PantallaRuleta {

    @FXML private StackPane root;
    @FXML private Group wheelGroup;
    @FXML private Label resultLabel;
    @FXML private Button spinButton;
    @FXML private Button closeButton;

    private final Random random = new Random();
    private Consumer<String> onFinishedCallback;
    private boolean isSpinning = false;
    private Partida partida;
    private Jugador jugador;

    // Resultados mapeados a ángulos (Norte/Oeste/Sur/Este)
    // 0: Boles de neu (Norte)
    // 1: Peix (Oeste)
    // 2: Dau lent (Sur)
    // 3: Dau ràpid (Este)
    private final String[] results = {
        "Boles de neu", // 0 grados (Norte)
        "Peix",         // 270 grados (Oeste)
        "Dau lent",     // 180 grados (Sur)
        "Dau ràpid"     // 90 grados (Este)
    };

    @FXML
    public void initialize() {
        System.out.println("PantallaRuleta initialized");
    }

    public void setOnFinishedCallback(Consumer<String> callback) {
        this.onFinishedCallback = callback;
    }

    public void setGameContext(Partida partida, Jugador jugador) {
        this.partida = partida;
        this.jugador = jugador;
        
        // Si és IA, fem que giti automàticament després de 1 segon
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

        // Reset rotation to avoid cumulative errors
        wheelGroup.setRotate(wheelGroup.getRotate() % 360);

        // Elegir resultado (0: Boles, 1: Peix, 2: Dau lent, 3: Dau ràpid)
        int r = random.nextInt(100);
        int resultIndex;
        if (r < 40) resultIndex = 2;      // 40% Dau lent
        else if (r < 70) resultIndex = 0; // 30% Boles de neu
        else if (r < 85) resultIndex = 1; // 15% Peix
        else resultIndex = 3;            // 15% Dau ràpid

        // Cálculo de ángulo preciso:
        // El puntero está arriba (0°).
        // Si queremos que el puntero apunte a la sección en el ángulo S, 
        // la ruleta debe rotar R = (360 - S).
        double targetSectionAngle = 0;
        switch(resultIndex) {
            case 0: targetSectionAngle = 0;   break; // Boles
            case 1: targetSectionAngle = 270; break; // Peix (Oeste)
            case 2: targetSectionAngle = 180; break; // Dau lent (Sur)
            case 3: targetSectionAngle = 90;  break; // Dau ràpid (Este)
        }

        double targetRotation = 360 - targetSectionAngle;
        // Añadimos varias vueltas completas para el efecto visual
        double totalRotation = (360 * 10) + targetRotation; 

        RotateTransition rt = new RotateTransition(Duration.seconds(4), wheelGroup);
        rt.setFromAngle(wheelGroup.getRotate());
        rt.setToAngle(wheelGroup.getRotate() + totalRotation);
        rt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        rt.setCycleCount(1);
        rt.setOnFinished(e -> {
            isSpinning = false;
            controlador.SoundManager.getInstance().stopSound("event");
            String result = results[resultIndex];
            resultLabel.setText("¡Te ha tocado: " + result + "!");
            
            if (jugador != null) {
                switch (result) {
                    case "Boles de neu" -> jugador.getInv().anadirItem(new BolaDeNieve(1 + random.nextInt(2)));
                    case "Peix" -> jugador.getInv().anadirItem(new Pez(1));
                    case "Dau lent" -> jugador.getInv().anadirItem(new Dado("Dau Lent", 1, 1, 3));
                    case "Dau ràpid" -> jugador.getInv().anadirItem(new Dado("Dau Ràpid", 1, 5, 6));
                }
                if (partida != null) {
                    partida.anadirEvento(jugador.getNombre() + " ha obtingut: " + result);
                }
            }
            
            closeButton.setVisible(true);
            if (onFinishedCallback != null) {
                onFinishedCallback.accept(result);
            }
        });
        rt.play();
    }

    @FXML
    private void handleClose() {
        controlador.SoundManager.getInstance().playSound("click");
        if (root.getParent() instanceof StackPane mainStack) {
            mainStack.getChildren().remove(root);
        }
    }
}
