package vista;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.layout.VBox;

/**
 * CONTROLADOR PER A LA CONFIGURACIÓ D'AUDIO I AJUSTAMENTS DEL JOC.
 * PERMET MODIFICAR EL VOLUM DE LA MÚSICA I DELS EFECTES DE SO.
 */
public class PantallaAjustes {

    @FXML private VBox rootContainer;

    @FXML private Slider sldMusica;
    @FXML private Slider sldEfectes;
    @FXML private Label lblMusica;
    @FXML private Label lblEfectes;
    @FXML private Button btnMuteMusica;
    @FXML private Button btnMuteEfectes;
    @FXML private Button btnGuia;

    // VARIABLES PER RECURERAR EL VOLUM DESPRÉS D'UN MUTE
    private double prevMusicaVol = 80;
    private double prevEfectesVol = 100;

    /**
     * INICIALITZA ELS SLIDERS I CONFIGURA ELS ESDEVENIMENTS DE CANVI DE VOLUM.
     */
    @FXML
    public void initialize() {
        // CARREGAR VALORS ACTUALS DEL SOUNDMANAGER
        double currentMusic = controlador.SoundManager.getInstance().getMusicVolume() * 100.0;
        double currentSound = controlador.SoundManager.getInstance().getSoundVolume() * 100.0;
        
        sldMusica.setValue(currentMusic);
        sldEfectes.setValue(currentSound);
        lblMusica.setText((int)currentMusic + "%");
        lblEfectes.setText((int)currentSound + "%");
        actualizarIconoMuteMusica((int)currentMusic);
        actualizarIconoMuteEfectes((int)currentSound);

        // CONFIGURACIÓ DEL CONTROL DE MÚSICA
        sldMusica.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMusica.setText(newVal.intValue() + "%");
            controlador.SoundManager.getInstance().setMusicVolume(newVal.doubleValue() / 100.0);
            actualizarIconoMuteMusica(newVal.intValue());
        });

        // CONFIGURACIÓ DEL CONTROL D'EFECTES DE SO
        sldEfectes.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblEfectes.setText(newVal.intValue() + "%");
            controlador.SoundManager.getInstance().setSoundVolume(newVal.doubleValue() / 100.0);
            actualizarIconoMuteEfectes(newVal.intValue());
        });

        if (btnGuia != null) {
            btnGuia.setOnAction(e -> handleAyuda());
        }

        aplicarAnimacionEntrada();
    }

    private void actualizarIconoMuteMusica(int vol) {
        if (vol > 0) {
            btnMuteMusica.setText("🔊");
            controlador.SoundManager.getInstance().setMusicEnabled(true);
        } else {
            btnMuteMusica.setText("🔇");
            controlador.SoundManager.getInstance().setMusicEnabled(false);
        }
    }

    private void actualizarIconoMuteEfectes(int vol) {
        if (vol > 0) {
            btnMuteEfectes.setText("🔊");
            controlador.SoundManager.getInstance().setSoundEnabled(true);
        } else {
            btnMuteEfectes.setText("🔇");
            controlador.SoundManager.getInstance().setSoundEnabled(false);
        }
    }

    /**
     * APLICA UNA ANIMACIÓ DE CREIXEMENT I APARICIÓ GRADUAL AL PANNELL.
     */
    private void aplicarAnimacionEntrada() {
        rootContainer.setOpacity(0);
        rootContainer.setScaleX(0.7);
        rootContainer.setScaleY(0.7);

        FadeTransition fade = new FadeTransition(Duration.millis(300), rootContainer);
        fade.setFromValue(0); fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), rootContainer);
        scale.setFromX(0.7); scale.setFromY(0.7);
        scale.setToX(1.0); scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    /**
     * COMMUTA ENTRE EL VOLUM ACTUAL I EL SILENCI PER A LA MÚSICA.
     */
    @FXML
    private void handleMuteMusica() {
        if (sldMusica.getValue() > 0) {
            prevMusicaVol = sldMusica.getValue();
            sldMusica.setValue(0);
        } else {
            sldMusica.setValue(prevMusicaVol > 0 ? prevMusicaVol : 80);
        }
    }

    /**
     * COMMUTA ENTRE EL VOLUM ACTUAL I EL SILENCI PER ALS EFECTES.
     */
    @FXML
    private void handleMuteEfectes() {
        if (sldEfectes.getValue() > 0) {
            prevEfectesVol = sldEfectes.getValue();
            sldEfectes.setValue(0);
        } else {
            sldEfectes.setValue(prevEfectesVol > 0 ? prevEfectesVol : 100);
        }
    }

    /**
     * OBRE LA GUIA D'AJUDA DEL JOC EN UNA FINESTRA MODAL SOBREPOSADA.
     */
    @FXML
    public void handleAyuda() {
        controlador.SoundManager.getInstance().playSound("click");
        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaAyuda.fxml");
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlUrl);
            javafx.scene.Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (rootContainer.getScene() != null) {
                stage.initOwner(rootContainer.getScene().getWindow());
            }
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.setAlwaysOnTop(true);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TANCA LA FINESTRA D'AJUSTAMENTS AMB UNA ANIMACIÓ DE SORTIDA.
     */
    @FXML
    private void handleGuardar() {
        controlador.SoundManager.getInstance().playSound("click");
        tancarFinestra();
    }

    @FXML
    private void handleCancelar() {
        controlador.SoundManager.getInstance().playSound("click");
        tancarFinestra();
    }

    /**
     * TANCAR EL JOC COMPLETAMENT.
     */
    @FXML
    private void handleSortir() {
        controlador.SoundManager.getInstance().playSound("click");
        javafx.application.Platform.exit();
        System.exit(0);
    }

    private void tancarFinestra() {
        FadeTransition fade = new FadeTransition(Duration.millis(200), rootContainer);
        fade.setFromValue(1); fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(200), rootContainer);
        scale.setFromX(1.0); scale.setFromY(1.0);
        scale.setToX(0.8); scale.setToY(0.8);

        fade.setOnFinished(e -> {
            Stage stage = (Stage) rootContainer.getScene().getWindow();
            stage.close();
        });

        fade.play();
        scale.play();
    }
}
