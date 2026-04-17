package controlador;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * Gestiona la connexió i les operacions BBDD del joc.
 * Implementació final alineada amb l'Informe Tècnic Oficial.
 */
public class GestorBBDD {

    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    private Connection conexion;

    public GestorBBDD() {
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
        // Inicialitzem dades estàtiques si les taules estan buides
        inicializarTablasMaestras();
    }

    public Connection getConexion() { return conexion; }

    private static Connection conectarDirecte(String url, String user, String pwd) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, user, pwd);
            if (con != null && con.isValid(5)) {
                System.out.println("Connectat a la BBDD correctament.");
            }
            return con;
        } catch (Exception e) {
            System.out.println("Error connexió: " + e.getMessage());
        }
        return null;
    }

    public static Connection conectarBaseDatos(Scanner scan) {
        return conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
    }

    public static void cerrar(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
        if (con == null) return 0;
        try (Statement st = con.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error " + etiqueta + ": " + e.getMessage());
            return 0;
        }
    }

    public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (con == null) return resultados;
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int numColumnas = meta.getColumnCount();
            while (rs.next()) {
                LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= numColumnas; i++) {
                    fila.put(meta.getColumnLabel(i).toUpperCase(), rs.getString(i));
                }
                resultados.add(fila);
            }
        } catch (SQLException e) {
            System.out.println("Error SELECT: " + e.getMessage());
        }
        return resultados;
    }

    private boolean ejecutar(Connection con, String sql) {
        if (con == null) return false;
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            // Silenciem errors d'insercions duplicades en càrrega inicial
            if (!e.getMessage().contains("ORA-00001")) {
                System.out.println("Error SQL: " + e.getMessage());
            }
            return false;
        }
    }

    private void commit(Connection con) {
        try { if (con != null) con.commit(); } catch (SQLException ignored) {}
    }

    /**
     * Omple les taules mestres si estan buides (Annex 02).
     */
    private void inicializarTablasMaestras() {
        if (conexion == null) return;
        
        // 1. Tipus de casella
        ArrayList<LinkedHashMap<String, String>> resTipus = select(conexion, "SELECT COUNT(*) as TOTAL FROM tipus_casella");
        if (resTipus.isEmpty() || Integer.parseInt(resTipus.get(0).get("TOTAL")) == 0) {
            System.out.println("Poblant taules mestres...");
            ejecutar(conexion, "INSERT INTO tipus_casella VALUES (1, 'Normal', 'Sense efecte')");
            ejecutar(conexion, "INSERT INTO tipus_casella VALUES (2, 'Os', 'Retorna a l''inici')");
            ejecutar(conexion, "INSERT INTO tipus_casella VALUES (3, 'Forat', 'Retrocedeix')");
            ejecutar(conexion, "INSERT INTO tipus_casella VALUES (4, 'Trineu', 'Avanca')");
            ejecutar(conexion, "INSERT INTO tipus_casella VALUES (5, 'Interrogant', 'Event aleatori')");
        }

        // 2. Taulell 1
        ArrayList<LinkedHashMap<String, String>> resTau = select(conexion, "SELECT COUNT(*) as TOTAL FROM taulell WHERE id_taulell = 1");
        if (resTau.isEmpty() || Integer.parseInt(resTau.get(0).get("TOTAL")) == 0) {
            ejecutar(conexion, "INSERT INTO taulell VALUES (1, 50)");
        }

        // 3. Caselles per defecte
        ArrayList<LinkedHashMap<String, String>> resCas = select(conexion, "SELECT COUNT(*) as TOTAL FROM casella WHERE id_taulell = 1");
        if (resCas.isEmpty() || Integer.parseInt(resCas.get(0).get("TOTAL")) == 0) {
            for (int i = 1; i <= 50; i++) {
                int tipus = 1; // Majoritariament normals
                if (i == 4 || i == 15 || i == 30) tipus = 2; // Ous
                if (i == 10 || i == 25 || i == 40) tipus = 3; // Forats
                if (i == 5 || i == 20 || i == 35) tipus = 4; // Trineus
                if (i % 7 == 0) tipus = 5; // Interrogants
                ejecutar(conexion, "INSERT INTO casella (id_casella, id_taulell, id_tipus, numero_casella) VALUES (" + i + ", 1, " + tipus + ", " + i + ")");
            }
        }
        commit(conexion);
    }

    // ── MÈTODES ESPECÍFICS JOC ──────────────────────────────────────────────

    public boolean registrarUsuario(String username) {
        if (conexion == null) return false;
        if (!select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) return true;

        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = 1;
        if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
        
        String sql = "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories) VALUES (" + nextId + ", '" + username + "', 'Blau', 0)";
        return executeInsUpDel(conexion, sql, "Registro") > 0;
    }

    public int getIDJugador(String username) {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT id_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
        if (res.isEmpty() || res.get(0).get("ID_JUGADOR") == null) return -1;
        return Integer.parseInt(res.get(0).get("ID_JUGADOR"));
    }

    public boolean loginUsuario(String username) {
        return !select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty();
    }

    public void guardarBBDD(Partida p) {
        if (conexion == null) return;
        try {
            java.util.List<Jugador> jugadores = p.getJugadores();
            for (Jugador j : jugadores) {
                if (getIDJugador(j.getNombre()) == -1) registrarUsuario(j.getNombre());
            }

            int idPartida = p.getId();
            if (idPartida <= 0) {
                ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_partida) as MAX_ID FROM partida");
                idPartida = 1;
                if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) idPartida = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
                p.setId(idPartida);
            }

            int numTorn = p.getJugadorActual() + 1;
            String sqlP = "MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                    "WHEN MATCHED THEN UPDATE SET torn_actual = " + numTorn + " " +
                    "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual) " +
                    "VALUES (" + idPartida + ", 1, 'Partida #" + idPartida + "', SYSDATE, " + numTorn + ")";
            ejecutar(conexion, sqlP);

            for (int i = 0; i < jugadores.size(); i++) {
                Jugador j = jugadores.get(i);
                int idJ = getIDJugador(j.getNombre());
                if (idJ == -1) continue;

                if (p.isFinalizada() && j.equals(p.getGanador())) {
                    ejecutar(conexion, "UPDATE jugador SET victories = victories + 1 WHERE id_jugador = " + idJ);
                }

                int d=0, pe=0, b=0;
                if (j instanceof Pinguino pin && pin.getInv() != null) {
                    d = pin.getInv().contarItems("DadoRapido") + pin.getInv().contarItems("DadoLento");
                    pe = pin.getInv().contarItems("Peces");
                    b = pin.getInv().contarItems("BolaNieve");
                }

                ejecutar(conexion, "MERGE INTO jugador_partida dst USING (SELECT " + idJ + " AS id_j, " + idPartida + " AS id_p FROM dual) src " +
                        "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) " +
                        "WHEN MATCHED THEN UPDATE SET posicio_actual = " + j.getPosicion() + ", daus = " + d + ", peixos = " + pe + ", boles_neu = " + b + " " +
                        "WHEN NOT MATCHED THEN INSERT (id_jugador, id_partida, posicio_actual, daus, peixos, boles_neu) " +
                        "VALUES (" + idJ + ", " + idPartida + ", " + j.getPosicion() + ", " + d + ", " + pe + ", " + b + ")");

                ejecutar(conexion, "MERGE INTO torn dst USING (SELECT " + idJ + " AS id_j, " + idPartida + " AS id_p FROM dual) src " +
                        "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) " +
                        "WHEN MATCHED THEN UPDATE SET ordre = " + (i + 1) + " " +
                        "WHEN NOT MATCHED THEN INSERT (id_jugador, id_partida, ordre) VALUES (" + idJ + ", " + idPartida + ", " + (i + 1) + ")");
            }
            commit(conexion);
            System.out.println("Desat correctament.");
        } catch (Exception ex) {
            System.out.println("Error al desar: " + ex.getMessage());
        }
    }

    public Partida cargarBBDD(int id_partida) {
        Partida p = new Partida();
        p.setId(id_partida);
        if (conexion == null) return p;

        ArrayList<LinkedHashMap<String, String>> resP = select(conexion, "SELECT * FROM partida WHERE id_partida = " + id_partida);
        if (resP.isEmpty()) return p;

        int ordreActual = resP.get(0).get("TORN_ACTUAL") != null ? Integer.parseInt(resP.get(0).get("TORN_ACTUAL")) : 1;

        String sqlJ = "SELECT j.nom_jugador, j.color_jugador, jp.posicio_actual, jp.daus, jp.peixos, jp.boles_neu " +
                      "FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
                      "JOIN torn t ON (j.id_jugador = t.id_jugador AND jp.id_partida = t.id_partida) " +
                      "WHERE jp.id_partida = " + id_partida + " ORDER BY t.ordre ASC";

        ArrayList<LinkedHashMap<String, String>> filas = select(conexion, sqlJ);
        for (LinkedHashMap<String, String> f : filas) {
            String nom = f.get("NOM_JUGADOR");
            String col = f.get("COLOR_JUGADOR") != null ? f.get("COLOR_JUGADOR") : "Blau";
            int pos = f.get("POSICIO_ACTUAL") != null ? Integer.parseInt(f.get("POSICIO_ACTUAL")) : 1;

            Jugador j;
            if (nom.toLowerCase().contains("foca")) {
                j = new Foca();
            } else {
                Inventario inv = new Inventario();
                int dd = f.get("DAUS")!=null?Integer.parseInt(f.get("DAUS")):0;
                int pp = f.get("PEIXOS")!=null?Integer.parseInt(f.get("PEIXOS")):0;
                int bb = f.get("BOLES_NEU")!=null?Integer.parseInt(f.get("BOLES_NEU")):0;
                if(dd>0) inv.anadirItem(new Dado("n", dd, 1, 6));
                if(pp>0) inv.anadirItem(new Pez(pp));
                if(bb>0) inv.anadirItem(new BolaDeNieve(bb));
                Pinguino pin = new Pinguino(nom, col, pos, inv);
                pin.setEsIA(nom.toLowerCase().contains("cpu"));
                j = pin;
            }
            j.setPosicion(pos);
            j.setColor(col);
            p.anadirJugador(j);
        }
        p.setJugadorActual(ordreActual - 1);
        return p;
    }

    public ArrayList<Integer> getListaPartidas() {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<LinkedHashMap<String, String>> resList = select(conexion, "SELECT id_partida FROM partida ORDER BY id_partida DESC");
        for (LinkedHashMap<String, String> f : resList) ids.add(Integer.parseInt(f.get("ID_PARTIDA")));
        return ids;
    }
}