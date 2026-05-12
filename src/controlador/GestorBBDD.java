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
    private java.util.List<String> logsConnexio = new java.util.ArrayList<>();

    // FILTRE PER MOSTRAR NOMÉS USUARIS REALS (EXCLOU BOTS I JUGADORS NO REGISTRATS)
    private static final String FILTRE_USUARIS = "contrasenya IS NOT NULL AND contrasenya != 'BOT_PWD' " +
            "AND nom_jugador NOT LIKE 'Jugador %' AND nom_jugador NOT LIKE 'CPU %' " +
            "AND nom_jugador NOT LIKE 'BOT %' AND nom_jugador NOT LIKE 'Foca %'";

    /**
     * CONSTRUCTOR QUE ESTABLEIX LA CONNEXIÓ AUTOMÀTICAMENT.
     * PRIORITZA LA XARXA LOCAL I COMMUTA A LA REMOTA SI ÉS NECESSARI.
     */
    public GestorBBDD() {
        logsConnexio.add("INICIANT CONNEXIÓ A LA BBDD...");
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
        
        if (this.conexion == null) {
            logsConnexio.add("INTENTANT CONNEXIÓ REMOTA...");
            this.conexion = conectarDirecte(URL_REMOTO, USER_PROJ, PASS_PROJ);
        }
        
        if (this.conexion != null) {
            logsConnexio.add("CONNEXIÓ ESTABLERTA AMB ÈXIT.");
            assegurarEstructuraPLSQL();
            inicializarTablasMaestras();
        } else {
            logsConnexio.add("NO S'HA POGUT ESTABLIR CAP CONNEXIÓ AMB EL SERVIDOR.");
        }
    }

    public java.util.List<String> getLogsConnexio() { return logsConnexio; }

    public Connection getConexion() { return conexion; }

    /**
     * MÈTODE INTERN PER ESTABLIR LA CONNEXIÓ JDBC AMB EL DRIVER D'ORACLE.
     */
    private Connection conectarDirecte(String url, String user, String pwd) {
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
            logsConnexio.add("ERROR CONNEXIÓ (" + url + "): " + e.getMessage());
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
     * INICIALITZA LES TAULES AMB DADES PER DEFECTE SI NO EXISTEIXEN.
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
            if (resTau.isEmpty() || Integer.parseInt(resTau.get(0).get("TOTAL")) == 0) {
                ejecutar(conexion, "INSERT INTO taulell (id_taulell, mida_taulell) VALUES (1, 50)");
            }
            
            // ELIMINAR COLUMNA COLOR_JUGADOR SI EXISTEIX (ALTRE MANTENIMENT)
            ArrayList<LinkedHashMap<String, String>> colCheck = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='COLOR_JUGADOR'");
            if (!colCheck.isEmpty()) {
                ejecutar(conexion, "ALTER TABLE jugador DROP COLUMN color_jugador");
            }

            // REGISTRE DE BOTS PER A PERSISTÈNCIA (SI NO EXISTEIXEN)
            for (int i = 1; i <= 4; i++) {
                String botName = "BOT " + i;
                ArrayList<LinkedHashMap<String, String>> existBot = select(conexion, "SELECT id_jugador FROM jugador WHERE nom_jugador = '" + botName + "'");
                if (existBot.isEmpty()) {
                    ejecutar(conexion, "INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) VALUES (99" + i + ", '" + botName + "', 0, 'BOT_PWD')");
                }
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
                        String nUpper = nom.toUpperCase();
                        pin.setEsIA(nUpper.contains("CPU") || nUpper.startsWith("BOT") || nUpper.contains("IA"));
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

    // ══════════════════════════════════════════════════════════════════════════
    // MÈTODES D'ESTADÍSTIQUES — CRIDEN FUNCIONS I PROCEDIMENTS PL/SQL
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * OBTÉ LES VICTÒRIES D'UN JUGADOR PEL SEU ID.
     */
    public int getVictoriesSQL(int idJugador) {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT victories FROM jugador WHERE id_jugador = " + idJugador);
        if (res.isEmpty() || res.get(0).get("VICTORIES") == null) return 0;
        return Integer.parseInt(res.get(0).get("VICTORIES"));
    }

    /**
     * CRIDA LA FUNCIÓ PL/SQL GET_MITJA_VICTORIES.
     * Retorna la mitjana de victòries entre tots els jugadors.
     */
    public double getMitjaGlobalSQL() {
        double result = 0.0;
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{? = call GET_MITJA_VICTORIES}")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.execute();
                result = cs.getDouble(1);
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL GET_MITJA_VICTORIES: " + e.getMessage());
            }
        }
        return result;
    }

    public double getMitjaVictoriesSQL() { return getMitjaGlobalSQL(); }

    /**
     * CRIDA LA FUNCIÓ PL/SQL GET_MAX_VICTORIES_RECORD.
     * Retorna el màxim número de victòries (rècord).
     */
    public int getMaxVictoriesRecordSQL() {
        int result = 0;
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{? = call GET_MAX_VICTORIES_RECORD}")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.execute();
                result = cs.getInt(1);
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL GET_MAX_VICTORIES_RECORD: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * CRIDA EL PROCEDIMENT PL/SQL RANKING_PARTIDES_TOTALS.
     * Retorna un cursor amb el rànquing per partides jugades (de més a menys).
     */
    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesTotalsSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call RANKING_PARTIDES_TOTALS(?)}")) {
                cs.registerOutParameter(1, -10); // OracleTypes.CURSOR = -10
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                    fila.put("TOTAL", String.valueOf(rs.getInt("TOTAL")));
                    resultados.add(fila);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL RANKING_PARTIDES_TOTALS: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * CRIDA EL PROCEDIMENT PL/SQL GET_JUGADORS_RECORD.
     * Passa el rècord com a paràmetre i retorna els jugadors que el tenen.
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsRecordSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            int record = getMaxVictoriesRecordSQL(); // Obtenim el rècord via PL/SQL
            try (CallableStatement cs = conexion.prepareCall("{call GET_JUGADORS_RECORD(?, ?)}")) {
                cs.setInt(1, record);                // p_record IN
                cs.registerOutParameter(2, -10);     // p_cursor OUT (SYS_REFCURSOR)
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(2);
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                    fila.put("VICTORIES", String.valueOf(rs.getInt("VICTORIES")));
                    resultados.add(fila);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL GET_JUGADORS_RECORD: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * CRIDA EL PROCEDIMENT PL/SQL GET_JUGADORS_SOBRE_MITJA.
     * Retorna els jugadors amb més victòries que la mitjana global.
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsSobreMitjaSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call GET_JUGADORS_SOBRE_MITJA(?)}")) {
                cs.registerOutParameter(1, -10);     // p_cursor OUT (SYS_REFCURSOR)
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                    fila.put("VICTORIES", String.valueOf(rs.getInt("VICTORIES")));
                    resultados.add(fila);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL GET_JUGADORS_SOBRE_MITJA: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * CRIDA LA FUNCIÓ PL/SQL PERCENTATGE_MENYS_VICTORIES.
     * Passa el nº de victòries i retorna el % de jugadors que en tenen menys.
     */
    public double getPercentatgeMenysVictoriesSQL(int vics) {
        double result = 0.0;
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{? = call PERCENTATGE_MENYS_VICTORIES(?)}")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC); // RETURN NUMBER
                cs.setInt(2, vics);                                  // p_vics IN
                cs.execute();
                result = cs.getDouble(1);
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL PERCENTATGE_MENYS_VICTORIES: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * CRIDA EL PROCEDIMENT PL/SQL CONSULTAR_ESTADISTIQUES_JUGADOR.
     * Retorna victòries, partides totals i posició al rànquing.
     * Controla errors: -20001 (jugador no existeix) i -20002 (sense partides).
     */
    public LinkedHashMap<String, String> consultarEstadistiquesJugador(String nom) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall(
                    "{call CONSULTAR_ESTADISTIQUES_JUGADOR(?, ?, ?, ?)}")) {
                cs.setString(1, nom);                                       // p_nom IN
                cs.registerOutParameter(2, java.sql.Types.NUMERIC);         // p_vics OUT
                cs.registerOutParameter(3, java.sql.Types.NUMERIC);         // p_total_partides OUT
                cs.registerOutParameter(4, java.sql.Types.NUMERIC);         // p_posicio_ranking OUT
                cs.execute();
                result.put("VICTORIES", String.valueOf(cs.getInt(2)));
                result.put("TOTAL_PARTIDES", String.valueOf(cs.getInt(3)));
                result.put("POSICIO_RANKING", String.valueOf(cs.getInt(4)));
            } catch (SQLException e) {
                // CONTROL D'ERRORS PL/SQL
                String msg = e.getMessage();
                if (msg != null && msg.contains("20001")) {
                    result.put("ERROR", "El jugador '" + nom + "' no existeix a la base de dades.");
                } else if (msg != null && msg.contains("20002")) {
                    result.put("ERROR", "El jugador '" + nom + "' encara no ha guardat cap partida.");
                } else {
                    result.put("ERROR", "Error inesperat: " + msg);
                }
            }
        }
        return result;
    }
}
