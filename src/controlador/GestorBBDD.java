package controlador;

import model.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * GESTIONA LA CONNEXIÓ I LES OPERACIONS AMB LA BASE DE DADES ORACLE.
 * Aquesta classe actua com a capa de persistència, encarregant-se de comunicar 
 * l'aplicació Java amb el servidor de base de dades. Inclou la lògica de 
 * seguretat (hashing), sincronització d'estructures (PL/SQL) i gestió de 
 * consultes complexes per a estadístiques i rànquings.
 * 
 * @author Alex Camacho (Grup 03 - Pingu)
 * @version 2.0
 */
public class GestorBBDD {

    // URLS DE CONNEXIÓ PER A L'ENTORN LOCAL D'ILERNA I L'ACCÉS REMOT
    // Es defineixen dues rutes per garantir que el joc funcioni tant a classe com des de casa.
    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String URL_REMOTO = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    // Atributs d'instància per mantenir l'estat de la sessió actual
    private Connection conexion;
    private java.util.List<String> logsConnexio = new java.util.ArrayList<>();

    // FILTRE PER MOSTRAR NOMÉS USUARIS REALS (EXCLOU BOTS I JUGADORS NO REGISTRATS)
    // Aquesta constant s'utilitza en consultes SQL per filtrar jugadors automàtics o temporals.
    private static final String FILTRE_USUARIS = "contrasenya IS NOT NULL AND contrasenya != 'BOT_PWD' " +
            "AND nom_jugador NOT LIKE 'Jugador %' AND nom_jugador NOT LIKE 'CPU %' " +
            "AND nom_jugador NOT LIKE 'BOT %' AND nom_jugador NOT LIKE 'Foca %'";

    /**
     * CONSTRUCTOR QUE ESTABLEIX LA CONNEXIÓ AUTOMÀTICAMENT.
     * El constructor intenta primer una connexió local (més ràpida) i, si falla 
     * per timeout, intenta la remota. Un cop connectat, assegura que les taules 
     * i les dades mestres estiguin correctament inicialitzades.
     */
    public GestorBBDD() {
        logsConnexio.add("INICIANT CONNEXIÓ A LA BBDD...");
        
        // Reduïm el timeout inicial a 2 segons per detectar ràpidament si no som a la xarxa local d'Ilerna.
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ, 2);
        
        if (this.conexion == null) {
            logsConnexio.add("SERVIDOR LOCAL NO TROBAT. INTENTANT CONNEXIÓ REMOTA...");
            // El timeout remot és superior (7s) ja que la latència d'Internet pot ser major.
            this.conexion = conectarDirecte(URL_REMOTO, USER_PROJ, PASS_PROJ, 7);
        }
        
        if (this.conexion != null) {
            logsConnexio.add("CONNEXIÓ ESTABLERTA AMB ÈXIT.");
            assegurarEstructuraPLSQL();     // Verifica columnes com 'contrasenya'
            inicializarTablasMaestras();   // Insereix tipus de caselles si falten
        } else {
            logsConnexio.add("CRÍTIC: NO S'HA POGUT CONNECTAR AMB CAP SERVIDOR.");
        }
    }

    /** @return Llista cronològica dels intents i errors de connexió. */
    public java.util.List<String> getLogsConnexio() { return logsConnexio; }

    /** @return L'objecte Connection actiu o null si no n'hi ha cap. */
    public Connection getConexion() { return conexion; }

    /**
     * MÈTODE INTERN PER ESTABLIR LA CONNEXIÓ JDBC AMB EL DRIVER D'ORACLE.
     * @param url Adreça del servidor Oracle.
     * @param user Nom d'usuari de l'esquema.
     * @param pwd Contrasenya de l'esquema.
     * @param timeout Temps màxim d'espera en segons.
     * @return Connection object o null si falla.
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
        } catch (Exception e) {
            String msg = e.getMessage();
            // Capturem el timeout específic d'Oracle (ORA-12170) per donar un feedback clar
            if (msg.contains("ORA-12170") || msg.contains("timeout")) {
                logsConnexio.add("TIMEOUT A " + url + " (" + timeout + "s).");
            } else {
                logsConnexio.add("ERROR A " + url + ": " + msg);
            }
        }
        return con;
    }

    /**
     * TANCA LA CONNEXIÓ AMB LA BASE DE DADES DE FORMA SEGURA.
     * @param con La connexió que es vol tancar.
     */
    public static void cerrar(Connection con) {
        if (con != null) {
            try { 
                con.close(); 
            } catch (SQLException ignored) {
                // Ignorem l'error si la connexió ja estava tancada
            }
        }
    }

    /**
     * EXECUTA UNA SENTÈNCIA SQL DE TIPUS INSERT, UPDATE O DELETE.
     * @param con Connexió activa.
     * @param sql Cadena SQL a executar.
     * @param etiqueta Descripció per al log en cas d'error (ex: "GUARDAT").
     * @return Número de files afectades.
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
     * Cada mapa representa una fila, on la clau és el nom de la columna en MAJÚSCULES.
     * @param con Connexió activa.
     * @param sql Consulta SELECT.
     * @return Llista de LinkedHashMap (per mantenir l'ordre de les columnes).
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
                        // Normalitzem els noms de les columnes a majúscules per evitar conflictes
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
     * EXECUTA UNA SENTÈNCIA SQL SENSE RETORNAR RESULTATS (ÚTIL PER A MANTENIMENT).
     * @return true si s'ha executat correctament, false si hi ha hagut error (excepte clau duplicada).
     */
    private boolean ejecutar(Connection con, String sql) {
        boolean ok = false;
        if (con != null) {
            try (Statement st = con.createStatement()) {
                st.executeUpdate(sql);
                ok = true;
            } catch (SQLException e) {
                // ORA-00001 és clau duplicada; sovint l'ignorem en insercions de dades mestres
                if (!e.getMessage().contains("ORA-00001")) {
                    System.out.println("ERROR SQL: " + e.getMessage());
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
     * INICIALITZA LES TAULES AMB DADES PER DEFECTE SI NO EXISTEIXEN.
     * Garanteix que els tipus de caselles (NORMAL, OS, FORAT, etc.) estiguin presents
     * per al correcte funcionament de la lògica de joc.
     */
    private void inicializarTablasMaestras() {
        if (conexion != null) {
            // Inserció de tipus de caselles bàsiques
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (1, 'NORMAL', 'SENSE EFECTE')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (2, 'OS', 'RETORNA A L''INICI')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (3, 'FORAT', 'RETROCEDEIX')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (4, 'TRINEU', 'AVANÇA')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (5, 'INTERROGANT', 'EVENT ALEATORI')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (6, 'SUELOQUEBRADIZO', 'ES TRENCA AL PASSAR')");

            // Verificació d'existència del taulell estàndard (ID=1)
            ArrayList<LinkedHashMap<String, String>> resTau = select(conexion, "SELECT COUNT(*) as TOTAL FROM taulell WHERE id_taulell = 1");
            if (resTau.isEmpty() || Integer.parseInt(resTau.get(0).get("TOTAL")) == 0) {
                ejecutar(conexion, "INSERT INTO taulell (id_taulell, mida_taulell) VALUES (1, 50)");
            }
            
            // ELIMINAR COLUMNA COLOR_JUGADOR SI EXISTEIX (TASCA DE NETEJA D'ESQUEMA)
            ArrayList<LinkedHashMap<String, String>> colCheck = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='COLOR_JUGADOR'");
            if (!colCheck.isEmpty()) {
                ejecutar(conexion, "ALTER TABLE jugador DROP COLUMN color_jugador");
            }

            // REGISTRE DE BOTS PER A PERSISTÈNCIA (SI NO EXISTEIXEN)
            // S'assignen IDs a partir de 991 per no col·lidir amb els jugadors humans.
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
     * @param text Text en clar que es vol encriptar.
     * @return El hash hexadecimal resultant de 64 caràcters.
     */
    private String sha256(String text) {
        String res = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                // Formatem cada byte a dos dígits hexadecimals
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
     * REGISTRA UN NOU JUGADOR HUMÀ SI NO EXISTEIX EL NOM D'USUARI.
     * @param username Nom del jugador.
     * @param password Contrasenya en clar (serà encriptada).
     * @return true si s'ha creat correctament, false si ja existia o hi ha hagut error.
     */
    public boolean registrarUsuario(String username, String password) {
        boolean ok = false;
        if (conexion != null) {
            // Primer comprovem que l'usuari no estigui ja registrat
            ArrayList<LinkedHashMap<String, String>> exist = select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
            if (exist.isEmpty()) {
                String hashPw = sha256(password);
                // Calculem el següent ID disponible (autoincrement manual si no hi ha seqüència activa)
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
     * Útil quan s'actualitza el joc per afegir funcionalitats de Login.
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
     * @param username Nom a cercar.
     * @return ID numèric o -1 si no es troba.
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
     * @param username Nom d'usuari.
     * @param password Contrasenya en clar.
     * @return true si coincideix el nom i el hash de la contrasenya.
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
     * Utilitza la sentència MERGE d'Oracle per fer un "Update or Insert" de forma atòmica.
     * @param p L'objecte partida amb tot el seu estat.
     * @return true si s'ha guardat tot correctament.
     */
    public boolean guardarBBDD(Partida p) {
        boolean ok = false;
        if (conexion != null && p != null) {
            try {
                int idPartida = p.getId();
                // Si la partida és nova, obtenim un ID de la seqüència
                if (idPartida <= 0) {
                    ArrayList<LinkedHashMap<String, String>> resSeq = select(conexion, "SELECT SEC_ID_PARTIDA.NEXTVAL AS NEXT_ID FROM dual");
                    if (!resSeq.isEmpty() && resSeq.get(0).get("NEXT_ID") != null) {
                        idPartida = Integer.parseInt(resSeq.get(0).get("NEXT_ID"));
                        p.setId(idPartida);
                    } else {
                        // Fallback si la seqüència no existeix
                        ArrayList<LinkedHashMap<String, String>> resMax = select(conexion, "SELECT MAX(id_partida) as MAX_ID FROM partida");
                        idPartida = 1;
                        if (!resMax.isEmpty() && resMax.get(0).get("MAX_ID") != null) idPartida = Integer.parseInt(resMax.get(0).get("MAX_ID")) + 1;
                        p.setId(idPartida);
                    }
                }

                // Guardat o actualització de la capçalera de la partida
                String sqlP = "MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                               "WHEN MATCHED THEN UPDATE SET torn_actual = " + (p.getJugadorActual() + 1) + ", nom_partida = '" + p.getNombre() + "', " +
                               "finalitzada = " + (p.isFinalizada() ? 1 : 0) + " " +
                               "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual, finalitzada) " +
                               "VALUES (" + idPartida + ", 1, '" + p.getNombre() + "', SYSDATE, " + (p.getJugadorActual() + 1) + ", " + (p.isFinalizada() ? 1 : 0) + ")";
                ejecutar(conexion, sqlP);

                // Guardat de les posicions individuals dels jugadors a la taula intermedia
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

                // ACTUALITZAR LES VICTÒRIES DEL GUANYADOR SI LA PARTIDA ACABA D'ACABAR
                if (p.isFinalizada() && p.getGanador() != null) {
                    int idG = getIDJugador(p.getGanador().getNombre());
                    if (idG != -1) {
                        ejecutar(conexion, "UPDATE jugador SET victories = victories + 1 WHERE id_jugador = " + idG);
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
     * CARREGA UNA PARTIDA COMPLETAMENT DES DE LA BASE DE DADES.
     * Reconstrueix l'objecte Partida, incloent els jugadors i les seves posicions.
     * @param id Identificador de la partida a carregar.
     * @return Objecte Partida inicialitzat.
     */
    public Partida cargarBBDD(int id) {
        Partida p = new Partida();
        p.setId(id);
        if (conexion != null) {
            // Carreguem les dades bàsiques de la partida
            ArrayList<LinkedHashMap<String, String>> resP = select(conexion, "SELECT * FROM partida WHERE id_partida = " + id);
            if (!resP.isEmpty()) {
                p.setNombre(resP.get(0).get("NOM_PARTIDA"));
                int tornActual = Integer.parseInt(resP.get(0).get("TORN_ACTUAL"));
                p.setJugadorActual(tornActual - 1); // Passem de format Oracle (1-4) a Java (0-3)
                
                String finStr = resP.get(0).get("FINALITZADA");
                p.setFinalizada("1".equals(finStr) || "SÍ".equalsIgnoreCase(finStr));

                // Carreguem els jugadors que participen en aquesta partida
                ArrayList<LinkedHashMap<String, String>> resJ = select(conexion, 
                    "SELECT j.nom_jugador, jp.posicio_actual FROM jugador j " +
                    "JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador WHERE jp.id_partida = " + id);
                
                for (LinkedHashMap<String, String> row : resJ) {
                    String nom = row.get("NOM_JUGADOR");
                    int pos = Integer.parseInt(row.get("POSICIO_ACTUAL"));
                    
                    Jugador j;
                    // Identifiquem el tipus de jugador pel nom per instanciar la classe correcta
                    if (nom.toUpperCase().contains("FOCA")) {
                        j = new Foca();
                    } else {
                        Pinguino pin = new Pinguino(nom, "BLAU");
                        String nUpper = nom.toUpperCase();
                        // Detectem si és una IA si el nom conté paraules clau
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
     * @return Una llista on cada mapa inclou dades de la partida i els noms dels jugadors concatenats.
     */
    public ArrayList<LinkedHashMap<String, String>> getListaPartidasDetalladas() {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, 
            "SELECT id_partida, nom_partida, TO_CHAR(data_creacio, 'DD/MM/YYYY') as DATA_CREACIO, torn_actual, finalitzada FROM partida ORDER BY id_partida DESC");
        
        for (LinkedHashMap<String, String> row : res) {
            int idP = Integer.parseInt(row.get("ID_PARTIDA"));
            // Obtenim els jugadors per a cada partida per mostrar-los a la taula
            ArrayList<LinkedHashMap<String, String>> jugs = select(conexion, 
                "SELECT j.nom_jugador FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador WHERE jp.id_partida = " + idP);
            
            StringBuilder sb = new StringBuilder();
            for (LinkedHashMap<String, String> jRow : jugs) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(jRow.get("NOM_JUGADOR"));
            }
            row.put("JUGADORS", sb.toString());
            // Transformem l'estat numèric a text per a l'usuari
            row.put("FINALITZADA", "1".equals(row.get("FINALITZADA")) ? "SÍ" : "NO");
        }
        return res;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MÈTODES D'ESTADÍSTIQUES — CRIDEN FUNCIONS I PROCEDIMENTS PL/SQL
    // Aquesta secció compleix els requisits avançats d'utilització de CallableStatement.
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
     * @return La mitjana de victòries de tota la base de dades.
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
     * @return El número de victòries del jugador que més en té.
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
     * @return Rànquing basat en la quantitat de partides jugades (històric).
     */
    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesTotalsSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall("{call RANKING_PARTIDES_TOTALS(?)}")) {
                // El paràmetre 1 és un SYS_REFCURSOR (específic d'Oracle)
                cs.registerOutParameter(1, -10); // -10 és el codi d'OracleTypes.CURSOR
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
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL RANKING_PARTIDES_TOTALS: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * CRIDA EL PROCEDIMENT PL/SQL GET_JUGADORS_RECORD.
     * Filtra els jugadors que han assolit el màxim de victòries actual.
     */
    public ArrayList<LinkedHashMap<String, String>> getJugadorsRecordSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion != null) {
            int record = getMaxVictoriesRecordSQL(); 
            try (CallableStatement cs = conexion.prepareCall("{call GET_JUGADORS_RECORD(?, ?)}")) {
                cs.setInt(1, record);                
                cs.registerOutParameter(2, -10);     
                cs.execute();
                ResultSet rs = (ResultSet) cs.getObject(2);
                if (rs != null) {
                    while (rs.next()) {
                        LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                        fila.put("NOM_JUGADOR", rs.getString("NOM_JUGADOR"));
                        fila.put("VICTORIES", String.valueOf(rs.getInt("VICTORIES")));
                        resultados.add(fila);
                    }
                    rs.close();
                }
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL GET_JUGADORS_RECORD: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * CRIDA EL PROCEDIMENT PL/SQL GET_JUGADORS_SOBRE_MITJA.
     * Retorna els jugadors que superen el rendiment mitjà de la comunitat.
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
            } catch (SQLException e) {
                System.out.println("ERROR PL/SQL GET_JUGADORS_SOBRE_MITJA: " + e.getMessage());
            }
        }
        return resultados;
    }

    /**
     * CRIDA LA FUNCIÓ PL/SQL PERCENTATGE_MENYS_VICTORIES.
     * Calcula la teva posició relativa (percentil) en comparació amb altres jugadors.
     * @param vics Número de victòries a comparar.
     * @return Percentatge de jugadors amb menys victòries que el paràmetre.
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
     * Obté un resum detallat de les mètriques d'un jugador pel seu nom.
     * Inclou la gestió d'excepcions personalitzades de PL/SQL (RAISE_APPLICATION_ERROR).
     * @param nom Nom del jugador a consultar.
     * @return Mapa amb VICTORIES, TOTAL_PARTIDES i POSICIO_RANKING.
     */
    public LinkedHashMap<String, String> consultarEstadistiquesJugador(String nom) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        if (conexion != null) {
            try (CallableStatement cs = conexion.prepareCall(
                    "{call CONSULTAR_ESTADISTIQUES_JUGADOR(?, ?, ?, ?)}")) {
                cs.setString(1, nom);                                       
                cs.registerOutParameter(2, java.sql.Types.NUMERIC);         
                cs.registerOutParameter(3, java.sql.Types.NUMERIC);         
                cs.registerOutParameter(4, java.sql.Types.NUMERIC);         
                cs.execute();
                result.put("VICTORIES", String.valueOf(cs.getInt(2)));
                result.put("TOTAL_PARTIDES", String.valueOf(cs.getInt(3)));
                result.put("POSICIO_RANKING", String.valueOf(cs.getInt(4)));
            } catch (SQLException e) {
                String msg = e.getMessage();
                // Gestionem els codis d'error definits al servidor (20001 i 20002)
                if (msg != null && msg.contains("20001")) {
                    result.put("ERROR", "El jugador '" + nom + "' no existeix.");
                } else if (msg != null && msg.contains("20002")) {
                    result.put("ERROR", "El jugador '" + nom + "' no té dades.");
                } else {
                    result.put("ERROR", "Error: " + msg);
                }
            }
        } else {
            result.put("ERROR", "SENSE CONNEXIÓ A LA BBDD");
        }
        return result;
    }
}
