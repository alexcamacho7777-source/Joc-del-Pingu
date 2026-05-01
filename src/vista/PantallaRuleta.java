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
        "Boles de neu", 
        "Peix", 
        "Dau lent", 
        "Dau ràpid",
        "Perdre torn",
        "Perdre objecte",
        "Motos de neu"
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
        isSpinning = true;
        spinButton.setDisable(true);
        resultLabel.setText("Girant...!");

        // Probabilitats: Dau lent (alta), Boles de neu, Peix, Dau ràpid (baixa)
        // Probabilitats expandides
        int r = random.nextInt(100);
        int tempIndex;
        if (r < 30) tempIndex = 2;      // 30% Dau lent
        else if (r < 50) tempIndex = 0; // 20% Boles de neu
        else if (r < 65) tempIndex = 1; // 15% Peix
        else if (r < 75) tempIndex = 3; // 10% Dau ràpid
        else if (r < 85) tempIndex = 6; // 10% Motos de neu 
        else if (r < 92) tempIndex = 4; // 7% Perdre torn
        else tempIndex = 5;            // 8% Perdre objecte

        final int resultIndex = tempIndex;
        // Ángulos (ahora hay 7 divisiones, pero para simplificar visualmente usaremos 8 o 4)
        // Como el disco visual original probablemente tiene 4, lo mantendremos girando a uno de los 4 cuadrantes
        // y el texto dirá el resultado real. Para hacerlo "pro", deberíamos tener 7-8 divisiones.
        // Pero el usuario pidió "nivel no tan alto", así que la aleatoriedad basta.
        int visualIndex = resultIndex % 4;
        double baseAngle = (visualIndex * 90);
        double randomOffset = (random.nextDouble() * 60) - 30;
        double targetAngle = baseAngle + randomOffset;
        
        double totalRotation = 360 * 8 + targetAngle; 

        RotateTransition rt = new RotateTransition(Duration.seconds(4), wheelGroup);
        rt.setByAngle(totalRotation);
        rt.setCycleCount(1);
        rt.setOnFinished(e -> {
            isSpinning = false;
            String result = results[resultIndex];
            resultLabel.setText("¡Te ha tocado: " + result + "!");
            
            // Aplicar el resultat al jugador
            if (jugador != null) {
                switch (result) {
                    case "Boles de neu" -> jugador.getInv().anadirItem(new BolaDeNieve(1 + random.nextInt(3)));
                    case "Peix" -> jugador.getInv().anadirItem(new Pez(1));
                    case "Dau lent" -> jugador.getInv().anadirItem(new Dado("Dau Lent", 1, 1, 3));
                    case "Dau ràpid" -> jugador.getInv().anadirItem(new Dado("Dau Ràpid", 1, 5, 10));
                    case "Perdre torn" -> {
                        if (partida != null) partida.setJugadorPierdeTurno(jugador);
                    }
                    case "Perdre objecte" -> jugador.getInv().quitarItemAleatorio(random);
                    case "Motos de neu" -> {
                        if (partida != null) {
                            int nextSled = partida.getTablero().buscarSiguienteTrineo(jugador.getPosicion());
                            if (nextSled != -1) jugador.setPosicion(nextSled);
                        }
                    }
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
        if (root.getParent() instanceof StackPane mainStack) {
            mainStack.getChildren().remove(root);
        }
    }
}
