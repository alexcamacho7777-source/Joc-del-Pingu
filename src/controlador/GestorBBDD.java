package controlador;

import model.*;

import java.security.MessageDigest;
import java.sql.*;
import java.sql.CallableStatement;
import java.sql.Types;
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
        // INTENT DE CONNEXIÓ LOCAL
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
        
        // SI LA CONNEXIÓ LOCAL FALLA, S'INTENTA LA REMOTA PER INTERNET
        if (this.conexion == null) {
            System.out.println("INTENTANT CONNEXIÓ REMOTA...");
            this.conexion = conectarDirecte(URL_REMOTO, USER_PROJ, PASS_PROJ);
        }
        
        // SI TENIM CONNEXIÓ, VERIFIQUEM QUE L'ESTRUCTURA DE TAULES SIGUI CORRECTA
        if (this.conexion != null) {
            assegurarEstructuraPLSQL();
            inicializarTablasMaestras();
        }
    }

    /**
     * RETORNA L'OBJECTE DE CONNEXIÓ ACTUAL.
     */
    public Connection getConexion() { return conexion; }

    /**
     * MÈTODE INTERN PER ESTABLIR LA CONNEXIÓ JDBC AMB EL DRIVER D'ORACLE.
     */
    private static Connection conectarDirecte(String url, String user, String pwd) {
        try {
            // CARREGUEM EL DRIVER DE LA BASE DE DADES
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (ClassNotFoundException e) {
                Class.forName("oracle.jdbc.driver.OracleDriver");
            }
            // DEFINIM UN TEMPS MÀXIM D'ESPERA PER A L'INICI DE SESSIÓ
            DriverManager.setLoginTimeout(5); 
            Connection con = DriverManager.getConnection(url, user, pwd);
            if (con != null && con.isValid(5)) {
                System.out.println("CONNECTAT CORRECTAMENT A: " + url);
            }
            return con;
        } catch (Exception e) {
            System.err.println("ERROR EN LA CONNEXIÓ: " + e.getMessage());
        }
        return null;
    }

    /**
     * TANCA LA CONNEXIÓ AMB LA BASE DE DADES DE FORMA SEGURA.
     */
    public static void cerrar(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    /**
     * EXECUTA UNA SENTÈNCIA SQL DE TIPUS INSERT, UPDATE O DELETE.
     */
    public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
        if (con == null) return 0;
        try (Statement st = con.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("ERROR EN " + etiqueta + ": " + e.getMessage());
            return 0;
        }
    }

    /**
     * EXECUTA UNA CONSULTA SELECT I RETORNA ELS RESULTATS EN UNA LLISTA DE MAPES.
     * CADA MAPA REPRESENTA UNA FILA ON LA CLAU ÉS EL NOM DE LA COLUMNA.
     */
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
            System.out.println("ERROR EN SELECT: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * EXECUTA UNA SENTÈNCIA SQL SENSE RETORNAR RESULTATS, GESTIONANT ERRORS COMUNS.
     */
    private boolean ejecutar(Connection con, String sql) {
        if (con == null) return false;
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            // IGNORAREM ELS ERRORS DE CLAU DUPLICADA (ORA-00001) DURANT LA CÀRREGA INICIAL
            if (!e.getMessage().contains("ORA-00001")) {
                System.out.println("ERROR SQL: " + e.getMessage());
            }
            return false;
        }
    }

    /**
     * REALITZA UN COMMIT PER CONFIRMAR ELS CANVIS A LA BASE DE DADES.
     */
    private void commit(Connection con) {
        try { if (con != null) con.commit(); } catch (SQLException ignored) {}
    }

    /**
     * INICIALITZA LES TAULES MESTRES (TIPUS DE CASELLA I TAULELL) AMB DADES PER DEFECTE.
     * AIXÒ ASSEGURA QUE EL JOC TINGUI ELS COMPONENTS BÀSICS EN UNA BASE DE DADES BUIDA.
     */
    private void inicializarTablasMaestras() {
        if (conexion != null) {
            // INSERCIÓ DELS 6 TIPUS DE CASELLES DEL JOC
            System.out.println("VERIFICANT TAULES MESTRES...");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (1, 'Normal', 'SENSE EFECTE')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (2, 'Os', 'RETORNA A L''INICI')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (3, 'Forat', 'RETROCEDEIX')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (4, 'Trineu', 'AVANÇA')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (5, 'Interrogant', 'EVENT ALEATORI')");
            ejecutar(conexion, "INSERT INTO tipus_casella (id_tipus, nom_tipus, descripcio) VALUES (6, 'SueloQuebradizo', 'ES TRENCA AL PASSAR')");

            // CREACIÓ DEL TAULELL ESTÀNDARD DE 50 CASELLES
            ArrayList<LinkedHashMap<String, String>> resTau = select(conexion, "SELECT COUNT(*) as TOTAL FROM taulell WHERE id_taulell = 1");
            if (resTau.isEmpty() || Integer.parseInt(resTau.get(0).get("TOTAL")) == 0) {
                ejecutar(conexion, "INSERT INTO taulell (id_taulell, mida_taulell) VALUES (1, 50)");
            }

            // CREACIÓ DE LES CASELLES INDIVIDUALS SI NO EXISTEIXEN
            ArrayList<LinkedHashMap<String, String>> resCas = select(conexion, "SELECT COUNT(*) as TOTAL FROM casella WHERE id_taulell = 1");
            if (resCas.isEmpty() || Integer.parseInt(resCas.get(0).get("TOTAL")) == 0) {
                for (int i = 1; i <= 50; i++) {
                    int tipus = 1; 
                    if (i == 4 || i == 15 || i == 30) tipus = 2; 
                    if (i == 10 || i == 25 || i == 40) tipus = 3; 
                    if (i == 5 || i == 20 || i == 35) tipus = 4; 
                    if (i % 7 == 0) tipus = 5; 
                    ejecutar(conexion, "INSERT INTO casella (id_casella, id_taulell, id_tipus, numero_casella) VALUES (" + i + ", 1, " + tipus + ", " + i + ")");
                }
            }
            commit(conexion);
        }
    }

    /**
     * GENERA UN HASH SHA-256 PER ENCRIPTAR LES CONTRASENYES DELS USUARIS.
     */
    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("ERROR CALCULANT EL HASH", e);
        }
    }

    /**
     * REGISTRA UN NOU JUGADOR HUMÀ AMB SEGURETAT DE CONTRASENYA.
     */
    public boolean registrarUsuario(String username, String password) {
        if (conexion == null) return false;
        // VERIFIQUEM QUE L'USUARI NO EXISTEIXI PRÈVIAMENT
        if (!select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) return false;

        String hashPw = sha256(password);
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = 1;
        if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;

        String sql = "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories, contrasenya) VALUES ("
                + nextId + ", '" + username + "', 'Blau', 0, '" + hashPw + "')";
        return executeInsUpDel(conexion, sql, "REGISTRE") > 0;
    }

    /**
     * ASSEGURA QUE L'ESTRUCTURA DE LES TAULES SIGUI COMPATIBLE AMB LES NOVES FUNCIONALITATS.
     * AFEGEIX COLUMNES DE CONTRASENYA, VICTORIES I ESTAT DE PARTIDA SI NO EXISTEIXEN.
     */
    private void assegurarEstructuraPLSQL() {
        if (conexion != null) {
            // ACTUALITZACIÓ DE LA TAULA JUGADOR
            ArrayList<LinkedHashMap<String, String>> colsJ = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='CONTRASENYA'");
            if (colsJ.isEmpty()) ejecutar(conexion, "ALTER TABLE jugador ADD (contrasenya VARCHAR2(64))");
        
            colsJ = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='JUGADOR' AND column_name='VICTORIES'");
            if (colsJ.isEmpty()) ejecutar(conexion, "ALTER TABLE jugador ADD (victories NUMBER DEFAULT 0)");

            // ACTUALITZACIÓ DE LA TAULA PARTIDA
            ArrayList<LinkedHashMap<String, String>> colsP = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='PARTIDA' AND column_name='FINALITZADA'");
            if (colsP.isEmpty()) ejecutar(conexion, "ALTER TABLE partida ADD (finalitzada NUMBER(1) DEFAULT 0)");

            ArrayList<LinkedHashMap<String, String>> colsP2 = select(conexion, "SELECT column_name FROM user_tab_columns WHERE table_name='PARTIDA' AND column_name='ID_GUANYADOR'");
            if (colsP2.isEmpty()) ejecutar(conexion, "ALTER TABLE partida ADD (id_guanyador NUMBER)");

            // ACTUALITZACIÓ DE LA TAULA CASELLA
            ejecutar(conexion, "ALTER TABLE casella MODIFY (id_casella NUMBER(10))");

            System.out.println("ESTRUCTURA DE TAULES VERIFICADA CORRECTAMENT.");
            commit(conexion);
        }
    }

    /**
     * OBTÉ L'ID INTERN D'UN JUGADOR PEL SEU NOM D'USUARI.
     */
    public int getIDJugador(String username) {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT id_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
        if (res.isEmpty() || res.get(0).get("ID_JUGADOR") == null) return -1;
        return Integer.parseInt(res.get(0).get("ID_JUGADOR"));
    }

    /**
     * VALIDA LES CREDENCIALS D'UN USUARI EN EL MOMENT DEL LOGIN.
     */
    public boolean loginUsuario(String username, String password) {
        if (conexion == null) return false;
        String hashPw = sha256(password);
        ArrayList<LinkedHashMap<String, String>> res = select(conexion,
                "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "' AND contrasenya = '" + hashPw + "'");
        return !res.isEmpty();
    }

    /**
     * REGISTRA UN JUGADOR QUE NO ÉS HUMÀ (CPU) SENSE CONTRASENYA.
     */
    private void registrarJugadorSenseContrasenya(String username) {
        if (conexion != null) {
            if (select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) {
                ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
                int nextId = 1;
                if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
                executeInsUpDel(conexion, "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories) VALUES ("
                        + nextId + ", '" + username + "', 'Gris', 0)", "REGISTRE IA");
            }
        }
    }

    /**
     * GUARDA L'ESTAT COMPLET D'UNA PARTIDA A LA BASE DE DADES.
     * UTILITZA LA SENTÈNCIA MERGE PER ACTUALITZAR O INSERIR SEGONS SI JA EXISTEIX.
     */
    public boolean guardarBBDD(Partida p) {
        if (conexion == null) return false;
        try {
            java.util.List<Jugador> jugadores = p.getJugadores();
            for (Jugador j : jugadores) {
                if (getIDJugador(j.getNombre()) == -1) registrarJugadorSenseContrasenya(j.getNombre());
            }

            int idPartida = p.getId();
            if (idPartida <= 0) {
                // OBTENIM EL SEGÜENT ID DE LA SEQÜÈNCIA SQL
                ArrayList<LinkedHashMap<String, String>> resSeq = select(conexion, "SELECT SEC_ID_PARTIDA.NEXTVAL AS NEXT_ID FROM dual");
                if (!resSeq.isEmpty() && resSeq.get(0).get("NEXT_ID") != null) {
                    idPartida = Integer.parseInt(resSeq.get(0).get("NEXT_ID"));
                } else {
                    ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_partida) as MAX_ID FROM partida");
                    idPartida = 1;
                    if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) idPartida = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
                }
                p.setId(idPartida);
            }

            int numTorn = p.getJugadorActual() + 1;
            
            // ACTUALITZACIÓ DEL TAULELL
            if (p.getTablero() != null) {
                int mida = p.getTablero().getTotalCasillas();
                String sqlMergeTau = "MERGE INTO taulell dst USING (SELECT " + idPartida + " AS id_t, " + mida + " AS v_mida FROM dual) src " +
                                     "ON (dst.id_taulell = src.id_t) " +
                                     "WHEN NOT MATCHED THEN INSERT (id_taulell, mida_taulell) VALUES (src.id_t, src.v_mida)";
                ejecutar(conexion, sqlMergeTau);
            }

            int idGuanyador = (p.isFinalizada() && p.getGanador() != null) ? getIDJugador(p.getGanador().getNombre()) : -1;
            String valFinalitzada = p.isFinalizada() ? "1" : "0";
            String valGuanyador = (idGuanyador != -1) ? String.valueOf(idGuanyador) : "NULL";

            // ACTUALITZACIÓ DE LES DADES DE LA PARTIDA
            String sqlP = "MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                    "WHEN MATCHED THEN UPDATE SET torn_actual = " + numTorn + ", nom_partida = '" + p.getNombre() + "', " +
                    "finalitzada = " + valFinalitzada + ", id_guanyador = " + valGuanyador + " " +
                    "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual, finalitzada, id_guanyador) " +
                    "VALUES (" + idPartida + ", " + idPartida + ", '" + p.getNombre() + "', SYSDATE, " + numTorn + ", " + valFinalitzada + ", " + valGuanyador + ")";
            ejecutar(conexion, sqlP);
            
            // ACTUALITZACIÓ DE LES CASELLES
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

            // ACTUALITZACIÓ DE LA POSICIÓ I INVENTARI DELS JUGADORS
            for (int i = 0; i < jugadores.size(); i++) {
                Jugador j = jugadores.get(i);
                int idJ = getIDJugador(j.getNombre());
                if (idJ != -1) {
                    int d=0, pe=0, b=0;
                    if (j instanceof Pinguino pin) {
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
            }
            commit(conexion);
            System.out.println("PARTIDA GUARDADA AMB ÈXIT. ID=" + idPartida);
            return true;
        } catch (Exception ex) {
            System.err.println("ERROR EN EL GUARDAT: " + ex.getMessage());
            return false;
        }
    }

    /**
     * CARREGA TOTA LA INFORMACIÓ D'UNA PARTIDA DES DE LA BASE DE DADES.
     * RECONSTRUEIX EL TAULELL, ELS JUGADORS I ELS SEUS INVENTARIS.
     */
    public Partida cargarBBDD(int id_partida) {
        Partida p = new Partida();
        p.setId(id_partida);
        if (conexion == null) return p;

        ArrayList<LinkedHashMap<String, String>> resP = select(conexion, "SELECT * FROM partida WHERE id_partida = " + id_partida);
        if (resP.isEmpty()) return p;

        p.setNombre(resP.get(0).get("NOM_PARTIDA"));
        int ordreActual = resP.get(0).get("TORN_ACTUAL") != null ? Integer.parseInt(resP.get(0).get("TORN_ACTUAL")) : 1;

        // CÀRREGA DELS JUGADORS EN ORDRE DE TORN
        String sqlJ = "SELECT j.nom_jugador, j.color_jugador, jp.posicio_actual, jp.daus, jp.peixos, jp.boles_neu " +
                      "FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
                      "JOIN torn t ON (j.id_jugador = t.id_jugador AND jp.id_partida = t.id_partida) " +
                      "WHERE jp.id_partida = " + id_partida + " ORDER BY t.ordre ASC";

        ArrayList<LinkedHashMap<String, String>> filas = select(conexion, sqlJ);
        for (LinkedHashMap<String, String> f : filas) {
            String nom = f.get("NOM_JUGADOR");
            String col = f.get("COLOR_JUGADOR") != null ? f.get("COLOR_JUGADOR") : "Blau";
            int pos = f.get("POSICIO_ACTUAL") != null ? Integer.parseInt(f.get("POSICIO_ACTUAL")) : 0;

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
        int index = ordreActual - 1;
        if (index < 0 || index >= p.getJugadores().size()) index = 0;
        p.setJugadorActual(index);

        // RECONSTRUCCIÓ DEL TAULELL I LES SEVES CASELLES ESPECIALS
        int idTaulell = resP.get(0).get("ID_TAULELL") != null ? Integer.parseInt(resP.get(0).get("ID_TAULELL")) : 1;
        ArrayList<LinkedHashMap<String, String>> resC = select(conexion, "SELECT * FROM casella WHERE id_taulell = " + idTaulell + " ORDER BY numero_casella ASC");
        if (!resC.isEmpty()) {
            ArrayList<Casilla> casillas = new ArrayList<>();
            for (LinkedHashMap<String, String> rowC : resC) {
                int tipus = Integer.parseInt(rowC.get("ID_TIPUS"));
                int posC = Integer.parseInt(rowC.get("NUMERO_CASELLA"));
                Casilla c;
                switch (tipus) {
                    case 2: c = new Oso(posC); break;
                    case 3: c = new Agujero(posC); break;
                    case 4: c = new Trineo(posC); break;
                    case 5: c = new Evento(posC); break;
                    case 6: c = new SueloQuebradizo(posC); break;
                    default: c = new Normal(posC); break;
                }
                casillas.add(c);
            }
            Tablero tab = new Tablero();
            tab.setCasillas(casillas);
            p.setTablero(tab);
        }
        return p;
    }

    /**
     * OBTÉ LA LLISTA DE TOTES LES PARTIDES DISPONIBLES AMB INFORMACIÓ DETALLADA PER AL LOBBY.
     */
    public ArrayList<LinkedHashMap<String, String>> getListaPartidasDetalladas() {
        if (conexion == null) return new ArrayList<>();
        ArrayList<LinkedHashMap<String, String>> partides = select(conexion,
                "SELECT id_partida, nom_partida, TO_CHAR(data_creacio,'DD/MM/YYYY') AS data_creacio, " +
                "torn_actual, finalitzada FROM partida ORDER BY id_partida DESC");

        for (LinkedHashMap<String, String> fila : partides) {
            String idPartida = fila.get("ID_PARTIDA");
            ArrayList<LinkedHashMap<String, String>> jugadors = select(conexion,
                    "SELECT j.nom_jugador FROM jugador j JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
                    "WHERE jp.id_partida = " + idPartida + " ORDER BY jp.id_jugador ASC");
            StringBuilder sb = new StringBuilder();
            for (LinkedHashMap<String, String> jf : jugadors) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(jf.get("NOM_JUGADOR"));
            }
            fila.put("JUGADORS", sb.length() > 0 ? sb.toString() : "-");
            String fin = fila.get("FINALITZADA");
            fila.put("FINALITZADA", (fin != null && fin.equals("1")) ? "SÍ" : "NO");
        }
        return partides;
    }

    /**
     * CRIDA A LA FUNCIÓ PL/SQL GET_VICTORIES_JUGADOR PER OBTENIR EL COMPTADOR DE GUANYS.
     */
    public int getVictoriesSQL(int idJugador) {
        if (conexion == null) return 0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_VICTORIES_JUGADOR(?) }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setInt(2, idJugador);
            cs.execute();
            return cs.getInt(1);
        } catch (SQLException e) {
            System.err.println("ERROR CRIDANT GET_VICTORIES_JUGADOR: " + e.getMessage());
            return -1;
        }
    }

    /**
     * OBTÉ LA MITJA DE PUNTUACIÓ GLOBAL DELS JUGADORS (CRIDA PL/SQL).
     */
    public double getMitjaGlobalSQL() {
        if (conexion == null) return 0.0;
        try (CallableStatement cs = conexion.prepareCall("{ ? = call GET_MITJA_PUNTUACIO_GLOBAL }")) {
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.execute();
            return cs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("ERROR CRIDANT GET_MITJA_PUNTUACIO_GLOBAL: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * OBTÉ EL RÀNQUING DE JUGADORS SEGONS EL NÚMERO DE PARTIDES JUGADES.
     */
    public ArrayList<LinkedHashMap<String, String>> getRankingPartidesTotalsSQL() {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (conexion == null) return resultados;
        try (CallableStatement cs = conexion.prepareCall("{ call RANKING_PARTIDES_TOTALS(?) }")) {
            cs.registerOutParameter(1, -10); // ORACLE CURSOR
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
            System.err.println("ERROR CRIDANT RANKING_PARTIDES_TOTALS: " + e.getMessage());
        }
        return resultados;
    }
}
