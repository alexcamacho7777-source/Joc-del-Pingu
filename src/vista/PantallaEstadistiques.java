package vista;

import controlador.GestorBBDD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.MapValueFactory;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PantallaEstadistiques {

    @FXML private Label lblMitjaGlobal;
    @FXML private Label lblVictoriesPropies;
    @FXML private TableView<Map> tblRankingPartides;
    @FXML private TableColumn<Map, String> colNomPartides;
    @FXML private TableColumn<Map, String> colTotalPartides;

    @FXML private TableView<Map> tblRankingRecord;
    @FXML private TableColumn<Map, String> colNomRecord;
    @FXML private TableColumn<Map, String> colMaxPeixos;

    @FXML private TextField txtPuntuacio;
    @FXML private Label lblResultatPercentatge;

    private GestorBBDD db;

    @FXML
    public void initialize() {
        db = new GestorBBDD();
        
        // 1. Dades globals
        double mitja = db.getMitjaGlobalSQL();
        lblMitjaGlobal.setText(String.format("%.2f", mitja));

        // 2. Dades pròpies (si loguejat)
        String user = PantallaMenu.getLoggedInUser();
        if (user != null) {
            int id = db.getIDJugador(user);
            int vics = db.getVictoriesSQL(id);
            lblVictoriesPropies.setText(String.valueOf(vics));
        } else {
            lblVictoriesPropies.setText("-");
        }

        // 3. Configurar Taules
        colNomPartides.setCellValueFactory(new MapValueFactory<>("NOM_JUGADOR"));
        colTotalPartides.setCellValueFactory(new MapValueFactory<>("TOTAL"));

        colNomRecord.setCellValueFactory(new MapValueFactory<>("NOM_JUGADOR"));
        colMaxPeixos.setCellValueFactory(new MapValueFactory<>("RECORD"));

        cargarRankings();
    }

    private void cargarRankings() {
        ArrayList<LinkedHashMap<String, String>> resPartides = db.getRankingPartidesSQL();
        ObservableList<Map> itemsPartides = FXCollections.observableArrayList(resPartides);
        tblRankingPartides.setItems(itemsPartides);

        ArrayList<LinkedHashMap<String, String>> resRecord = db.getRankingRecordSQL();
        ObservableList<Map> itemsRecord = FXCollections.observableArrayList(resRecord);
        tblRankingRecord.setItems(itemsRecord);
    }

    @FXML
    private void handleCalcularPercentatge(ActionEvent event) {
        try {
            int p = Integer.parseInt(txtPuntuacio.getText());
            double perc = db.getPercentatgeMenysSQL(p);
            lblResultatPercentatge.setText(String.format("Has superat al %.1f%% dels jugadors!", perc));
        } catch (NumberFormatException e) {
            lblResultatPercentatge.setText("Introdueix un número vàlid.");
        }
    }

    @FXML
    private void handleTornar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
