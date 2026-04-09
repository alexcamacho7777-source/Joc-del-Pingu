package controlador;

import model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Gestiona la connexió i les operacions BBDD del joc.
 * Segueix el patró del professor:
 *   select / insert / update / delete / print / cerrar
 *   procesamientoSelect / procesarValor
 */
public class GestorBBDD {

    // ── Credencials per defecte ───────────────────────────────────────────────
    private static final String URL_DEFAULT  = "jdbc:oracle:thin:@//192.168.26.3:1521/XEPDB1";
    private static final String USER_DEFAULT = "DW2526_GR03_PINGU";
    private static final String PASS_DEFAULT = "AACGFAM";

    private String urlBBDD;
    private String username;
    private String password;

    // Constructor per defecte
    public GestorBBDD() {
        this.urlBBDD  = URL_DEFAULT;
        this.username = USER_DEFAULT;
        this.password = PASS_DEFAULT;
    }

    // Constructor personalitzat
    public GestorBBDD(String urlBBDD, String username, String password) {
        this.urlBBDD  = urlBBDD;
        this.username = username;
        this.password = password;
    }

    // Getters / Setters
    public String getUrlBBDD()           { return urlBBDD;   }
    public void   setUrlBBDD(String u)   { this.urlBBDD  = u; }
    public String getUsername()          { return username;  }
    public void   setUsername(String u)  { this.username = u; }
    public String getPassword()          { return password;  }
    public void   setPassword(String p)  { this.password = p; }

    // ── Connexió ──────────────────────────────────────────────────────────────

    /** Obre i retorna una connexió nova a la BBDD. */
    public Connection getConexion() throws SQLException {
        return DriverManager.getConnection(urlBBDD, username, password);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MÈTODES GENÈRICS (patró del professor)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Tanca la connexió de forma segura.
     */
    public void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.err.println("Error al tancar connexió: " + e.getMessage());
            }
        }
    }

    /**
     * Imprimeix per consola els resultats d'un SELECT.
     *
     * @param con      Connexió activa
     * @param sql      Sentència SELECT
     * @param columnes Noms de les columnes a mostrar
     */
    public void print(Connection con, String sql, String[] columnes) {
        ArrayList<LinkedHashMap<String, String>> files = select(con, sql);
        if (files.isEmpty()) {
            System.out.println("(sense resultats)");
            return;
        }
        for (LinkedHashMap<String, String> fila : files) {
            StringBuilder sb = new StringBuilder();
            for (String col : columnes) {
                sb.append(col).append(": ").append(fila.get(col)).append("  |  ");
            }
            System.out.println(sb.toString());
        }
    }

    /**
     * Executa una sentència INSERT.
     *
     * @param con Connexió activa
     * @param sql Sentència INSERT
     */
    public void insert(Connection con, String sql) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error INSERT: " + e.getMessage());
        }
    }

    /**
     * Executa una sentència UPDATE.
     *
     * @param con Connexió activa
     * @param sql Sentència UPDATE
     */
    public void update(Connection con, String sql) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error UPDATE: " + e.getMessage());
        }
    }

    /**
     * Executa una sentència DELETE.
     *
     * @param con Connexió activa
     * @param sql Sentència DELETE
     */
    public void delete(Connection con, String sql) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error DELETE: " + e.getMessage());
        }
    }

    /**
     * Executa un SELECT i retorna les files com a llista de mapes columna→valor.
     *
     * @param con Connexió activa
     * @param sql Sentència SELECT
     * @return Llista de files; cada fila és un LinkedHashMap (columna → valor en String)
     */
    public ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {
        ArrayList<LinkedHashMap<String, String>> files = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int numCols = meta.getColumnCount();

            while (rs.next()) {
                LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= numCols; i++) {
                    fila.put(meta.getColumnName(i), rs.getString(i));
                }
                files.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("Error SELECT: " + e.getMessage());
        }
        return files;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PROCESSAMENT DE RESULTATS (patró del professor)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Processa les files d'un SELECT i crida procesarValor() per a cada cel·la.
     * Segueix exactament el mateix patró que el professor a menu.java.
     *
     * @param con      Connexió activa
     * @param sql      Sentència SELECT
     * @param columnes Columnes que volem recuperar i processar
     */
    public void procesamientoSelect(Connection con, String sql, ArrayList<String> columnes) {
        ArrayList<LinkedHashMap<String, String>> filas = select(con, sql);

        if (filas.isEmpty()) {
            System.out.println("No s'ha trobat cap resultat.");
        } else {
            for (LinkedHashMap<String, String> fila : filas) {
                for (String col : columnes) {
                    String valor = fila.get(col);
                    if (valor == null) {
                        System.out.println("Avís: la columna '" + col + "' no existeix o no té valor.");
                    } else {
                        procesarValor(col, valor);
                    }
                }
            }
        }
    }

    /**
     * Processa un valor recuperat del SELECT i el mostra per consola.
     * Adaptat a les columnes de les taules del joc: PARTIDAS i JUGADORES.
     *
     * @param col   Nom de la columna
     * @param valor Valor de la columna (sempre arriba com a String des de la BBDD)
     */
    public void procesarValor(String col, String valor) {
        switch (col.toUpperCase()) {
            case "ID":
                System.out.println("ID partida:       " + valor);
                break;
            case "TURNOS":
                int tornos = Integer.parseInt(valor);
                System.out.println("Torns jugats:     " + tornos);
                break;
            case "JUGADOR_ACTUAL":
                int jugAct = Integer.parseInt(valor);
                System.out.println("Jugador actual:   " + jugAct);
                break;
            case "FINALIZADA":
                boolean fin = valor.equals("1");
                System.out.println("Finalitzada:      " + fin);
                break;
            case "NOMBRE":
                System.out.println("Nom jugador:      " + valor);
                break;
            case "COLOR":
                System.out.println("Color:            " + valor);
                break;
            case "POSICION":
                int pos = Integer.parseInt(valor);
                System.out.println("Posició:          " + pos);
                break;
            case "PARTIDA_ID":
                System.out.println("Partida ID:       " + valor);
                break;
            default:
                System.out.println(col + ": " + valor);
                break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MÈTODES ESPECÍFICS DEL JOC
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Guarda (o actualitza via MERGE) una partida i els seus jugadors a la BBDD.
     *
     * @param p Partida a guardar
     */
    public void guardarBBDD(Partida p) {
        Connection con = null;
        try {
            con = getConexion();

            // MERGE de la partida (id=1 fix per ara)
            String sqlPartida =
                "MERGE INTO partidas dst " +
                "USING (SELECT 1 FROM dual) src ON (dst.id = 1) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET turnos = ?, jugador_actual = ?, finalizada = ? " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (turnos, jugador_actual, finalizada) VALUES (?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sqlPartida)) {
                ps.setInt(1, p.getTurnos());
                ps.setInt(2, p.getJugadorActual());
                ps.setInt(3, p.isFinalizada() ? 1 : 0);
                ps.setInt(4, p.getTurnos());
                ps.setInt(5, p.getJugadorActual());
                ps.setInt(6, p.isFinalizada() ? 1 : 0);
                ps.executeUpdate();
            }

            // MERGE de cada jugador
            String sqlJugador =
                "MERGE INTO jugadores dst " +
                "USING (SELECT ? AS nombre FROM dual) src " +
                "  ON (dst.nombre = src.nombre AND dst.partida_id = 1) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET posicion = ? " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (nombre, color, posicion, partida_id) VALUES (?, ?, ?, 1)";

            for (Jugador j : p.getJugadores()) {
                try (PreparedStatement ps = con.prepareStatement(sqlJugador)) {
                    ps.setString(1, j.getNombre());
                    ps.setInt   (2, j.getPosicion());
                    ps.setString(3, j.getNombre());
                    ps.setString(4, j.getColor());
                    ps.setInt   (5, j.getPosicion());
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
        } finally {
            cerrar(con);
        }
    }

    /**
     * Carrega una partida des de la BBDD a partir del seu ID.
     * Utilitza select() i procesarValor() seguint el patró del professor.
     *
     * @param id Identificador de la partida
     * @return Partida carregada, o una Partida buida si no es troba
     */
    public Partida cargarBBDD(int id) {
        Partida partida = new Partida();
        Connection con = null;

        try {
            con = getConexion();

            // ── Dades de la partida ──────────────────────────────────────────
            ArrayList<String> colsPartida = new ArrayList<>();
            colsPartida.add("TURNOS");
            colsPartida.add("JUGADOR_ACTUAL");
            colsPartida.add("FINALIZADA");

            ArrayList<LinkedHashMap<String, String>> filesPartida =
                select(con, "SELECT * FROM partidas WHERE id = " + id);

            if (filesPartida.isEmpty()) {
                System.out.println("No s'ha trobat la partida amb id=" + id);
                return partida;
            }

            LinkedHashMap<String, String> filaPartida = filesPartida.get(0);
            partida.setTurnos       (Integer.parseInt(filaPartida.get("TURNOS")));
            partida.setJugadorActual(Integer.parseInt(filaPartida.get("JUGADOR_ACTUAL")));
            partida.setFinalizada   ("1".equals(filaPartida.get("FINALIZADA")));

            // Mostrar dades per consola (patró professor)
            System.out.println("── Partida carregada ──");
            for (String col : colsPartida) {
                procesarValor(col, filaPartida.get(col));
            }

            // ── Jugadors de la partida ───────────────────────────────────────
            ArrayList<String> colsJugador = new ArrayList<>();
            colsJugador.add("NOMBRE");
            colsJugador.add("COLOR");
            colsJugador.add("POSICION");

            ArrayList<LinkedHashMap<String, String>> filesJugadors =
                select(con, "SELECT * FROM jugadores WHERE partida_id = " + id);

            System.out.println("── Jugadors ──");
            for (LinkedHashMap<String, String> fila : filesJugadors) {
                String nom    = fila.get("NOMBRE");
                String color  = fila.get("COLOR");
                int    posicio = Integer.parseInt(fila.get("POSICION"));

                Pinguino pg = new Pinguino(nom, color);
                pg.setPosicion(posicio);
                partida.anadirJugador(pg);

                // Mostrar per consola (patró professor)
                for (String col : colsJugador) {
                    procesarValor(col, fila.get(col));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al carregar partida: " + e.getMessage());
        } finally {
            cerrar(con);
        }

        return partida;
    }
}
