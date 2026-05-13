package controlador;

import model.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * AQUESTA CLASSE Ã‰S EL "PONT" ENTRE EL JOC I LA BASE DE DADES.
 * SERVEIX PER ENVIAR I REBRE TOTA LA INFORMACIÃ“ DELS JUGADORS I LES PARTIDES.
 * TOT ES FA AMB ORACLE I PL/SQL PERQUÃˆ SIGUI MOLT RÃ€PID I SEGUR.
 */
public class GestorBBDD {

    // URLS DE CONNEXIÃ“ PER A L'ENTORN LOCAL D'ILERNA I L'ACCÃ‰S REMOT
    // Es defineixen dues rutes per garantir que el joc funcioni tant a classe com des de casa.
    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String URL_REMOTO = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    // Atributs d'instÃ ncia per mantenir l'estat de la sessiÃ³ actual
    private Connection conexion;
    private java.util.List<String> logsConnexio = new java.util.ArrayList<>();

    // FILTRE PER MOSTRAR NOMÃ‰S USUARIS REALS (EXCLOU BOTS I JUGADORS NO REGISTRATS)
    // Aquesta constant s'utilitza en consultes SQL per filtrar jugadors automÃ tics o temporals.
    private static final String FILTRE_USUARIS = "contrasenya IS NOT NULL AND contrasenya != 'BOT_PWD' " +
            "AND nom_jugador NOT LIKE 'Jugador %' AND nom_jugador NOT LIKE 'CPU %' " +
            "AND nom_jugador NOT LIKE 'BOT %' AND nom_jugador NOT LIKE 'Foca %'";

    /**
     * QUAN CREEM EL GESTOR, INTENTA CONNECTAR-SE A LA BASE DE DADES.
     * PRIMER PROVA A LA CLASSE (LOCAL) I SI NO POT, PROVA DES DE CASA (REMOT).
     * UN COP CONNECTAT, REVISA QUE TOTES LES TAULES ESTIGUIN BÃ‰.
     */
    public GestorBBDD() {
        logsConnexio.add("INICIANT CONNEXIÃ“ A LA BBDD...");
        
        // ReduÃ¯m el timeout inicial a 2 segons per detectar rÃ pidament si no som a la xarxa local d'Ilerna.
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ, 2);
        
        if (this.conexion == null) {
            logsConnexio.add("SERVIDOR LOCAL NO TROBAT. INTENTANT CONNEXIÃ“ REMOTA...");
            // El timeout remot Ã©s superior (7s) ja que la latÃ¨ncia d'Internet pot ser major.
            this.conexion = conectarDirecte(URL_REMOTO, USER_PROJ, PASS_PROJ, 7);
        }
        
        if (this.conexion != null) {
            logsConnexio.add("CONNEXIÃ“ ESTABLERTA AMB ÃˆXIT.");
            assegurarEstructuraPLSQL();     // Verifica columnes com 'contrasenya'
            inicializarTablasMaestras();   // Insereix tipus de caselles si falten
        } else {
            logsConnexio.add("CRÃTIC: NO S'HA POGUT CONNECTAR AMB CAP SERVIDOR.");
        }
    }

    /** @return Llista cronolÃ²gica dels intents i errors de connexiÃ³. */
    public java.util.List<String> getLogsConnexio() { return logsConnexio; }

    /** @return L'objecte Connection actiu o null si no n'hi ha cap. */
    public Connection getConexion() { return conexion; }

    /**
     * AQUÃ Ã‰S ON FEM LA CONNEXIÃ“ REAL AMB EL SERVIDOR.
     * LI DIEM ON Ã‰S EL SERVIDOR, L'USUARI I LA CONTRASENYA.
     * SI EL SERVIDOR NO RESPON EN POCS SEGONS, DONA UN ERROR DE TIMEOUT.
     */
    private Connection conectarDirecte(String url, String user, String pwd, int timeout) {
        Connection con = null;
        try {
            // Intentem carregar el driver d'Oracle (compatible amb múltiples versions)
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            }
            // Establim el límit d'espera per evitar que l'aplicació es quedi congelada
            DriverManager.setLoginTimeout(timeout); 
            con = DriverManager.getConnection(url, user, pwd);
            if (con != null) con.setAutoCommit(false); // Assegurem control de transaccions
        } catch (Exception e) {
            String msg = e.getMessage();
            // Capturem el timeout específic d'Oracle (ORA-12170) per donar un feedback clar
            if (msg != null && (msg.contains("ORA-12170") || msg.contains("timeout"))) {
                logsConnexio.add("TIMEOUT A " + url + " (" + timeout + "s).");
            } else {
                logsConnexio.add("ERROR A " + url + ": " + (msg != null ? msg : "Error desconegut"));
            }
        }
        return con;
    }

    /** Verifica si la connexió encara és vàlida. */
    public boolean isConexionViva() {
        try {
            return conexion != null && !conexion.isClosed() && conexion.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * TANCA LA CONNEXIÃ“ AMB LA BASE DE DADES DE FORMA SEGURA.
     * @param con La connexiÃ³ que es vol tancar.
     */
    public static void cerrar(Connection con) {
        if (con != null) {
            try { 
                con.close(); 
            } catch (SQLException ignored) {
                // Ignorem l'error si la connexiÃ³ ja estava tancada
            }
        }
    }

    /**
     * @deprecated Aquest mÃ¨tode utilitza SQL estÃ ndard. S'ha de prioritzar l'Ãºs de PL/SQL.
     * Es mantÃ© nomÃ©s per compatibilitat interna amb eines de manteniment.
     */
    @Deprecated
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
     * @deprecated Aquest mÃ¨tode utilitza SQL estÃ ndard. S'ha de prioritzar l'Ãºs de PL/SQL i cursors.
     */
    @Deprecated
    public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (con != null) {
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int numColumnas = meta.getColumnCount();
                while (rs.next()) {
                    LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= numColumnas; i++) {
                        // Normalitzem els noms de les columnes a majÃºscules per evitar conflictes
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
     * EXECUTA UNA SENTÃˆNCIA SQL SENSE RETORNAR RESULTATS (ÃšTIL PER A MANTENIMENT).
     * @return true si s'ha executat correctament, false si hi ha hagut error (excepte clau duplicada).
     */
    private boolean ejecutar(Connection con, String sql) {
        boolean ok = false;
        if (con != null) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate(sql);
                ok = true;
            } catch (SQLException e) {
                String msg = e.getMessage();
                // ORA-00001 Ã©s clau duplicada; sovint l'ignorem en insercions de dades mestres
                if (msg != null && !msg.contains("ORA-00001")) {
                    System.out.println("ERROR SQL: " + msg);
                }
            }
        }
        return ok;
    }

    /**
     * REALITZA UN COMMIT PER CONFIRMAR ELS CANVIS A LA BASE DE DADES.
     * Imprescindible quan no es treballa amb AutoCommit activat.
     */
    private void commit(Connection con) {
        try { 
            if (con != null) {
                con.commit();
            }
        } catch (SQLException ignored) {}
    }

    /**
     * INICIALITZA LES TAULES AMB DADES PER DEFECTE (VIA PL/SQL).
     */
    private void inicializarTablasMaestras() {
        if (conexion != null) {
            String pl = "BEGIN " +
                        "  -- Tipus de caselles\n" +
                        "  BEGIN INSERT INTO tipus_casella VALUES (1, 'NORMAL', 'SENSE EFECTE'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  BEGIN INSERT INTO tipus_casella VALUES (2, 'OS', 'RETORNA A L''INICI'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  BEGIN INSERT INTO tipus_casella VALUES (3, 'FORAT', 'RETROCEDEIX'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  BEGIN INSERT INTO tipus_casella VALUES (4, 'TRINEU', 'AVANÃ‡A'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  BEGIN INSERT INTO tipus_casella VALUES (5, 'INTERROGANT', 'EVENT ALEATORI'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  BEGIN INSERT INTO tipus_casella VALUES (6, 'SUELOQUEBRADIZO', 'ES TRENCA AL PASSAR'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  \n" +
                        "  -- Taulell\n" +
                        "  BEGIN INSERT INTO taulell (id_taulell, mida_taulell) VALUES (1, 50); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  \n" +
                        "  -- Bots i Foca\n" +
                        "  FOR i IN 1..4 LOOP\n" +
                        "    BEGIN INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) \n" +
                        "    VALUES (990+i, 'BOT '||i, 0, 'BOT_PWD'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "  END LOOP;\n" +
                        "  BEGIN INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) \n" +
                        "  VALUES (995, 'FOCA (CPU)', 0, 'FOCA_PWD'); EXCEPTION WHEN OTHERS THEN NULL; END;\n" +
                        "END;";
            try (CallableStatement cs = conexion.prepareCall(pl)) {
                cs.execute();
                commit(conexion);
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL (INIT): " + e.getMessage());
            }
        }
    }

    /**
     * GENERA UN HASH SHA-256 PER ENCRIPTAR LES CONTRASENYES.
     * @param text Text en clar que es vol encriptar.
     * @return El hash hexadecimal resultant de 64 carÃ cters.
     */
    private String sha256(String text) {
        String res = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                // Formatem cada byte a dos dÃ­gits hexadecimals
                sb.append(String.format("%02x", b));
            }
            res = sb.toString();
        } catch (Exception e) {
            // Fallback en cas d'error de l'algorisme (poc probable)
            res = text; 
        }
        return res;
    }

    /**
     * SERVEIX PER CREAR UN NOU JUGADOR A LA BASE DE DADES.
     * ENCRIPTEM LA CONTRASENYA PERQUÃˆ NINGÃš LA POGUI VEURE.
     * SI EL NOM JA EXISTEIX, ENS DIRÃ€ QUE NO S'HA POGUT CREAR.
     */
    public boolean registrarUsuario(String username, String password) {
        boolean ok = false;
        if (conexion != null && username != null && password != null) {
            String hash = sha256(password);
            // Bloc PL/SQL per comprovar existÃ¨ncia i inserir amb ID calculat
            String pl = "DECLARE v_exists NUMBER; v_id NUMBER; BEGIN " +
                        "SELECT COUNT(*) INTO v_exists FROM jugador WHERE nom_jugador = ?; " +
                        "IF v_exists = 0 THEN " +
                        "  SELECT NVL(MAX(id_jugador),0) + 1 INTO v_id FROM jugador; " +
                        "  INSERT INTO jugador (id_jugador, nom_jugador, victories, contrasenya) VALUES (v_id, ?, 0, ?); " +
                        "  ? := 1; " +
                        "ELSE ? := 0; END IF; END;";
            try (CallableStatement cs = conexion.prepareCall(pl)) {
                cs.setString(1, username);
                cs.setString(2, username);
                cs.setString(3, hash);
                cs.registerOutParameter(4, java.sql.Types.NUMERIC);
                cs.registerOutParameter(5, java.sql.Types.NUMERIC);
                cs.execute();
                ok = (cs.getInt(4) == 1);
                commit(conexion);
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL (REGISTRE): " + e.getMessage());
            }
        }
        return ok;
    }


    /**
     * OBTÃ‰ L'ID D'UN JUGADOR PEL SEU NOM (VIA PL/SQL).
     * @param username Nom del jugador a cercar.
     * @return L'ID numÃ¨ric o -1 si no es troba.
     */
    public synchronized int getIDJugador(String username) {
        int id = -1;
        if (isConexionViva() && username != null) {
            try (CallableStatement cs = conexion.prepareCall("BEGIN SELECT id_jugador INTO ? FROM jugador WHERE UPPER(nom_jugador) = UPPER(?); END;")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.setString(2, username);
                cs.execute();
                id = cs.getInt(1);
            } catch (Exception e) {
                // NO_DATA_FOUND o error de connexió
            }
        }
        return id;
    }

    /**
     * VALIDA LES CREDENCIALS D'UN USUARI (VIA PL/SQL).
     * @param username Nom del jugador.
     * @param password Contrasenya en text pla.
     * @return true si les credencials sÃ³n vÃ lides.
     */
    public boolean loginUsuario(String username, String password) {
        boolean valid = false;
        if (conexion != null && username != null && password != null) {
            String hash = sha256(password);
            try (CallableStatement cs = conexion.prepareCall("BEGIN SELECT 1 INTO ? FROM jugador WHERE nom_jugador = ? AND contrasenya = ?; END;")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.setString(2, username);
                cs.setString(3, hash);
                cs.execute();
                valid = (cs.getInt(1) == 1);
            } catch (SQLException e) {
                // Credencials incorrectes
            }
        }
        return valid;
    }

    /**
     * AQUEST Ã‰S EL MÃˆTODE MÃ‰S IMPORTANT: GUARDA LA PARTIDA.
     * GUARDA ON Ã‰S CADA JUGADOR, DE QUI Ã‰S EL TORN I SI S'HA ACABAT.
     * FA SERVIR "MERGE", QUE VOL DIR: SI JA EXISTEIX ACTUALITZA, SI NO CREA.
     */
    public synchronized boolean guardarBBDD(Partida p) {
        boolean ok = false;
        if (isConexionViva() && p != null) {
            try {
                int idPartida = p.getId();
                if (idPartida <= 0) {
                    idPartida = getNextIDPartida();
                    p.setId(idPartida);
                }

                // Bloc PL/SQL per al MERGE de la capçalera de la partida
                String plP = "BEGIN " +
                             "MERGE INTO partida dst USING (SELECT ? AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                             "WHEN MATCHED THEN UPDATE SET torn_actual = ?, nom_partida = ?, finalitzada = ? " +
                             "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual, finalitzada) " +
                             "VALUES (?, 1, ?, SYSDATE, ?, ?); " +
                             "END;";
                try (CallableStatement cs = conexion.prepareCall(plP)) {
                    cs.setInt(1, idPartida);
                    cs.setInt(2, p.getJugadorActual() + 1);
                    cs.setString(3, p.getNombre());
                    cs.setInt(4, p.isFinalizada() ? 1 : 0);
                    cs.setInt(5, idPartida);
                    cs.setString(6, p.getNombre());
                    cs.setInt(7, p.getJugadorActual() + 1);
                    cs.setInt(8, p.isFinalizada() ? 1 : 0);
                    cs.execute();
                }

                // Bloc PL/SQL per al MERGE de la posició de cada jugador
                String plJP = "BEGIN " +
                              "MERGE INTO jugador_partida dst USING (SELECT ? AS id_j, ? AS id_p FROM dual) src " +
                              "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) " +
                              "WHEN MATCHED THEN UPDATE SET posicio_actual = ? " +
                              "WHEN NOT MATCHED THEN INSERT (id_jugador, id_partida, posicio_actual) VALUES (src.id_j, src.id_p, ?); " +
                              "END;";
                for (Jugador j : p.getJugadores()) {
                    int idJ = getIDJugador(j.getNombre());
                    if (idJ != -1) {
                        try (CallableStatement cs = conexion.prepareCall(plJP)) {
                            cs.setInt(1, idJ);
                            cs.setInt(2, idPartida);
                            cs.setInt(3, j.getPosicion());
                            cs.setInt(4, j.getPosicion());
                            cs.execute();
                        }
                    }
                }

                // Incrementar victòries via PL/SQL si la partida ha acabat
                if (p.isFinalizada() && p.getGanador() != null) {
                    incrementarVictoriesGuanyador(getIDJugador(p.getGanador().getNombre()));
                }

                commit(conexion);
                ok = true;
            } catch (Exception e) {
                String msg = e.getMessage();
                System.err.println("ERROR PL/SQL (GUARDAT BBDD): " + (msg != null ? msg : e.toString()));
                // e.printStackTrace(); // Evitem saturar el log si ja tenim el missatge
            }
        }
        return ok;
    }

    /**
     * CARREGA UNA PARTIDA COMPLETAMENT DES DE LA BASE DE DADES (VIA PL/SQL CURSOR).
     * @param id Identificador de la partida a carregar.
     * @return Objecte Partida inicialitzat.
     */
    public Partida cargarBBDD(int id) {
        Partida p = new Partida();
        p.setId(id);
        if (conexion != null) {
            try {
                // 1. Carreguem dades bÃ siques de la partida via Cursor PL/SQL
                try (CallableStatement cs = conexion.prepareCall("BEGIN OPEN ? FOR SELECT * FROM partida WHERE id_partida = ?; END;")) {
                    cs.registerOutParameter(1, -10); // Oracle CURSOR
                    cs.setInt(2, id);
                    cs.execute();
                    ResultSet rs = (ResultSet) cs.getObject(1);
                    if (rs != null && rs.next()) {
                        p.setNombre(rs.getString("NOM_PARTIDA"));
                        p.setJugadorActual(rs.getInt("TORN_ACTUAL") - 1);
                        p.setFinalizada(rs.getInt("FINALITZADA") == 1);
                        rs.close();
                    }
                }

                // 2. Carreguem els jugadors que participen via Cursor PL/SQL
                try (CallableStatement cs = conexion.prepareCall("BEGIN OPEN ? FOR SELECT j.nom_jugador, jp.posicio_actual FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador WHERE jp.id_partida = ?; END;")) {
                    cs.registerOutParameter(1, -10);
                    cs.setInt(2, id);
                    cs.execute();
                    ResultSet rs = (ResultSet) cs.getObject(1);
                    if (rs != null) {
                        while (rs.next()) {
                            String nom = rs.getString("NOM_JUGADOR");
                            int pos = rs.getInt("POSICIO_ACTUAL");
                            Jugador j = (nom.toUpperCase().contains("FOCA")) ? new Foca() : new Pinguino(nom, "BLAU");
                            j.setNombre(nom);
                            j.setPosicion(pos);
                            p.anadirJugador(j);
                        }
                        rs.close();
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL (CÃ€RREGA BBDD): " + e.getMessage());
            }
        }
        return p;
    }

    /**
     * OBTÉ LA LLISTA DE PARTIDES AMB DETALLS PER AL LOBBY (VIA PL/SQL CURSOR).
     * @return Llista on cada mapa inclou dades de la partida i noms de jugadors concatenats.
     */
    public ArrayList<LinkedHashMap<String, String>> getListaPartidasDetalladas() {
        ArrayList<LinkedHashMap<String, String>> res = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("BEGIN OPEN ? FOR SELECT id_partida, nom_partida, TO_CHAR(data_creacio, 'DD/MM/YYYY') as DATA_CREACIO, torn_actual, finalitzada FROM partida ORDER BY id_partida DESC; END;")) {
                cs.registerOutParameter(1, -10); // Oracle CURSOR
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                if (rs != null) {
                    while (rs.next()) {
                        LinkedHashMap<String, String> row = new LinkedHashMap<>();
                        int idP = rs.getInt("ID_PARTIDA");
                        row.put("ID_PARTIDA", String.valueOf(idP));
                        row.put("NOM_PARTIDA", rs.getString("NOM_PARTIDA"));
                        row.put("DATA_CREACIO", rs.getString("DATA_CREACIO"));
                        row.put("TORN_ACTUAL", String.valueOf(rs.getInt("TORN_ACTUAL")));
                        row.put("FINALITZADA", (rs.getInt("FINALITZADA") == 1 ? "SI" : "NO"));
                        
                        row.put("JUGADORS", getNomsJugadorsPartidaPLSQL(idP));
                        res.add(row);
                    }
                    rs.close();
                }
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL getListaPartidasDetalladas: " + e.getMessage());
            }
        }
        return res;
    }

    /** MÃ¨tode auxiliar PL/SQL per obtenir els noms de jugadors d'una partida. */
    private String getNomsJugadorsPartidaPLSQL(int idP) {
        StringBuilder sb = new StringBuilder();
        try (CallableStatement cs = conexion.prepareCall("BEGIN OPEN ? FOR SELECT j.nom_jugador FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador WHERE jp.id_partida = ?; END;")) {
            cs.registerOutParameter(1, -10); // Oracle CURSOR
            cs.setInt(2, idP);
            cs.execute();
            ResultSet rs = (ResultSet) cs.getObject(1);
            if (rs != null) {
                while (rs.next()) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(rs.getString("NOM_JUGADOR"));
                }
                rs.close();
            }
        } catch (Exception e) {
            System.err.println("ERROR PL/SQL getNomsJugadorsPartidaPLSQL: " + e.getMessage());
        }
        return sb.toString();
    }
               // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // APARTAT DE PL/SQL AVANÃ‡AT: FUNCIONS, PROCEDIMENTS I TRIGGERS
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // JOC Dâ€™EN PINGU â€“ PL/SQL FUNCIONALITAT (ORDENAT I ENUMERAT)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    /**
     * EXTRA. OBTÃ‰ LES VICTÃ’RIES D'UN JUGADOR (VIA PL/SQL).
     */
    public int getVictoriesSQL(int idJugador) {
        int vics = 0;
        if (conexion != null && idJugador != -1) {
            try (CallableStatement cs = conexion.prepareCall("BEGIN SELECT victories INTO ? FROM jugador WHERE id_jugador = ?; END;")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.setInt(2, idJugador);
                cs.execute();
                vics = cs.getInt(1);
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL getVictoriesSQL: " + e.getMessage());
            }
        }
        return vics;
    }

    /**
     * EXTRA. OBTÃ‰ EL RÃ€NQUING GLOBAL DE VICTÃ’RIES (VIA PL/SQL CURSOR).
     */
    public ArrayList<LinkedHashMap<String, String>> getRankingGlobalVictoriesSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("BEGIN OPEN ? FOR SELECT nom_jugador, victories FROM jugador WHERE victories > 0 ORDER BY victories DESC; END;")) {
                cs.registerOutParameter(1, -10); // Oracle CURSOR
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                if (rs != null) {
                    while (rs.next()) {
                        LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                        fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                        fila.put("VICTORIES", String.valueOf(rs.getInt("VICTORIES")));
                        resultados.add(fila);
                    }
                    rs.close();
                }
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL getRankingGlobalVictoriesSQL: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * PUNT 1: DEMANEM UN NÃšMERO NOU PER A UNA PARTIDA NOVA.
     * FEM SERVIR UNA "SEQUENCE" D'ORACLE PERQUÃˆ ELS NÃšMEROS NO ES REPETEIXIN.
     */
    public int getNextIDPartida() {
        int id = -1;
        if (conexion != null) {
            // Bloc anÃ²nim PL/SQL per obtenir el NEXTVAL sense fer un SELECT directe des de Java
            try (CallableStatement cs = conexion.prepareCall("BEGIN ? := SEC_ID_PARTIDA.NEXTVAL; END;")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.execute();
                id = cs.getInt(1);
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL (PUNT 1): " + e.getMessage());
            }
        }
        return id;
    }

    /**
     * EXERCICI 2. (T) ASSIGNAR AUTOMÃ€TICAMENT EL NÂº SEQÃœENCIAL A LA CLAU PRIMÃ€RIA.
     * Aquesta operaciÃ³ es realitza invocant el bloc PL/SQL del Punt 1 abans de la inserciÃ³.
     */


    /**
     * PUNT 3: SUMEM UNA VICTÃ’RIA AL JUGADOR QUE HA GUANYAT.
     * AIXÃ’ ES FA DIRECTAMENT AMB UN UPDATE DINS D'UN BLOC PL/SQL.
     */
    public void incrementarVictoriesGuanyador(int idJugador) {
        if (conexion != null && idJugador != -1) {
            // Ãšs obligatori de PL/SQL per incrementar el comptador de victÃ²ries
            try (CallableStatement cs = conexion.prepareCall("BEGIN UPDATE jugador SET victories = victories + 1 WHERE id_jugador = ?; END;")) {
                cs.setInt(1, idJugador);
                cs.execute();
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL (PUNT 3): " + e.getMessage());
            }
        }
    }

    /**
     * EXERCICI 4. (F) OBTENIR EL MÃ€XIM RÃˆCORD DE VICTÃ’RIES (RECORD GLOBAL).
     * Invoca la funciÃ³ PL/SQL 'GET_MAX_VICTORIES_RECORD'.
     */
    public int getMaxVictoriesRecordSQL() {
        int result = 0;
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{? = call GET_MAX_VICTORIES_RECORD}")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.execute();
                result = cs.getInt(1);
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL (PUNT 4): " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * EXERCICI 5. (P) OBTENIR ELS JUGADORS QUE TENEN EL RÃˆCORD ACTUAL.
     * Invoca el procediment 'GET_JUGADORS_RECORD' mitjanÃ§ant un cursor.
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsRecordSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call GET_JUGADORS_RECORD(?)}")) {
                cs.registerOutParameter(1, -10); // Oracle CURSOR
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                if (rs != null) {
                    while (rs.next()) {
                        LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                        fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                        fila.put("VICTORIES", String.valueOf(rs.getInt("VICTORIES")));
                        resultados.add(fila);
                    }
                    rs.close();
                }
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL (PUNT 5): " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * EXERCICI 6. (F) OBTENIR LA MITJANA DE VICTÃ’RIES GLOBAL.
     * Invoca la funciÃ³ PL/SQL 'GET_MITJA_VICTORIES'.
     */
    public double getMitjaGlobalSQL() {
        double result = 0.0;
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{? = call GET_MITJA_VICTORIES}")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.execute();
                result = cs.getDouble(1);
            } catch (SQLException e) {
                System.err.println("ERROR PL/SQL (PUNT 6): " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * EXERCICI 7. (P) MOSTRAR JUGADORS AMB MÃ‰S VICTÃ’RIES QUE LA MITJANA.
     * Invoca el procediment PL/SQL 'GET_JUGADORS_SOBRE_MITJA'.
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsSobreMitjaSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call GET_JUGADORS_SOBRE_MITJA(?)}")) {
                cs.registerOutParameter(1, -10);     
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                if (rs != null) {
                    while (rs.next()) {
                        LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                        fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                        fila.put("VICTORIES", String.valueOf(rs.getInt("VICTORIES")));
                        resultados.add(fila);
                    }
                    rs.close();
                }
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL (PUNT 7): " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * PUNT 8: CALCULEM QUINA POSICIÃ“ TÃ‰ EL JUGADOR AL RÃ€NQUING.
     * ENS DIU EL PERCENTATGE DE GENT QUE TÃ‰ MENYS VICTÃ’RIES QUE NOSALTRES.
     */
    public double getPercentatgeMenysVictoriesSQL(int vics) {
        double result = 0.0;
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{? = call PERCENTATGE_MENYS_VICTORIES(?)}")) {
                cs.registerOutParameter(1, java.sql.Types.NUMERIC);
                cs.setInt(2, vics);
                cs.execute();
                result = cs.getDouble(1);
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL (PUNT 8): " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * 9. (T) MOSTRAR AUTOMÃ€TICAMENT EL PERCENTIL QUAN S'INCREMENTEN VICTÃ’RIES.
     * Implementat mitjanÃ§ant un 'Compound Trigger' definit Ã­ntegrament en PL/SQL.
     */
    private void assegurarEstructuraPLSQL() {
        if (conexion != null) {
            // VerificaciÃ³ i actualitzaciÃ³ d'esquema via PL/SQL pur (EXECUTE IMMEDIATE)
            String plSchema = "DECLARE v_count NUMBER; BEGIN " +
                              "SELECT COUNT(*) INTO v_count FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='CONTRASENYA'; " +
                              "IF v_count = 0 THEN EXECUTE IMMEDIATE 'ALTER TABLE jugador ADD (contrasenya VARCHAR2(64))'; END IF; " +
                              "END;";
            try (CallableStatement cs = conexion.prepareCall(plSchema)) {
                cs.execute();
            } catch (SQLException e) {}
            
            // DefiniciÃ³ i execuciÃ³ del Trigger Compost per a la gestiÃ³ del rÃ nquing en temps real
            String triggerFix = 
                "CREATE OR REPLACE TRIGGER TRG_AVIS_RANKING\n" +
                "FOR UPDATE OF victories ON JUGADOR\n" +
                "COMPOUND TRIGGER\n" +
                "  TYPE t_jugador_rec IS RECORD (nom VARCHAR2(100), vics NUMBER);\n" +
                "  TYPE t_jugador_tab IS TABLE OF t_jugador_rec;\n" +
                "  v_jugadors t_jugador_tab := t_jugador_tab();\n" +
                "  \n" +
                "  AFTER EACH ROW IS\n" +
                "  BEGIN\n" +
                "    v_jugadors.EXTEND;\n" +
                "    v_jugadors(v_jugadors.LAST).nom := :NEW.nom_jugador;\n" +
                "    v_jugadors(v_jugadors.LAST).vics := :NEW.victories;\n" +
                "  END AFTER EACH ROW;\n" +
                "  \n" +
                "  AFTER STATEMENT IS\n" +
                "    v_perc NUMBER;\n" +
                "  BEGIN\n" +
                "    FOR i IN 1 .. v_jugadors.COUNT LOOP\n" +
                "      v_perc := PERCENTATGE_MENYS_VICTORIES(v_jugadors(i).vics);\n" +
                "      DBMS_OUTPUT.PUT_LINE('AVIS: ' || v_jugadors(i).nom || ' supera al ' || ROUND(v_perc, 2) || '%');\n" +
                "    END LOOP;\n" +
                "  END AFTER STATEMENT;\n" +
                "END;";
            
            try (CallableStatement cs = conexion.prepareCall("BEGIN EXECUTE IMMEDIATE ?; END;")) {
                cs.setString(1, triggerFix);
                cs.execute();
            } catch (SQLException e) {
                String msg = e.getMessage();
                if (msg != null && !msg.contains("already exists")) {
                    System.err.println("ERROR PL/SQL (TRIGGER): " + msg);
                }
            }
            commit(conexion);
        }
    }

    /**
     * EXERCICI 10. (P) MOSTRAR EL RÃ€NQUING DE JUGADORS PER TOTAL DE PARTIDES JUGADES.
     * Invoca el procediment 'RANKING_PARTIDES_TOTALS'.
     */
    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesTotalsSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call RANKING_PARTIDES_TOTALS(?)}")) {
                cs.registerOutParameter(1, -10); // Oracle CURSOR
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(1);
                if (rs != null) {
                    while (rs.next()) {
                        LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                        fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                        fila.put("TOTAL", String.valueOf(rs.getInt("TOTAL")));
                        resultados.add(fila);
                    }
                    rs.close();
                }
            } catch (Exception e) {
                System.err.println("ERROR PL/SQL (PUNT 10): " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * EXERCICI 11. (P) MOSTRAR LA POSICIÃ“ AL RÃ€NQUING I ESTADÃSTIQUES D'UN JUGADOR.
     * Invoca el procediment 'CONSULTAR_ESTADISTIQUES_JUGADOR' amb control d'errors PL/SQL.
     */
    public LinkedHashMap<String, String> consultarEstadistiquesJugador(String nom) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call CONSULTAR_ESTADISTIQUES_JUGADOR(?, ?, ?, ?)}")) {
                cs.setString(1, nom);                                       
                cs.registerOutParameter(2, java.sql.Types.NUMERIC); 
                cs.registerOutParameter(3, java.sql.Types.NUMERIC); 
                cs.registerOutParameter(4, java.sql.Types.NUMERIC); 
                cs.execute();
                result.put("VICTORIES", String.valueOf(cs.getInt(2)));
                result.put("TOTAL_PARTIDES", String.valueOf(cs.getInt(3)));
                result.put("POSICIO_RANKING", String.valueOf(cs.getInt(4)));
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("20001")) result.put("ERROR", "El jugador '" + nom + "' no existeix.");
                else if (msg != null && msg.contains("20002")) result.put("ERROR", "El jugador '" + nom + "' no tÃ© dades.");
                else {
                    // Si el procediment falla, intentem la versiÃ³ de 3 parÃ metres com a contingÃ¨ncia PL/SQL
                    return consultarEstadistiquesJugadorV3(nom);
                }
            }
        }
        return result;
    }

    /**
     * VersiÃ³ simplificada (V3) de consulta per a sistemes que no suporten el cÃ lcul de rÃ nquing.
     */
    private LinkedHashMap<String, String> consultarEstadistiquesJugadorV3(String nom) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try (CallableStatement cs = conexion.prepareCall("{call CONSULTAR_ESTADISTIQUES_JUGADOR(?, ?, ?)}")) {
            cs.setString(1, nom);
            cs.registerOutParameter(2, java.sql.Types.NUMERIC);
            cs.registerOutParameter(3, java.sql.Types.NUMERIC);
            cs.execute();
            result.put("VICTORIES", String.valueOf(cs.getInt(2)));
            result.put("TOTAL_PARTIDES", String.valueOf(cs.getInt(3)));
            result.put("POSICIO_RANKING", "N/A");
        } catch (SQLException e) {}
        return result;
    }

}
