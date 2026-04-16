package controlador;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * Gestiona la connexió i les operacions BBDD del joc.
 * Implementació final alineada amb l'Informe Tècnic Corregit.
 */
public class GestorBBDD {

    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String URL_FUERA  = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    private Connection conexion;

    public GestorBBDD() {
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
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
            System.out.println("No s'ha pogut connectar directament: " + e.getMessage());
        }
        return null;
    }

    public static Connection conectarBaseDatos(Scanner scan) {
        System.out.println("Iniciant connexió manual...");
        return conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
    }

    public static void cerrar(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    public static int insert(Connection con, String sql) { return executeInsUpDel(con, sql, "Insert"); }
    public static int update(Connection con, String sql) { return executeInsUpDel(con, sql, "Update"); }
    public static int delete(Connection con, String sql) { return executeInsUpDel(con, sql, "Delete"); }

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
            System.out.println("Error en SELECT: " + e.getMessage());
        }
        return resultados;
    }

    public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
        if (con == null) return 0;
        try (Statement st = con.createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            System.out.println("Error en " + etiqueta + " [" + (sql.length()>60?sql.substring(0,60):sql) + "]: " + e.getMessage());
            return 0;
        }
    }

    public static void procesamientoSelect(Connection con, String sql, ArrayList<String> columnas) {
        ArrayList<LinkedHashMap<String, String>> filas = select(con, sql);
        for (LinkedHashMap<String, String> fila : filas) {
            for (String col : columnas) {
                String valor = fila.get(col.toUpperCase());
                if (valor != null) procesarValor(col, valor);
            }
        }
    }

    public static void procesarValor(String col, String valor) {
        System.out.println(col.toUpperCase() + ": " + valor);
    }

    // ── MÈTODES ESPECÍFICS ───────────────────────────────────────────────────

    public boolean registrarUsuario(String username) {
        if (conexion == null) return false;
        if (!select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) return true;

        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = 1;
        if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) {
            nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
        }
        String sql = "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories) VALUES (" + nextId + ", '" + username + "', 'Blau', 0)";
        return insert(conexion, sql) > 0;
    }

    public int getIDJugador(String username) {
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT id_jugador FROM jugador WHERE nom_jugador = '" + username + "'");
        if (res.isEmpty() || res.get(0).get("ID_JUGADOR") == null) return -1;
        return Integer.parseInt(res.get(0).get("ID_JUGADOR"));
    }

    public boolean loginUsuario(String username) {
        return !select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty();
    }

    private boolean ejecutar(Connection con, String sql) {
        if (con == null) return false;
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.out.println("Error SQL: " + e.getMessage());
            return false;
        }
    }

    private void commit(Connection con) {
        try { if (con != null) con.commit(); } catch (SQLException ignored) {}
    }

    /**
     * Guarda la partida segons l'informe tècnic oficial.
     */
    public void guardarBBDD(Partida p) {
        if (conexion == null) return;

        try {
            // 1. Jugadors
            java.util.List<Jugador> jugadores = p.getJugadores();
            for (Jugador j : jugadores) {
                if (getIDJugador(j.getNombre()) == -1) registrarUsuario(j.getNombre());
            }

            // 2. Taulell
            ejecutar(conexion, "MERGE INTO taulell dst USING (SELECT 1 AS id_t FROM dual) src ON (dst.id_taulell = src.id_t) " +
                    "WHEN NOT MATCHED THEN INSERT (id_taulell, mida_taulell) VALUES (1, 50)");

            // 3. ID Partida
            int idPartida = p.getId();
            if (idPartida <= 0) {
                ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_partida) as MAX_ID FROM partida");
                idPartida = 1;
                if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) idPartida = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
                p.setId(idPartida);
            }

            // 4. Partida (torn_actual és l'ordre del jugador 1, 2, 3...)
            int numTorn = p.getJugadorActual() + 1;
            String sqlP = "MERGE INTO partida dst USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
                    "WHEN MATCHED THEN UPDATE SET torn_actual = " + numTorn + " " +
                    "WHEN NOT MATCHED THEN INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual) " +
                    "VALUES (" + idPartida + ", 1, 'Partida #" + idPartida + "', SYSDATE, " + numTorn + ")";
            ejecutar(conexion, sqlP);

            // 5. Jugador_Partida i Torn
            for (int i = 0; i < jugadores.size(); i++) {
                Jugador j = jugadores.get(i);
                int idJ = getIDJugador(j.getNombre());
                if (idJ == -1) continue;

                // Victories
                if (p.isFinalizada() && j.equals(p.getGanador())) {
                    ejecutar(conexion, "UPDATE jugador SET victories = victories + 1 WHERE id_jugador = " + idJ);
                }

                // Inventari
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
            System.out.println("Partida #" + idPartida + " guardada OK.");
        } catch (Exception ex) {
            System.out.println("Error guardar: " + ex.getMessage());
        }
    }

    /**
     * Carrega la partida segons l'informe tècnic oficial.
     */
    public Partida cargarBBDD(int id_partida) {
        Partida p = new Partida();
        p.setId(id_part_id_partida); if(true) p.setId(id_partida); // Fix simple
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
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT id_partida FROM partida ORDER BY id_partida DESC");
        for (LinkedHashMap<String, String> f : res) ids.add(Integer.parseInt(f.get("ID_PARTIDA")));
        return ids;
    }
}