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
    @FXML private Button btnMuteMusica;
    @FXML private Button btnMuteEfectes;
    @FXML private Button btnGuia;

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

        if (btnGuia != null) {
            btnGuia.setOnAction(e -> handleAyuda());
        }

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
    public void handleAyuda() {
        controlador.SoundManager.getInstance().playSound("click");
        try {
            java.net.URL fxmlUrl = getClass().getResource("/resources/PantallaAyuda.fxml");
            if (fxmlUrl == null) {
                mostrarError("No s'ha trobat el fitxer: /resources/PantallaAyuda.fxml");
                return;
            }
            
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
            
            // Forzamos que esté encima
            stage.setAlwaysOnTop(true);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error carregant la guia: " + e.toString());
        }
    }

    private void mostrarError(String msg) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error de Guia");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

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
