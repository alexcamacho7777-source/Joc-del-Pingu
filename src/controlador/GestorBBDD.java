package controlador;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * Gestiona la connexió i les operacions BBDD del joc.
 * Conté els mètodes estàtics del professor + credencials pròpies del projecte
 * + mètodes específics del joc (guardarBBDD, cargarBBDD, procesamientoSelect).
 */
public class GestorBBDD {

    // ── Credencials hard-coded del projecte ───────────────────────────────────
    private static final String URL_CENTRO = "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2";
    private static final String URL_FUERA  = "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";
    private static final String USER_PROJ  = "DW2526_GR03_PINGU";
    private static final String PASS_PROJ  = "AACGFAM";

    // Connexió activa (instància)
    private Connection conexion;

    // ── Constructor (obre connexió automàticament amb credencials del projecte) ─
    public GestorBBDD() {
        this.conexion = conectarDirecte(URL_CENTRO, USER_PROJ, PASS_PROJ);
    }

    public Connection getConexion() { return conexion; }

    /** Connecta amb URL + credencials donades sense Scanner. */
    private static Connection conectarDirecte(String url, String user, String pwd) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, user, pwd);
            if (con != null && con.isValid(5)) {
                System.out.println("Connectat a la BBDD correctament.");
            }
            return con;
        } catch (ClassNotFoundException e) {
            System.out.println("Driver Oracle no trobat. Comprova que ojdbc està al Build Path.");
        } catch (SQLException e) {
            System.out.println("No s'ha pogut connectar: " + e.getMessage());
        }
        return null;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MÈTODES DEL PROFESSOR (estàtics, sense canvis)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Connecta a la BBDD Oracle demanant entorn i credencials per consola.
     */
    public static Connection conectarBaseDatos(Scanner scan) {
        System.out.println("Intentando conectarse a la base de datos...");

        String entorno = "";
        boolean valido = false;
        while (!valido) {
            System.out.println("Selecciona centro o fuera de centro (CENTRO/FUERA):");
            entorno = scan.nextLine().trim().toLowerCase();
            if (entorno.equalsIgnoreCase("centro") || entorno.equalsIgnoreCase("fuera")) {
                valido = true;
            } else {
                System.out.println("Entrada no válida. Escribe CENTRO o FUERA.");
            }
        }

        String url = entorno.equals("centro") ? URL_CENTRO : URL_FUERA;

        System.out.println("¿Usuario?");
        String user = scan.nextLine().trim();
        System.out.println("¿Contraseña?");
        String pwd = scan.nextLine();

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection(url, user, pwd);
            if (con.isValid(5)) {
                System.out.println("Conectados a la base de datos (" + entorno.toUpperCase() + ").");
            } else {
                System.out.println("Conexión creada, pero no parece válida. Revisa red/URL.");
            }
            return con;
        } catch (ClassNotFoundException e) {
            System.out.println("No se ha encontrado el driver de Oracle. ¿Está el ojdbc en el proyecto?");
        } catch (SQLException e) {
            System.out.println("No se pudo conectar. Revisa URL/usuario/contraseña.");
            System.out.println("Detalle: " + e.getMessage());
        }
        return null;
    }

    /** Tanca la connexió. */
    public static void cerrar(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException ignored) {}
        }
    }

    /** Executa un INSERT i retorna les files afectades. */
    public static int insert(Connection con, String sql) {
        return executeInsUpDel(con, sql, "Insert");
    }

    /** Executa un UPDATE i retorna les files afectades. */
    public static int update(Connection con, String sql) {
        return executeInsUpDel(con, sql, "Update");
    }

    /** Executa un DELETE i retorna les files afectades. */
    public static int delete(Connection con, String sql) {
        return executeInsUpDel(con, sql, "Delete");
    }

    /** Executa un SELECT i retorna llista de files (LinkedHashMap columna→valor). */
    public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {
        ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();
        if (con == null) {
            System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
            return resultados;
        }
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int numColumnas = meta.getColumnCount();
            while (rs.next()) {
                LinkedHashMap<String, String> fila = new LinkedHashMap<>();
                for (int i = 1; i <= numColumnas; i++) {
                    fila.put(meta.getColumnLabel(i), rs.getString(i));
                }
                resultados.add(fila);
            }
        } catch (SQLException e) {
            System.out.println("Error en SELECT: " + e.getMessage());
        }
        return resultados;
    }

    /** Imprimeix per consola les files d'un SELECT. */
    public static void print(Connection con, String sql, String[] listaElementosSeleccionados) {
        if (con == null) {
            System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
            return;
        }
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int fila = 0;
            boolean hayResultados = false;
            while (rs.next()) {
                hayResultados = true;
                fila++;
                System.out.println("---- Fila " + fila + " ----");
                for (String col : listaElementosSeleccionados) {
                    System.out.println(col + ": " + rs.getString(col));
                }
            }
            if (!hayResultados) System.out.println("No se ha encontrado nada");

        } catch (SQLException e) {
            System.out.println("Error en SELECT: " + e.getMessage());
        }
    }

    /** Mètode intern per a INSERT / UPDATE / DELETE. */
    public static int executeInsUpDel(Connection con, String sql, String etiqueta) {
        if (con == null) {
            System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
            return 0;
        }
        try (Statement st = con.createStatement()) {
            int filas = st.executeUpdate(sql);
            System.out.println(etiqueta + " hecho correctamente. Filas afectadas: " + filas);
            return filas;
        } catch (SQLException e) {
            System.out.println("Ha habido un error en " + etiqueta + ": " + e.getMessage());
            return 0;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PROCESSAMENT SELECT (patró del professor, adaptat al joc)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Processa les files d'un SELECT i crida procesarValor() per a cada cel·la.
     *
     * @param con      Connexió activa
     * @param sql      Sentència SELECT
     * @param columnas Columnes que volem processar
     */
    public static void procesamientoSelect(Connection con, String sql, ArrayList<String> columnas) {
        ArrayList<LinkedHashMap<String, String>> filas = select(con, sql);

        if (filas.isEmpty()) {
            System.out.println("No se ha encontrado nada");
        } else {
            for (LinkedHashMap<String, String> fila : filas) {
                for (String col : columnas) {
                    String valor = fila.get(col);
                    if (valor == null) {
                        System.out.println("Aviso: la columna '" + col + "' no existe en el SELECT o no tiene valor.");
                    } else {
                        procesarValor(col, valor);
                    }
                }
            }
        }
    }

    /**
     * Processa cada valor del SELECT. Adaptat a les taules del joc.
     * Aquí és on s'ha de treballar per al projecte.
     *
     * @param col   Nom de la columna
     * @param valor Valor (sempre arriba com a String)
     */
    public static void procesarValor(String col, String valor) {
        // Conversió de valors de les taules PARTIDAS i JUGADORES
        switch (col.toUpperCase()) {
            case "ID":
                System.out.println("ID partida:     " + valor);
                break;
            case "TURNOS":
                System.out.println("Torns jugats:   " + Integer.parseInt(valor));
                break;
            case "JUGADOR_ACTUAL":
                System.out.println("Jugador actual: " + Integer.parseInt(valor));
                break;
            case "FINALIZADA":
                System.out.println("Finalitzada:    " + ("1".equals(valor)));
                break;
            case "NOMBRE":
                System.out.println("Jugador:        " + valor);
                break;
            case "COLOR":
                System.out.println("Color:          " + valor);
                break;
            case "POSICION":
                System.out.println("Posició:        " + Integer.parseInt(valor));
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
     * Guarda (o actualitza) una partida i els seus jugadors a la BBDD.
     *
     * @param p Partida a guardar
     */
    public void guardarBBDD(Partida p) {
        if (conexion == null) {
            System.out.println("Sense connexió. No es pot guardar la partida.");
            return;
        }

        // Guardar partida (MERGE)
        String sqlPartida =
            "MERGE INTO partidas dst " +
            "USING (SELECT 1 FROM dual) src ON (dst.id = 1) " +
            "WHEN MATCHED THEN " +
            "  UPDATE SET turnos = " + p.getTurnos() +
            ", jugador_actual = " + p.getJugadorActual() +
            ", finalizada = " + (p.isFinalizada() ? 1 : 0) + " " +
            "WHEN NOT MATCHED THEN " +
            "  INSERT (turnos, jugador_actual, finalizada) VALUES (" +
            p.getTurnos() + ", " + p.getJugadorActual() + ", " +
            (p.isFinalizada() ? 1 : 0) + ")";

        insert(conexion, sqlPartida);

        // Guardar jugadors
        for (Jugador j : p.getJugadores()) {
            String sqlJugador =
                "MERGE INTO jugadores dst " +
                "USING (SELECT '" + j.getNombre() + "' AS nombre FROM dual) src " +
                "  ON (dst.nombre = src.nombre AND dst.partida_id = 1) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET posicion = " + j.getPosicion() + " " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (nombre, color, posicion, partida_id) VALUES ('" +
                j.getNombre() + "', '" + j.getColor() + "', " +
                j.getPosicion() + ", 1)";
            insert(conexion, sqlJugador);
        }
    }

    /**
     * Carrega una partida des de la BBDD a partir del seu ID.
     *
     * @param id ID de la partida
     * @return Partida carregada, o Partida buida si no es troba
     */
    public Partida cargarBBDD(int id) {
        Partida partida = new Partida();
        if (conexion == null) {
            System.out.println("Sense connexió. No es pot carregar la partida.");
            return partida;
        }

        // Carregar partida
        ArrayList<String> colsPartida = new ArrayList<>();
        colsPartida.add("TURNOS");
        colsPartida.add("JUGADOR_ACTUAL");
        colsPartida.add("FINALIZADA");

        ArrayList<LinkedHashMap<String, String>> filesPartida =
            select(conexion, "SELECT * FROM partidas WHERE id = " + id);

        if (filesPartida.isEmpty()) {
            System.out.println("No s'ha trobat la partida amb id=" + id);
            return partida;
        }

        LinkedHashMap<String, String> fp = filesPartida.get(0);
        partida.setTurnos       (Integer.parseInt(fp.get("TURNOS")));
        partida.setJugadorActual(Integer.parseInt(fp.get("JUGADOR_ACTUAL")));
        partida.setFinalizada   ("1".equals(fp.get("FINALIZADA")));

        // Mostrar per consola
        System.out.println("── Partida carregada ──");
        procesamientoSelect(conexion, "SELECT * FROM partidas WHERE id = " + id, colsPartida);

        // Carregar jugadors
        ArrayList<String> colsJugador = new ArrayList<>();
        colsJugador.add("NOMBRE");
        colsJugador.add("COLOR");
        colsJugador.add("POSICION");

        ArrayList<LinkedHashMap<String, String>> filesJugadors =
            select(conexion, "SELECT * FROM jugadores WHERE partida_id = " + id);

        System.out.println("── Jugadors ──");
        for (LinkedHashMap<String, String> fila : filesJugadors) {
            String nom    = fila.get("NOMBRE");
            String color  = fila.get("COLOR");
            int    posicio = Integer.parseInt(fila.get("POSICION"));

            Pinguino pg = new Pinguino(nom, color);
            pg.setPosicion(posicio);
            partida.anadirJugador(pg);

            for (String col : colsJugador) {
                procesarValor(col, fila.get(col));
            }
        }

        return partida;
    }
}