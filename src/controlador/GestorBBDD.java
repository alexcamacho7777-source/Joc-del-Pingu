import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Clase que proporciona métodos para interactuar con una base de datos Oracle.
 */
public class BBDD {

	/**
	 * Intenta establecer una conexión a la base de datos Oracle. NO HACE FALTA QUE
	 * ENTENDÁIS CÓMO FUNCIONA, SE HACE TODO DE MANERA AUTOMÁTICA.
	 *
	 * @param scan Scanner de main con el que vais a leer por consola
	 * @return Objeto Connection si la conexión es exitosa, null en caso contrario.
	 *         LA VARIABLE QUE DEVUELVE LA TENÉIS QUE GUARDAR PARA LAS DEMÁS
	 *         FUNCIONES
	 */
	public static Connection conectarBaseDatos(Scanner scan) {
		System.out.println("Intentando conectarse a la base de datos...");

		// 1) Elegir entorno con validación
		String entorno = "";
		boolean valido = false;
		while (!valido) {
			// PODEIS HARDCODEAR ESTAS VARIABLES SI VAIS A USAR SIEMPRE LAS MISMAS
			//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
			System.out.println("Selecciona centro o fuera de centro (CENTRO/FUERA):");
			entorno = scan.nextLine().trim().toLowerCase();

			if (entorno.equalsIgnoreCase("centro") || entorno.equalsIgnoreCase("fuera")) {
				valido = true;
			} else {
				System.out.println("Entrada no válida. Escribe CENTRO o FUERA.");
			}
		}

		String url = entorno.equals("centro") ? "jdbc:oracle:thin:@//192.168.3.26:1521/XEPDB2"
				: "jdbc:oracle:thin:@//oracle.ilerna.com:1521/XEPDB2";

		// 2) Pedir credenciales (con trim para evitar espacios raros)
		// PODEIS HARDCODEAR ESTAS CREDENCIALES SI VAIS A USAR SIEMPRE LAS MISMAS
		//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
		System.out.println("¿Usuario?");
		String user = scan.nextLine().trim();

		System.out.println("¿Contraseña?");
		String pwd = scan.nextLine(); // aquí NO hago trim por si la contraseña tuviera espacios

		// 3) Conectar
		try {
			// En muchos casos con JDBC moderno no hace falta, pero lo dejamos por si acaso
			Class.forName("oracle.jdbc.driver.OracleDriver");

			Connection con = DriverManager.getConnection(url, user, pwd);

			// 4) Comprobar que la conexión es válida (timeout 5 s)
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

	/**
	 * Cierra la conexión con la BBDD.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 */
	public static void cerrar(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException ignored) {
			}
		}
	}

	/**
	 * Realiza una inserción en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de inserción que hayáis creado.
	 */
	public static int insert(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Insert");
	}

	/**
	 * Realiza una actualización en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de actualización que hayáis creado.
	 */
	public static int update(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Update");
	}

	/**
	 * Realiza una eliminación en la base de datos.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de eliminación que hayáis creado.
	 */
	public static int delete(Connection con, String sql) {
		return executeInsUpDel(con, sql, "Delete");
	}

	/**
	 * Realiza una consulta en la base de datos y devuelve los resultados.
	 *
	 * @param con Objeto Connection que representa la conexión a la base de datos.
	 * @param sql Sentencia SQL de consulta.
	 * @return Devuelve un ArrayList con todas las filas del SELECT. Cada fila es un
	 *         Map con sus columnas (columna -> valor).
	 */
	public static ArrayList<LinkedHashMap<String, String>> select(Connection con, String sql) {

		ArrayList<LinkedHashMap<String, String>> resultados = new ArrayList<>();

		if (con == null) {
			System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
			return resultados;
		}

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

			ResultSetMetaData meta = rs.getMetaData();
			int numColumnas = meta.getColumnCount();

			while (rs.next()) {
				LinkedHashMap<String, String> fila = new LinkedHashMap<>();

				for (int i = 1; i <= numColumnas; i++) {
					String columna = meta.getColumnLabel(i);
					String valor = rs.getString(i);
					fila.put(columna, valor);
				}

				resultados.add(fila);
			}

		} catch (SQLException e) {
			System.out.println("Error en SELECT: " + e.getMessage());
		}

		return resultados;
	}

	/**
	 * Imprime los resultados de una consulta SELECT en la base de datos. EN ESTE
	 * CASO SÍ PODÉIS IMPRIMIR MÁS DE UNA FILA.
	 *
	 * @param con                         Objeto Connection que representa la
	 *                                    conexión a la base de datos.
	 * @param sql                         Sentencia SQL de consulta.
	 * @param listaElementosSeleccionados Array de Strings con los nombres de las
	 *                                    columnas seleccionadas.
	 */
	public static void print(Connection con, String sql, String[] listaElementosSeleccionados) {
		if (con == null) {
			System.out.println("No hay conexión. Llama antes a conectarBaseDatos().");
			return;
		}

		try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

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

			if (!hayResultados) {
				System.out.println("No se ha encontrado nada");
			}

		} catch (SQLException e) {
			System.out.println("Error en SELECT: " + e.getMessage());
		}
	}

	/**
	 * Ejecuta las consultas Insert, Update o Delete.
	 *
	 * @param con      Objeto Connection que representa la conexión a la base de
	 *                 datos.
	 * @param sql      Sentencia SQL que se va a ejecutar.
	 * @param etiqueta Consulta a ejecutar -> Insert / Update / Delete
	 * @return Número de filas afectadas
	 */
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
