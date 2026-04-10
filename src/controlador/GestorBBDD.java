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
        switch (col.toUpperCase()) {
            case "ID_PARTIDA":
                System.out.println("ID Partida:     " + valor);
                break;
            case "TORN_ACTUAL":
                System.out.println("Torn Actual:    " + valor);
                break;
            case "NOM_JUGADOR":
                System.out.println("Jugador:        " + valor);
                break;
            case "POSICIO_ACTUAL":
                System.out.println("Posició:        " + valor);
                break;
            case "DAUS":
                System.out.println("Daus restants:  " + valor);
                break;
            case "PEIXOS":
                System.out.println("Peixos:         " + valor);
                break;
            case "BOLES_NEU":
                System.out.println("Boles de neu:   " + valor);
                break;
            case "VICTORIES":
                System.out.println("Victòries:      " + valor);
                break;
            default:
                System.out.println(col + ": " + valor);
                break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MÈTODES ESPECÍFICS DEL JOC
    // ═════════════════════════════════════════════════════════════════════════
    
    /** Registra un nou jugador a la BBDD (Menú Registre) */
    public boolean registrarUsuario(String username) {
        if (conexion == null) return false;
        // Obtenim seguent ID
        ArrayList<LinkedHashMap<String, String>> res = select(conexion, "SELECT MAX(id_jugador) as MAX_ID FROM jugador");
        int nextId = 1;
        if (!res.isEmpty() && res.get(0).get("MAX_ID") != null) {
            nextId = Integer.parseInt(res.get(0).get("MAX_ID")) + 1;
        }
        
        // Comprovar si ja existeix
        if (!select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty()) {
            System.out.println("L'usuari ja existeix!");
            return false;
        }
        
        String sql = "INSERT INTO jugador (id_jugador, nom_jugador, color_jugador, victories) VALUES (" + nextId + ", '" + username + "', 'Blau', 0)";
        return insert(conexion, sql) > 0;
    }

    /** Comprova si un jugador existeix (Menú Login) */
    public boolean loginUsuario(String username) {
        if (conexion == null) return false;
        return !select(conexion, "SELECT nom_jugador FROM jugador WHERE nom_jugador = '" + username + "'").isEmpty();
    }

    /**
     * Guarda (o actualitza) una partida i els seus jugadors a la BBDD.
     */
    public void guardarBBDD(Partida p) {
        if (conexion == null) {
            System.out.println("Sense connexió. No es pot guardar la partida.");
            return;
        }

        int idPartida = 1;
        int idTaulell = 1;

        // MERGE PARTIDA
        String sqlPartida =
            "MERGE INTO partida dst " +
            "USING (SELECT " + idPartida + " AS id_p FROM dual) src ON (dst.id_partida = src.id_p) " +
            "WHEN MATCHED THEN " +
            "  UPDATE SET torn_actual = " + p.getJugadorActual() + " " +
            "WHEN NOT MATCHED THEN " +
            "  INSERT (id_partida, id_taulell, nom_partida, data_creacio, torn_actual) " +
            "  VALUES (" + idPartida + ", " + idTaulell + ", 'Partida Principal', SYSDATE, " + p.getJugadorActual() + ")";
        insert(conexion, sqlPartida);

        java.util.List<Jugador> jugadores = p.getJugadores();
        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            int idJugador = i + 1; // 1,2,3,4,5...
            
            // Si el jugador ha ganado, le sumamos 1 a victories
            int addVictoria = (p.isFinalizada() && j.equals(p.getGanador())) ? 1 : 0;

            // MERGE JUGADOR
            String sqlJugador =
                "MERGE INTO jugador dst " +
                "USING (SELECT " + idJugador + " AS id_j FROM dual) src ON (dst.id_jugador = src.id_j) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET nom_jugador = '" + j.getNombre() + "', color_jugador = '" + j.getColor() + "', victories = victories + " + addVictoria + " " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (id_jugador, nom_jugador, color_jugador, victories) " +
                "  VALUES (" + idJugador + ", '" + j.getNombre() + "', '" + j.getColor() + "', " + addVictoria + ")";
            insert(conexion, sqlJugador);

            // Obtener inventario si aplica
            int daus = 0, peixos = 0, boles = 0;
            if (j instanceof Pinguino pingu && pingu.getInv() != null) {
                Item itDau = pingu.getInv().getItem(Dado.class);
                if (itDau != null) daus = itDau.getCantidad();
                
                Item itPez = pingu.getInv().getItem(Pez.class);
                if (itPez != null) peixos = itPez.getCantidad();
                
                Item itBola = pingu.getInv().getItem(BolaDeNieve.class);
                if (itBola != null) boles = itBola.getCantidad();
            }

            // MERGE JUGADOR_PARTIDA (posició + inventari combinats segons disseny corregit)
            String sqlJP =
                "MERGE INTO jugador_partida dst " +
                "USING (SELECT " + idJugador + " AS id_j, " + idPartida + " AS id_p FROM dual) src " +
                "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET posicio_actual = " + j.getPosicion() + ", daus = " + daus + ", peixos = " + peixos + ", boles_neu = " + boles + " " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (id_jugador, id_partida, posicio_actual, daus, peixos, boles_neu) " +
                "  VALUES (" + idJugador + ", " + idPartida + ", " + j.getPosicion() + ", " + daus + ", " + peixos + ", " + boles + ")";
            insert(conexion, sqlJP);
            
            // MERGE TORN
            String sqlTorn =
                "MERGE INTO torn dst " +
                "USING (SELECT " + idJugador + " AS id_j, " + idPartida + " AS id_p FROM dual) src " +
                "ON (dst.id_jugador = src.id_j AND dst.id_partida = src.id_p) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET ordre = " + i + " " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (id_jugador, id_partida, ordre) VALUES (" + idJugador + ", " + idPartida + ", " + i + ")";
            insert(conexion, sqlTorn);
        }
        System.out.println("Partida guardada a la BBDD Correctament format Nou!");
    }

    /**
     * Carrega una partida des de la BBDD a partir del seu ID.
     */
    public Partida cargarBBDD(int id_partida) {
        Partida partida = new Partida();
        if (conexion == null) {
            System.out.println("Sense connexió. No es pot carregar la partida.");
            return partida;
        }

        ArrayList<String> colsPartida = new ArrayList<>();
        colsPartida.add("ID_PARTIDA");
        colsPartida.add("TORN_ACTUAL");

        ArrayList<LinkedHashMap<String, String>> filesPartida =
            select(conexion, "SELECT * FROM partida WHERE id_partida = " + id_partida);

        if (filesPartida.isEmpty()) {
            System.out.println("No s'ha trobat la partida amb ID=" + id_partida);
            return partida;
        }

        LinkedHashMap<String, String> fp = filesPartida.get(0);
        partida.setJugadorActual(Integer.parseInt(fp.get("TORN_ACTUAL")));

        // Mostrar per consola
        System.out.println("── Partida carregada ──");
        procesamientoSelect(conexion, "SELECT * FROM partida WHERE id_partida = " + id_partida, colsPartida);

        // Carregar jugadors_partida JOIN jugador
        ArrayList<String> colsJugador = new ArrayList<>();
        colsJugador.add("NOM_JUGADOR");
        colsJugador.add("POSICIO_ACTUAL");
        colsJugador.add("DAUS");
        colsJugador.add("PEIXOS");
        colsJugador.add("BOLES_NEU");

        String sqlJugadors = 
            "SELECT j.nom_jugador, j.color_jugador, jp.posicio_actual, jp.daus, jp.peixos, jp.boles_neu " +
            "FROM jugador j " +
            "JOIN jugador_partida jp ON j.id_jugador = jp.id_jugador " +
            "JOIN torn t ON j.id_jugador = t.id_jugador " +
            "WHERE jp.id_partida = " + id_partida + " AND t.id_partida = " + id_partida + " " +
            "ORDER BY t.ordre ASC";

        ArrayList<LinkedHashMap<String, String>> filesJugadors = select(conexion, sqlJugadors);

        System.out.println("── Jugadors ──");
        for (LinkedHashMap<String, String> fila : filesJugadors) {
            String nom = fila.get("NOM_JUGADOR");
            String color = fila.get("COLOR_JUGADOR") != null ? fila.get("COLOR_JUGADOR") : "Gris";
            int posicio = Integer.parseInt(fila.get("POSICIO_ACTUAL"));
            int daus = Integer.parseInt(fila.get("DAUS") != null ? fila.get("DAUS") : "0");
            int peixos = Integer.parseInt(fila.get("PEIXOS") != null ? fila.get("PEIXOS") : "0");
            int boles = Integer.parseInt(fila.get("BOLES_NEU") != null ? fila.get("BOLES_NEU") : "0");

            Jugador j;
            if (nom.contains("Foca") || nom.contains("CPU")) {
                Foca foca = new Foca();
                foca.setNombre(nom);
                foca.setColor(color);
                j = foca;
            } else {
                Inventario inv = new Inventario();
                if (daus > 0) inv.anadirItem(new Dado("normal", daus, 1, 6)); // Asumimos dados normales limitados a cantidad
                if (peixos > 0) inv.anadirItem(new Pez(peixos));
                if (boles > 0) inv.anadirItem(new BolaDeNieve(boles));
                j = new Pinguino(nom, color, posicio, inv);
            }
            j.setPosicion(posicio);
            partida.anadirJugador(j);

            for (String col : colsJugador) procesarValor(col, fila.get(col));
        }

        return partida;
    }
}