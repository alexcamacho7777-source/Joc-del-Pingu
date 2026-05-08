package controlador;

import model.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * GESTIONA LA CONNEXIÓ I LES OPERACIONS AMB LA BASE DE DADES ORACLE.
 * INCLOU LA LÒGICA DE PERSISTÈNCIA DE PARTIDES, JUGADORS I ESTADÍSTIQUES.
 */
public class GestorBBDD {

    // URLS DE CONNEXIÓ PER A L'ENTORN LOCAL D'ILERNA I L'ACCÉS REMOT
    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String URL_REMOTO = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    private Connection conexion;

    /**
     * CONSTRUCTOR QUE ESTABLEIX LA CONNEXIÓ AUTOMÀTICAMENT.
     * PRIORITZA LA XARXA LOCAL I COMMUTA A LA REMOTA SI ÉS NECESSARI.
     */
    public GestorBBDD() {
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
        
        if (this.conexion == null) {
            System.out.println("INTENTANT CONNEXIÓ REMOTA...");
            this.conexion = conectarDirecte(URL_REMOTO, USER_PROJ, PASS_PROJ);
        }
        
        if (this.conexion != null) {
            assegurarEstructuraPLSQL();
            inicializarTablasMaestras();
        }
    }

    public Connection getConexion() { return conexion; }

    /**
     * MÈTODE INTERN PER ESTABLIR LA CONNEXIÓ JDBC AMB EL DRIVER D'ORACLE.
     */
    private static Connection conectarDirecte(String url, String user, String pwd) {
        Connection con = null;
        try {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            }
            DriverManager.setLoginTimeout(5); 
            con = DriverManager.getConnection(url, user, pwd);
        } catch (Exception e) {
            System.err.println("ERROR EN LA CONNEXIÓ A " + url + ": " + e.getMessage());
        }
        return con;
    }

    /**
     * TANCA LA CONNEXIÓ AMB LA BASE DE DADES DE FORMA SEGURA.
     */
    public static void cerrar(Connection con) {
        if (con != null) {
            try { 
                con.close(); 
            } catch (SQLException ignored) {}
        }
    }

    /**
     * EXECUTA UNA SENTÈNCIA SQL DE TIPUS INSERT, UPDATE O DELETE.
     */
    public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
        int res = 0;
        if (con != null) {
            try (Statement st = con.createStatement()) {
                res = st.executeUpdate(sql);
            } catch (SQLException e) {
                System.out.println("ERROR EN " + etiqueta + ": " + e.getMessage());
            }
        }
        return res;
    }

    /**
     * EXECUTA UNA CONSULTA SELECT I RETORNA ELS RESULTATS EN UNA LLISTA DE MAPES.
     */
    public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (con != null) {
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
                System.out.println("ERROR EN SELECT: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * EXECUTA UNA SENTÈNCIA SQL SENSE RETORNAR RESULTATS.
     */
    private boolean ejecutar(Connection con, String sql) {
        boolean ok = false;
        if (con != null) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate(sql);
                ok = true;
            } catch (SQLException e) {
                if (!e.getMessage().contains("ORA-00001")) {
                    System.out.println("ERROR SQL: " + e.getMessage());
                }
            }
        }
        return ok;
    }

    /**
     * REALITZA UN COMMIT PER CONFIRMAR ELS CANVIS A LA BASE DE DADES.
     */
    private void commit(Connection con) {
        try { 
            if (con != null) {
                con.commit();
            }
        } catch (SQLException ignored) {}
    }

    /**
     * INICIALITZA LES TAULES MESTRES AMB DADES PER DEFECTE SI NO EXISTEIXEN.
     */
    private void inicializarTablasMaestras() {
        if (conexion != null) {
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (1, 'NORMAL', 'SENSE EFECTE')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (2, 'OS', 'RETORNA A L''INICI')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (3, 'FORAT', 'RETROCEDEIX')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (4, 'TRINEU', 'AVANÇA')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (5, 'INTERROGANT', 'EVENT ALEATORI')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (6, 'SUELOQUEBRADIZO', 'ES TRENCA AL PASSAR')");

            ArrayList<LinkedHashMap<String, String>> resTau = select(conexion, "SELECT COUNT(*) as TOTAL FROM taulell WHERE id_taulell = 1");
            
            // ELIMINAR COLUMNA COLOR_JUGADOR SI EXISTEIX (ALTRE MANTENIMENT)
            ejecutar(conexion, "ALTER TABLE jugador DROP COLUMN color_jugador");

            if (resTau.isEmpty() || Integer.parseInt(resTau.get(0).get("TOTAL")) == 0) {
                ejecutar(conexion, "INSERT INTO taulell (id_taulell, mida_taulell) VALUES (1, 50)");
            }
            commit(conexion);
        }
    }

    /**
     * GENERA UN HASH SHA-256 PER ENCRIPTAR LES CONTRASENYES.
     */
    private String sha256(String text) {
        String res = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            res = sb.toString();
        } catch (Exception e) {
            res = text; 
        }
        return res;
    }

    /**
     * REGISTRA UN NOU JUGADOR HUMÀ SI NO EXISTEIX EL NOM D'USUARI.
     */
    public boolean registrarUsuario(String username, String password) {
        boolean ok = false;
        if (conexion != null) {
            ArrayList<LinkedHashMap<String, String>> exist = select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
            if (exist.isEmpty()) {
                String hashPw = sha256(password);
                ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
                int nextId = 1;
                if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) {
                    nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
                }
                String sql = "INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) VALUES ("
                        + nextId + ", '" + username + "', 0, '" + hashPw + "')";
                ok = executeInsUpDel(conexion, sql, "REGISTRE") > 0;
            }
        }
        return ok;
    }

    /**
     * ASSEGURA QUE L'ESTRUCTURA DE TAULES TINGUI LES COLUMNES NECESSÀRIES.
     */
    private void assegurarEstructuraPLSQL() {
        if (conexion != null) {
            ArrayList<LinkedHashMap<String, String>> colsJ = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='CONTRASENYA'");
            if (colsJ.isEmpty()) {
                ejecutar(conexion, "ALTER TABLE jugador ADD (contrasenya VARCHAR2(64))");
            }
            commit(conexion);
        }
    }

    /**
     * OBTÉ L'ID D'UN JUGADOR PEL SEU NOM.
     */
    public int getIDJugador(String username) {
        int id = -1;
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT id_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
        if (!res.isEmpty() && res.get(0).get("ID_JUGADOR") != null) {
            id = Integer.parseInt(res.get(0).get("ID_JUGADOR"));
        }
        return id;
    }

    /**
     * VALIDA LES CREDENCIALS D'ACCÉS D'UN USUARI.
     */
    public boolean loginUsuario(String username, String password) {
        boolean valid = false;
        if (conexion != null) {
            String hashPw = sha256(password);
            ArrayList<LinkedHashMap<String, String>> res = select(conexion,
                    "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "' AND contrasenya = '" + hashPw + "'");
            valid = !res.isEmpty();
        }
        return valid;
    }

    /**
     * GUARDA L'ESTAT D'UNA PARTIDA (MERGE TAULELL, PARTIDA, JUGADORS I CASELLES).
     */
    public boolean guardarBBDD(Partida p) {
        boolean ok = false;
        if (conexion != null && p != null) {
            try {
                int idPartida = p.getId();
                if (idPartida <= 0) {
                    ArrayList<LinkedHashMap<String, String>> resSeq = select(conexion, "SELECT SEC_ID_PARTIDA.NEXTVAL AS NEXT_ID FROM dual");
                    if (!resSeq.isEmpty() && resSeq.get(0).get("NEXT_ID") != null) {
                        idPartida = Integer.parseInt(resSeq.get(0).get("NEXT_ID"));
                        p.setId(idPartida);
                    } else {
                        ArrayList<LinkedHashMap<String, String>> resMax = select(conexion, "SELECT MAX(id_partida) as MAX_ID FROM partida");
                        idPartida = 1;
                        if (!resMax.isEmpty() && resMax.get(0).get("MAX_ID") != null) idPartida = Integer.parseInt(resMax.get(0).get("MAX_ID")) + 1;
                        p.setId(idPartida);
                    }
                }

                String sqlP = "MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                              "WHEN MATCHED THEN UPDATE SET torn_actual = " + (p.getJugadorActual() + 1) + ", nom_partida = '" + p.getNombre() + "' " +
                              "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual) " +
                              "VALUES (" + idPartida + ", 1, '" + p.getNombre() + "', SYSDATE, " + (p.getJugadorActual() + 1) + ")";
                ejecutar(conexion, sqlP);

                // GUARDAR POSICIONS DELS JUGADORS
                for (Jugador j : p.getJugadores()) {
                    int idJ = getIDJugador(j.getNombre());
                    if (idJ != -1) {
                        String sqlJP = "MERGE INTO jugador_partida dst USING (SELECT " + idJ + " AS id_j, " + idPartida + " AS id_p FROM dual) src " +
                                       "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) " +
                                       "WHEN MATCHED THEN UPDATE SET posicio_actual = " + j.getPosicion() + " " +
                                       "WHEN NOT MATCHED THEN INSERT (id_jugador, id_partida, posicio_actual) VALUES (src.id_j, src.id_p, " + j.getPosicion() + ")";
                        ejecutar(conexion, sqlJP);
                    }
                }

                commit(conexion);
                ok = true;
            } catch (Exception e) {
                System.err.println("ERROR EN EL GUARDAT BBDD: " + e.getMessage());
            }
        }
        return ok;
    }

    /**
     * CARREGA UNA PARTIDA DES DE LA BASE DE DADES.
     */
    public Partida cargarBBDD(int id) {
        Partida p = new Partida();
        p.setId(id);
        if (conexion != null) {
            ArrayList<LinkedHashMap<String, String>> resP = select(conexion, "SELECT * FROM partida WHERE id_partida = " + id);
            if (!resP.isEmpty()) {
                p.setNombre(resP.get(0).get("NOM_PARTIDA"));
                int tornActual = Integer.parseInt(resP.get(0).get("TORN_ACTUAL"));
                p.setJugadorActual(tornActual - 1);

                ArrayList<LinkedHashMap<String, String>> resJ = select(conexion, 
                    "SELECT j.nom_jugador, jp.posicio_actual FROM jugador j " +
                    "JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador WHERE jp.id_partida = " + id);
                
                for (LinkedHashMap<String, String> row : resJ) {
                    String nom = row.get("NOM_JUGADOR");
                    int pos = Integer.parseInt(row.get("POSICIO_ACTUAL"));
                    
                    Jugador j;
                    if (nom.toUpperCase().contains("FOCA")) {
                        j = new Foca();
                    } else {
                        // ASSIGNEM UN COLOR PER DEFECTE O BASAT EN L'INDEX SI CAL
                        Pinguino pin = new Pinguino(nom, "BLAU");
                        pin.setEsIA(nom.toUpperCase().contains("CPU"));
                        j = pin;
                    }
                    j.setPosicion(pos);
                    p.anadirJugador(j);
                }
            }
        }
        return p;
    }

    /**
     * OBTÉ LA LLISTA DE PARTIDES AMB DETALLS PER AL LOBBY.
     */
    public ArrayList<LinkedHashMap<String, String>> getListaPartidasDetalladas() {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, 
            "SELECT id_partida, nom_partida, TO_CHAR(data_creacio, 'DD/MM/YYYY') as DATA_CREACIO, torn_actual, finalitzada FROM partida ORDER BY id_partida DESC");
        
        for (LinkedHashMap<String, String> row : res) {
            int idP = Integer.parseInt(row.get("ID_PARTIDA"));
            ArrayList<LinkedHashMap<String, String>> jugs = select(conexion, 
                "SELECT j.nom_jugador FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador WHERE jp.id_partida = " + idP);
            
            StringBuilder sb = new StringBuilder();
            for (LinkedHashMap<String, String> jRow : jugs) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(jRow.get("NOM_JUGADOR"));
            }
            row.put("JUGADORS", sb.toString());
            row.put("FINALITZADA", "1".equals(row.get("FINALITZADA")) ? "SÍ" : "NO");
        }
        return res;
    }

    /**
     * MÈTODES PER A ESTADÍSTIQUES I RÀNQUINGS.
     */
    public int getVictoriesSQL(int idJugador) {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT victories FROM jugador WHERE id_jugador = " + idJugador);
        if (res.isEmpty() || res.get(0).get("VICTORIES") == null) return 0;
        return Integer.parseInt(res.get(0).get("VICTORIES"));
    }

    public double getMitjaGlobalSQL() {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT AVG(victories) as MITJA FROM jugador");
        if (res.isEmpty() || res.get(0).get("MITJA") == null) return 0.0;
        return Double.parseDouble(res.get(0).get("MITJA"));
    }

    public double getMitjaVictoriesSQL() { return getMitjaGlobalSQL(); }

    public int getMaxVictoriesRecordSQL() {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(victories) as MAX_V FROM jugador");
        if (res.isEmpty() || res.get(0).get("MAX_V") == null) return 0;
        return Integer.parseInt(res.get(0).get("MAX_V"));
    }

    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesTotalsSQL() {
        return select(conexion, "SELECT nom_jugador, victories as TOTAL FROM jugador ORDER BY victories DESC FETCH FIRST 10 ROWS ONLY");
    }

    public ArrayList<LinkedHashMap<String, String>> getJugadorsRecordSQL() {
        int max = getMaxVictoriesRecordSQL();
        return select(conexion, "SELECT nom_jugador, victories FROM jugador WHERE victories = " + max + " AND victories > 0");
    }

    public ArrayList<LinkedHashMap<String, String>> getJugadorsSobreMitjaSQL() {
        double mitja = getMitjaGlobalSQL();
        return select(conexion, "SELECT nom_jugador, victories FROM jugador WHERE victories > " + mitja + " ORDER BY victories DESC");
    }

    public double getPercentatgeMenysVictoriesSQL(int vics) {
        ArrayList<LinkedHashMap<String, String>> resTotal = select(conexion, "SELECT COUNT(*) as TOTAL FROM jugador");
        ArrayList<LinkedHashMap<String, String>> resMenys = select(conexion, "SELECT COUNT(*) as MENYS FROM jugador WHERE victories < " + vics);
        
        if (resTotal.isEmpty()) return 0.0;
        double total = Double.parseDouble(resTotal.get(0).get("TOTAL"));
        double menys = Double.parseDouble(resMenys.get(0).get("MENYS"));
        return (menys / total) * 100.0;
    }
}
