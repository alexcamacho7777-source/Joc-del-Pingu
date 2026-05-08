package controlador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

/**
 * PUNT D'ENTRADA PRINCIPAL DE L'APLICACIÓ.
 * INICIALITZA L'ENTORN JAVAFX I CARREGA LA PANTALLA D'INTRODUCCIÓ.
 */
public class main extends Application {

    /**
     * MÈTODE D'INICIALITZACIÓ DE LA INTERFÍCIE GRÀFICA.
     * @param primaryStage FINESTRA PRINCIPAL DE L'APLICACIÓ.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // INICI DE LA MÚSICA DE FONS DELS MENÚS
        controlador.SoundManager.getInstance().playMenuMusic();

        // CÀRREGA DE LA PRIMERA PANTALLA (INTRODUCCIÓ)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaIntro.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("EL JOC DEL PINGÜÍ");
        
        // CONFIGURACIÓ DE PANTALLA COMPLETA PER A UNA EXPERIÈNCIA IMMERSIVA
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.show();
    }

    /**
     * MÈTODE ESTÀTIC PRINCIPAL PER LLANÇAR L'APLICACIÓ.
     */
    public static void main(String[] args) {
        launch(args);
    }
}