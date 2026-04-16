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

public class PantallaRuleta {

    @FXML private StackPane root;
    @FXML private Group wheelGroup;
    @FXML private Label resultLabel;
    @FXML private Button spinButton;
    @FXML private Button closeButton;

    private final Random random = new Random();
    private Consumer<String> onFinishedCallback;
    private boolean isSpinning = false;

    // Resultados mapeados a ángulos (Norte/Oeste/Sur/Este)
    // 0: Boles de neu (Norte)
    // 1: Peix (Oeste)
    // 2: Dau lent (Sur)
    // 3: Dau ràpid (Este)
    private final String[] results = {
        "Boles de neu", 
        "Peix", 
        "Dau lent", 
        "Dau ràpid"
    };

    @FXML
    public void initialize() {
        System.out.println("PantallaRuleta initialized");
    }

    public void setOnFinishedCallback(Consumer<String> callback) {
        this.onFinishedCallback = callback;
    }

    @FXML
    private void handleGirar() {
        if (isSpinning) return;
        isSpinning = true;
        spinButton.setDisable(true);
        resultLabel.setText("Girant...!");

        // Probabilitats: Dau lent (alta), Boles de neu, Peix, Dau ràpid (baixa)
        int r = random.nextInt(10);
        int resultIndex;
        if (r < 4) resultIndex = 2;      // 40% Dau lent
        else if (r < 7) resultIndex = 0; // 30% Boles de neu
        else if (r < 9) resultIndex = 1; // 20% Peix
        else resultIndex = 3;            // 10% Dau ràpid

        // Ángulos rectos (0, 90, 180, 270)
        double baseAngle = (resultIndex * 90);
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
