package controlador;

import model.*;

import java.security.MessageDigest;
import java.sql.*;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * Gestiona la connexió i les operacions BBDD del joc.
 * Implementació final alineada amb l'Informe Tècnic Oficial.
 */
public class GestorBBDD {

    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String URL_REMOTO = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    private Connection conexion;

    public GestorBBDD() {
        // Intentem connexió local primer
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
        
        // Si falla (p.ex. estem fora del centre), intentem la remota
        if (this.conexion == null) {
            System.out.println("Intentant connexió remota (oracle.ilerna.com)...");
            this.conexion = conectarDirecte(URL_REMOTO, USER_PROJ, PASS_PROJ);
        }
        
        // Inicialitzem dades estàtiques i estructura si les taules estan buides
        if (this.conexion != null) {
            assegurarEstructuraPLSQL();
            inicializarTablasMaestras();
        }
    }

    public Connection getConexion() { return conexion; }

    private static Connection conectarDirecte(String url, String user, String pwd) {
        try {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                // Alternativa por compatibilidad con versiones antiguas
                Class.forName("oracle.jdbc.driver.OracleDriver");
            }
            // Establir un timeout curt per a la prova (segons)
            DriverManager.setLoginTimeout(5); 
            Connection con = DriverManager.getConnection(url, user, pwd);
            if (con != null && con.isValid(5)) {
                System.out.println("Connectat a " + url + " correctament.");
            }
            return con;
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No s'ha trobat el Driver Oracle (ojdbc8.jar).");
            System.err.println("Assegura't que ojdbc8.jar estigui al Build Path (Modulepath en Java 9+).");
        } catch (Exception e) {
            System.err.println("Error connexió: " + e.getMessage());
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
        System.out.println("Verificant taules mestres...");
        ejecutar(conexion, "INSERT INTO tipus_casella VALUES (1, 'Normal', 'Sense efecte')");
        ejecutar(conexion, "INSERT INTO tipus_casella VALUES (2, 'Os', 'Retorna a l''inici')");
        ejecutar(conexion, "INSERT INTO tipus_casella VALUES (3, 'Forat', 'Retrocedeix')");
        ejecutar(conexion, "INSERT INTO tipus_casella VALUES (4, 'Trineu', 'Avanca')");
        ejecutar(conexion, "INSERT INTO tipus_casella VALUES (5, 'Interrogant', 'Event aleatori')");
        ejecutar(conexion, "INSERT INTO tipus_casella VALUES (6, 'SueloQuebradizo', 'Es trenca al passar')");

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

    // ── UTILITAT: SHA-256 ────────────────────────────────────────────────────
    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculant hash", e);
        }
    }

    /**
     * Registra un nou usuari amb contrasenya (hash SHA-256).
     * Retorna false si l'usuari ja existeix.
     */
    public boolean registrarUsuario(String username, String password) {
        if (conexion == null) return false;
        // Si ja existeix, no el tornem a crear
        if (!select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) return false;

        String hashPw = sha256(password);
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = 1;
        if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;

        // Intentem inserir amb la columna contrasenya
        assegurarEstructuraPLSQL();

        String sql = "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories, contrasenya) VALUES ("
                + nextId + ", '" + username + "', 'Blau', 0, '" + hashPw + "')";
        return executeInsUpDel(conexion, sql, "Registro") > 0;
    }

    /**
     * Afegeix les columnes i estructura necessària per a la nova funcionalitat PL/SQL si no existeixen.
     */
    private void assegurarEstructuraPLSQL() {
        if (conexion == null) return;
        
        // 1. Columnes a JUGADOR
        ArrayList<LinkedHashMap<String, String>> colsJ = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='CONTRASENYA'");
        if (colsJ.isEmpty()) ejecutar(conexion, "ALTER TABLE jugador ADD (contrasenya VARCHAR2(64))");
        
        colsJ = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='VICTORIES'");
        if (colsJ.isEmpty()) ejecutar(conexion, "ALTER TABLE jugador ADD (victories NUMBER DEFAULT 0)");

        // 2. Columnes a PARTIDA
        ArrayList<LinkedHashMap<String, String>> colsP = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='PARTIDA' AND column_name='FINALITZADA'");
        if (colsP.isEmpty()) ejecutar(conexion, "ALTER TABLE partida ADD (finalitzada NUMBER(1) DEFAULT 0)");

        colsP = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='PARTIDA' AND column_name='ID_GUANYADOR'");
        if (colsP.isEmpty()) ejecutar(conexion, "ALTER TABLE partida ADD (id_guanyador NUMBER)");

        System.out.println("Estructura de taules verificada.");
        commit(conexion);
    }

    public int getIDJugador(String username) {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT id_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
        if (res.isEmpty() || res.get(0).get("ID_JUGADOR") == null) return -1;
        return Integer.parseInt(res.get(0).get("ID_JUGADOR"));
    }

    /**
     * Valida login comprovant usuari I contrasenya (hash SHA-256).
     * Retorna true si les credencials coincideixen.
     */
    public boolean loginUsuario(String username, String password) {
        if (conexion == null) return false;
        assegurarEstructuraPLSQL();

        String hashPw = sha256(password);
        ArrayList<LinkedHashMap<String, String>> res = select(conexion,
                "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "' AND contrasenya = '" + hashPw + "'");
        return !res.isEmpty();
    }

    /**
     * Registra un jugador (CPU/IA) sense contrasenya.
     * Només per a jugadors artificials generats pel joc, no per a humans.
     */
    private void registrarJugadorSenseContrasenya(String username) {
        if (conexion == null) return;
        if (!select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) return;
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = 1;
        if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
        executeInsUpDel(conexion, "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories) VALUES ("
                + nextId + ", '" + username + "', 'Gris', 0)", "RegistreIA");
    }

    public void guardarBBDD(Partida p) {
        if (conexion == null) return;
        try {
            java.util.List<Jugador> jugadores = p.getJugadores();
            for (Jugador j : jugadores) {
                if (getIDJugador(j.getNombre()) == -1) registrarJugadorSenseContrasenya(j.getNombre());
            }

            int idPartida = p.getId();
            if (idPartida <= 0) {
                ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_partida) as MAX_ID FROM partida");
                idPartida = 1;
                if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) idPartida = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
                p.setId(idPartida);
            }

            int numTorn = p.getJugadorActual() + 1;
            
            // 1. Guardar el taulell primer (per evitar error de clau forana ORA-02291 a PARTIDA)
            if (p.getTablero() != null) {
                ejecutar(conexion, "MERGE INTO taulell dst USING (SELECT " + idPartida + " AS id_t FROM dual) src ON (dst.id_taulell = src.id_t) " +
                        "WHEN NOT MATCHED THEN INSERT VALUES (" + idPartida + ", " + p.getTablero().getTotalCasillas() + ")");
            }

            int idGuanyador = (p.isFinalizada() && p.getGanador() != null) ? getIDJugador(p.getGanador().getNombre()) : -1;
            String valFinalitzada = p.isFinalizada() ? "1" : "0";
            String valGuanyador = (idGuanyador != -1) ? String.valueOf(idGuanyador) : "NULL";

            // 2. Guardar la partida (incloent estat de finalització i guanyador)
            String sqlP = "MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                    "WHEN MATCHED THEN UPDATE SET torn_actual = " + numTorn + ", nom_partida = '" + p.getNombre() + "', " +
                    "finalitzada = " + valFinalitzada + ", id_guanyador = " + valGuanyador + " " +
                    "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual, finalitzada, id_guanyador) " +
                    "VALUES (" + idPartida + ", " + idPartida + ", '" + p.getNombre() + "', SYSDATE, " + numTorn + ", " + valFinalitzada + ", " + valGuanyador + ")";
            ejecutar(conexion, sqlP);
            
            // Guardem la SEED al taulell per simplificar (si no existeix la columna, el mètode 'ejecutar' fallarà silenciosament o mostrarà error)
            ejecutar(conexion, "UPDATE taulell SET mida = " + p.getTablero().getTotalCasillas() + " WHERE id_taulell = " + idPartida);
            
            // 3. Guardar les caselles
            if (p.getTablero() != null) {
                ArrayList<Casilla> casillas = p.getTablero().getCasillas();
                for (int i = 0; i < casillas.size(); i++) {
                    Casilla c = casillas.get(i);
                    int tipus = 1;
                    if (c instanceof Oso) tipus = 2;
                    else if (c instanceof Agujero) tipus = 3;
                    else if (c instanceof Trineo) tipus = 4;
                    else if (c instanceof Evento) tipus = 5;
                    else if (c instanceof SueloQuebradizo) tipus = 6;

                    ejecutar(conexion, "MERGE INTO casella dst USING (SELECT " + idPartida + " AS id_t, " + i + " AS num_c FROM dual) src " +
                            "ON (dst.id_taulell = src.id_t AND dst.numero_casella = src.num_c) " +
                            "WHEN NOT MATCHED THEN INSERT (id_casella, id_taulell, id_tipus, numero_casella) " +
                            "VALUES (" + (idPartida * 100 + i) + ", " + idPartida + ", " + tipus + ", " + i + ")");
                }
            }

            for (int i = 0; i < jugadores.size(); i++) {
                Jugador j = jugadores.get(i);
                int idJ = getIDJugador(j.getNombre());
                if (idJ == -1) continue;


                int d=0, pe=0, b=0;
                if (j instanceof Pinguino) {
                    Pinguino pin = (Pinguino) j;
                    if (pin.getInv() != null) {
                        d = pin.getInv().contarItems("DadoRapido") + pin.getInv().contarItems("DadoLento");
                        pe = pin.getInv().contarItems("Peces");
                        b = pin.getInv().contarItems("BolaNieve");
                    }
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

        p.setNombre(resP.get(0).get("NOM_PARTIDA"));

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

        // Carregar el Taulell
        int idTaulell = resP.get(0).get("ID_TAULELL") != null ? Integer.parseInt(resP.get(0).get("ID_TAULELL")) : 1;
        ArrayList<LinkedHashMap<String, String>> resC = select(conexion, "SELECT * FROM casella WHERE id_taulell = " + idTaulell + " ORDER BY numero_casella ASC");
        if (!resC.isEmpty()) {
            ArrayList<Casilla> casillas = new ArrayList<>();
            for (LinkedHashMap<String, String> rowC : resC) {
                int tipus = Integer.parseInt(rowC.get("ID_TIPUS"));
                int pos = Integer.parseInt(rowC.get("NUMERO_CASELLA"));
                Casilla c;
                switch (tipus) {
                    case 2: c = new Oso(pos); break;
                    case 3: c = new Agujero(pos); break;
                    case 4: c = new Trineo(pos); break;
                    case 5: c = new Evento(pos); break;
                    case 6: c = new SueloQuebradizo(pos); break;
                    default: c = new Normal(pos); break;
                }
                casillas.add(c);
            }
            Tablero tab = new Tablero();
            tab.setCasillas(casillas);
            p.setTablero(tab);
        }

        return p;
    }

    public ArrayList<Integer> getListaPartidas() {
        ArrayList<Integer> ids = new ArrayList<>();
        ArrayList<LinkedHashMap<String, String>> resList = select(conexion, "SELECT id_partida FROM partida ORDER BY id_partida DESC");
        for (LinkedHashMap<String, String> f : resList) ids.add(Integer.parseInt(f.get("ID_PARTIDA")));
        return ids;
    }

    /**
     * Retorna una llista de mapes amb la informació detallada de cada partida:
     * ID, NOM, DATA, JUGADORS (llista separada per comes), TORN_ACTUAL, FINALITZADA (S/N).
     */
    public ArrayList<LinkedHashMap<String, String>> getListaPartidasDetalladas() {
        if (conexion == null) return new ArrayList<>();

        // Partides bàsiques sense la columna finalitzada que dóna error
        ArrayList<LinkedHashMap<String, String>> partides = select(conexion,
                "SELECT id_partida, nom_partida, TO_CHAR(data_creacio,'DD/MM/YYYY') AS data_creacio, " +
                "torn_actual FROM partida ORDER BY id_partida DESC");

        for (LinkedHashMap<String, String> fila : partides) {
            String idPartida = fila.get("ID_PARTIDA");
            // Jugadors de la partida
            ArrayList<LinkedHashMap<String, String>> jugadors = select(conexion,
                    "SELECT j.nom_jugador FROM jugador j " +
                    "JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
                    "WHERE jp.id_partida = " + idPartida + " ORDER BY jp.id_jugador ASC");
            StringBuilder sb = new StringBuilder();
            for (LinkedHashMap<String, String> jf : jugadors) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(jf.get("NOM_JUGADOR"));
            }
            fila.put("JUGADORS", sb.length() > 0 ? sb.toString() : "-");
            // Finalitzada
            String fin = fila.getOrDefault("FINALITZADA", null);
            fila.put("FINALITZADA", (fin != null && fin.equalsIgnoreCase("1")) ? "Si" : "No");
        }
        return partides;
    }

    /**
     * Crida la funció SQL per obtenir les victòries d'un jugador.
     */
    public int getVictoriesSQL(int idJugador) {
        if (conexion == null) return 0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_VICTORIES_JUGADOR(?) }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setInt(2, idJugador);
            cs.execute();
            return cs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error cridant GET_VICTORIES_JUGADOR: " + e.getMessage());
            return -1;
        }
    }

    public int getRecordSQL(int idJugador) {
        if (conexion == null) return 0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_RECORD_JUGADOR(?) }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setInt(2, idJugador);
            cs.execute();
            return cs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error cridant GET_RECORD_JUGADOR: " + e.getMessage());
            return -1;
        }
    }

    public double getMitjaGlobalSQL() {
        if (conexion == null) return 0.0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_MITJA_PUNTUACIO_GLOBAL }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.execute();
            return cs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error cridant GET_MITJA_PUNTUACIO_GLOBAL: " + e.getMessage());
            return 0.0;
        }
    }

    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesSQL() {
        return select(conexion, "SELECT j.nom_jugador, COUNT(jp.id_partida) as TOTAL " +
                               "FROM jugador j LEFT JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
                               "GROUP BY j.nom_jugador ORDER BY TOTAL DESC");
    }

    public ArrayList<LinkedHashMap<String, String>> getRankingRecordSQL() {
        return select(conexion, "SELECT j.nom_jugador, MAX(jp.peixos) as RECORD " +
                               "FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
                               "GROUP BY j.nom_jugador ORDER BY RECORD DESC");
    }

    /**
     * Retorna el rècord màxim de victòries registrat (Funció GET_MAX_VICTORIES_RECORD).
     */
    public int getMaxVictoriesRecordSQL() {
        if (conexion == null) return 0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_MAX_VICTORIES_RECORD }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.execute();
            return cs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error cridant GET_MAX_VICTORIES_RECORD: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Retorna els jugadors que tenen el rècord de victòries (Procediment GET_JUGADORS_RECORD).
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsRecordSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion == null) return resultados;
        try (CallableStatement cs = conexion.prepareCall("{ call GET_JUGADORS_RECORD(?) }")) {
            cs.registerOutParameter(1, -10); // -10 es el valor de OracleTypes.CURSOR
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                    fila.put("VICTORIES", rs.getString("VICTORIES"));
                    resultados.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cridant GET_JUGADORS_RECORD: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * Retorna la mitja de victòries (Funció GET_MITJA_VICTORIES).
     */
    public double getMitjaVictoriesSQL() {
        if (conexion == null) return 0.0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_MITJA_VICTORIES }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.execute();
            return cs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error cridant GET_MITJA_VICTORIES: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Retorna jugadors amb victòries per sobre de la mitja (Procediment GET_JUGADORS_SOBRE_MITJA).
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsSobreMitjaSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion == null) return resultados;
        try (CallableStatement cs = conexion.prepareCall("{ call GET_JUGADORS_SOBRE_MITJA(?) }")) {
            cs.registerOutParameter(1, -10); // -10 es el valor de OracleTypes.CURSOR
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                    fila.put("VICTORIES", rs.getString("VICTORIES"));
                    resultados.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cridant GET_JUGADORS_SOBRE_MITJA: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * Retorna el percentatge de jugadors amb menys victòries que el valor passat (Funció PERCENTATGE_MENYS_VICTORIES).
     */
    public double getPercentatgeMenysVictoriesSQL(int vics) {
        if (conexion == null) return 0.0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call PERCENTATGE_MENYS_VICTORIES(?) }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setInt(2, vics);
            cs.execute();
            return cs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error cridant PERCENTATGE_MENYS_VICTORIES: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Ranking de jugadors per total de partides jugades (Procediment RANKING_PARTIDES_TOTALS).
     */
    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesTotalsSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion == null) return resultados;
        try (CallableStatement cs = conexion.prepareCall("{ call RANKING_PARTIDES_TOTALS(?) }")) {
            cs.registerOutParameter(1, -10); // -10 es el valor de OracleTypes.CURSOR
            cs.execute();
            try (ResultSet rs = (ResultSet) cs.getObject(1)) {
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                    fila.put("TOTAL", rs.getString("TOTAL"));
                    resultados.add(fila);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error cridant RANKING_PARTIDES_TOTALS: " + e.getMessage());
        }
        return resultados;
    }
}
