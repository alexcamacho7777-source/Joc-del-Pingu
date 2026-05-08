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

/**
 * CONTROLADOR DE LA PANTALLA D'ESTADÍSTIQUES I RÀNQUINGS.
 * MOSTRA DADES AGREGADES DE LA BBDD SOBRE EL RENDIMENT DELS JUGADORS.
 */
public class PantallaEstadistiques {

    @FXML private Label lblMitjaGlobal;
    @FXML private Label lblRecordGlobal;
    @FXML private Label lblVictoriesPropies;
    
    // TAULES PER MOSTRAR ELS DIFERENTS RÀNQUINGS
    @FXML private TableView<Map> tblRankingPartides;
    @FXML private TableColumn<Map, String> colNomPartides;
    @FXML private TableColumn<Map, String> colTotalPartides;

    @FXML private TableView<Map> tblRankingRecord;
    @FXML private TableColumn<Map, String> colNomRecord;
    @FXML private TableColumn<Map, String> colMaxVics;

    @FXML private TableView<Map> tblRankingSobreMitja;
    @FXML private TableColumn<Map, String> colNomSobreMitja;
    @FXML private TableColumn<Map, String> colVicsSobreMitja;

    @FXML private TextField txtPuntuacio;
    @FXML private Label lblResultatPercentatge;

    private GestorBBDD db;

    /**
     * INICIALITZA LES DADES DE LA PANTALLA, CARREGA LES MITJANES I ELS RÀNQUINGS.
     */
    @FXML
    public void initialize() {
        db = new GestorBBDD();
        
        // 1. OBTENCIÓ DE DADES GLOBALS (MITJANA I RÈCORD)
        double mitja = db.getMitjaVictoriesSQL();
        lblMitjaGlobal.setText(String.format("%.2f", mitja));

        int record = db.getMaxVictoriesRecordSQL();
        lblRecordGlobal.setText(String.valueOf(record));

        // 2. DADES DEL JUGADOR ACTUAL (SI HA INICIAT SESSIÓ)
        String user = PantallaMenu.getLoggedInUser();
        if (user != null) {
            int id = db.getIDJugador(user);
            int vics = db.getVictoriesSQL(id);
            lblVictoriesPropies.setText(String.valueOf(vics));
        } else {
            lblVictoriesPropies.setText("-");
        }

        // 3. CONFIGURACIÓ DE LES TAULES AMB ELS MAPES DE DADES DE LA BBDD
        colNomPartides.setCellValueFactory(new MapValueFactory<>("NOM_JUGADOR"));
        colTotalPartides.setCellValueFactory(new MapValueFactory<>("TOTAL"));

        colNomRecord.setCellValueFactory(new MapValueFactory<>("NOM_JUGADOR"));
        colMaxVics.setCellValueFactory(new MapValueFactory<>("VICTORIES"));

        colNomSobreMitja.setCellValueFactory(new MapValueFactory<>("NOM_JUGADOR"));
        colVicsSobreMitja.setCellValueFactory(new MapValueFactory<>("VICTORIES"));

        cargarRankings();
    }

    /**
     * RECUPERA I MOSTRA ELS RÀNQUINGS DETALLATS DES DE LA BASE DE DADES.
     */
    private void cargarRankings() {
        // RÀNQUING PER NOMBRE TOTAL DE PARTIDES JUGADES
        ArrayList<LinkedHashMap<String, String>> resPartides = db.getRankingPartidesTotalsSQL();
        ObservableList<Map> itemsPartides = FXCollections.observableArrayList(resPartides);
        tblRankingPartides.setItems(itemsPartides);

        // RÀNQUING DE JUGADORS AMB MÉS VICTÒRIES (RÈCORD)
        ArrayList<LinkedHashMap<String, String>> resRecord = db.getJugadorsRecordSQL();
        ObservableList<Map> itemsRecord = FXCollections.observableArrayList(resRecord);
        tblRankingRecord.setItems(itemsRecord);

        // JUGADORS QUE ESTAN PER SOBRE DE LA MITJANA GLOBAL
        ArrayList<LinkedHashMap<String, String>> resSobreMitja = db.getJugadorsSobreMitjaSQL();
        ObservableList<Map> itemsSobreMitja = FXCollections.observableArrayList(resSobreMitja);
        tblRankingSobreMitja.setItems(itemsSobreMitja);
    }

    /**
     * CALCULA Quin PERCENTATGE DE JUGADORS SUPERES SEGONS LES VICTÒRIES INTRODUÏDES.
     */
    @FXML
    private void handleCalcularPercentatge(ActionEvent event) {
        try {
            int vics = Integer.parseInt(txtPuntuacio.getText());
            double perc = db.getPercentatgeMenysVictoriesSQL(vics);
            lblResultatPercentatge.setText(String.format("SUPERES AL %.1f%% DELS JUGADORS!", perc));
        } catch (NumberFormatException e) {
            lblResultatPercentatge.setText("INTRODUEIX UN NÚMERO VÀLID.");
        }
    }

    /**
     * TORNA AL MENÚ PRINCIPAL DEL JOC.
     */
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
