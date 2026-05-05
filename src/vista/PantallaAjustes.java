package vista;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.VBox;

public class PantallaAjustes {

    @FXML private VBox rootContainer;

    @FXML private Slider sldMusica;
    @FXML private Slider sldEfectes;
    @FXML private Label lblMusica;
    @FXML private Label lblEfectes;
    @FXML private ChoiceBox<String> cbQualitat;
    @FXML private Button btnMuteMusica;
    @FXML private Button btnMuteEfectes;

    private double prevMusicaVol = 80;
    private double prevEfectesVol = 100;

    @FXML
    public void initialize() {
        // Inicialitzar valors labels
        sldMusica.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMusica.setText(newVal.intValue() + "%");
            controlador.SoundManager.getInstance().setMusicVolume(newVal.doubleValue() / 100.0);
            if (newVal.intValue() > 0) {
                btnMuteMusica.setText("🔊");
                controlador.SoundManager.getInstance().setMusicEnabled(true);
            } else {
                btnMuteMusica.setText("🔇");
                controlador.SoundManager.getInstance().setMusicEnabled(false);
            }
        });

        sldEfectes.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblEfectes.setText(newVal.intValue() + "%");
            controlador.SoundManager.getInstance().setSoundVolume(newVal.doubleValue() / 100.0);
            if (newVal.intValue() > 0) {
                btnMuteEfectes.setText("🔊");
                controlador.SoundManager.getInstance().setSoundEnabled(true);
            } else {
                btnMuteEfectes.setText("🔇");
                controlador.SoundManager.getInstance().setSoundEnabled(false);
            }
        });

        // Inicialitzar ChoiceBox amb només 2 opcions
        cbQualitat.setItems(FXCollections.observableArrayList("Baja", "Alta"));
        cbQualitat.setValue("Alta");

        // Animació d'entrada
        rootContainer.setOpacity(0);
        rootContainer.setScaleX(0.7);
        rootContainer.setScaleY(0.7);

        FadeTransition fade = new FadeTransition(Duration.millis(300), rootContainer);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), rootContainer);
        scale.setFromX(0.7);
        scale.setFromY(0.7);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    @FXML
    private void handleMuteMusica() {
        if (sldMusica.getValue() > 0) {
            prevMusicaVol = sldMusica.getValue();
            sldMusica.setValue(0);
        } else {
            sldMusica.setValue(prevMusicaVol > 0 ? prevMusicaVol : 80);
        }
    }

    @FXML
    private void handleMuteEfectes() {
        if (sldEfectes.getValue() > 0) {
            prevEfectesVol = sldEfectes.getValue();
            sldEfectes.setValue(0);
        } else {
            sldEfectes.setValue(prevEfectesVol > 0 ? prevEfectesVol : 100);
        }
    }

    @FXML
    private void handleGuardar() {
        controlador.SoundManager.getInstance().playSound("click");
        String qualitat = cbQualitat.getValue();
        // Aplicar lògica de qualitat si cal
        if (qualitat.equals("Baja")) {
            System.out.println("DEBUG: Aplicant gràfics baixos...");
            // Aquí es podria desactivar efectes en el controlador principal
        }
        tancarFinestra();
    }

    @FXML
    private void handleCancelar() {
        controlador.SoundManager.getInstance().playSound("click");
        tancarFinestra();
    }

    private void tancarFinestra() {
        FadeTransition fade = new FadeTransition(Duration.millis(200), rootContainer);
        fade.setFromValue(1);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), rootContainer);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.8);
        scale.setToY(0.8);

        fade.setOnFinished(e -> {
            Stage stage = (Stage) rootContainer.getScene().getWindow();
            stage.close();
        });

        fade.play();
        scale.play();
    }
}
