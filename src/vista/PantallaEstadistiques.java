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
import javafx.scene.layout.VBox;
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

    @FXML private Label lblNomTop1, lblVicTop1, lblNomTop2, lblVicTop2, lblNomTop3, lblVicTop3;
    @FXML private TableView<Map> tblRestaRanking;
    @FXML private TableColumn<Map, String> colPosicioResta;
    @FXML private TableColumn<Map, String> colNomResta;
    @FXML private TableColumn<Map, String> colVicsResta;

    @FXML private TableView<Map> tblRankingSobreMitja;
    @FXML private TableColumn<Map, String> colNomSobreMitja;
    @FXML private TableColumn<Map, String> colVicsSobreMitja;

    @FXML private Label lblResultatPercentatge;
    
    // MISSING COMPONENTS FROM FXML
    @FXML private TextField txtPuntuacio;
    @FXML private TextField txtNomJugador;
    
    // VISUAL COMPONENTS FOR CONSULTATION
    @FXML private VBox containerResultatConsulta;
    @FXML private Label lblNomResultat;
    @FXML private Label lblVicsResultat;
    @FXML private Label lblPartResultat;
    @FXML private Label lblRankResultat;
    @FXML private Label lblErrorConsulta;

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

        colPosicioResta.setCellValueFactory(new MapValueFactory<>("POS"));
        colNomResta.setCellValueFactory(new MapValueFactory<>("NOM_JUGADOR"));
        colVicsResta.setCellValueFactory(new MapValueFactory<>("VICTORIES"));

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

        // PODI I RÀNQUING GLOBAL (Top 3 i la resta)
        ArrayList<LinkedHashMap<String, String>> resGlobal = db.getRankingGlobalVictoriesSQL();
        
        if (resGlobal.size() > 0) {
            lblNomTop1.setText(resGlobal.get(0).get("NOM_JUGADOR"));
            lblVicTop1.setText(resGlobal.get(0).get("VICTORIES") + " vics");
        }
        if (resGlobal.size() > 1) {
            lblNomTop2.setText(resGlobal.get(1).get("NOM_JUGADOR"));
            lblVicTop2.setText(resGlobal.get(1).get("VICTORIES") + " vics");
        }
        if (resGlobal.size() > 2) {
            lblNomTop3.setText(resGlobal.get(2).get("NOM_JUGADOR"));
            lblVicTop3.setText(resGlobal.get(2).get("VICTORIES") + " vics");
        }
        
        // La resta dels jugadors van a la taula
        ArrayList<LinkedHashMap<String, String>> resta = new ArrayList<>();
        for (int i = 3; i < resGlobal.size(); i++) {
            LinkedHashMap<String, String> row = resGlobal.get(i);
            row.put("POS", String.valueOf(i + 1));
            resta.add(row);
        }
        ObservableList<Map> itemsResta = FXCollections.observableArrayList(resta);
        tblRestaRanking.setItems(itemsResta);

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
     * CONSULTA LES DADES ESPECÍFIQUES D'UN JUGADOR PEL SEU NOM.
     */
    @FXML
    private void handleConsultarJugador(ActionEvent event) {
        String nom = txtNomJugador.getText().trim();
        if (nom.isEmpty()) {
            containerResultatConsulta.setVisible(false);
            lblErrorConsulta.setText("INTRODUEIX UN NOM PER CONSULTAR.");
            lblErrorConsulta.setVisible(true);
            return;
        }

        LinkedHashMap<String, String> stats = db.consultarEstadistiquesJugador(nom);
        if (stats.containsKey("ERROR")) {
            containerResultatConsulta.setVisible(false);
            lblErrorConsulta.setText(stats.get("ERROR"));
            lblErrorConsulta.setVisible(true);
        } else {
            lblErrorConsulta.setVisible(false);
            lblNomResultat.setText(nom.toUpperCase());
            lblVicsResultat.setText(stats.get("VICTORIES"));
            lblPartResultat.setText(stats.get("TOTAL_PARTIDES"));
            
            String rank = stats.get("POSICIO_RANKING");
            lblRankResultat.setText(rank.equals("N/A") ? rank : "#" + rank);
            
            containerResultatConsulta.setVisible(true);
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
