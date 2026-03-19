package controlador;

import model.*;

import java.sql.*;

/**
 * Controlador que gestiona la persistencia de datos en base de datos.
 * Guarda y carga partidas, incluyendo posiciones, inventarios y turno actual.
 * La información sensible debe estar encriptada en BBDD.
 */
public class GestorBBDD {

    /** URL de conexión a la base de datos. */
    private String urlBBDD;

    /** Usuario de la base de datos. */
    privatess String username;

    /** Contraseña de la base de datos. */
    private String password;

    /**
     * Constructor de GestorBBDD.
     * @param urlBBDD  URL JDBC de la base de datos
     * @param username usuario
     * @param password contraseña
     */
    public GestorBBDD(String urlBBDD, String username, String password) {
        this.urlBBDD = urlBBDD;
        this.username = username;
        this.password = password;
    }

    public String getUrlBBDD() { return urlBBDD; }
    public void setUrlBBDD(String urlBBDD) { this.urlBBDD = urlBBDD; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /**
     * Obtiene una conexión a la base de datos.
     * @return conexión JDBC
     * @throws SQLException si hay error de conexión
     */
    private Connection getConexion() throws SQLException {
        return DriverManager.getConnection(urlBBDD, username, password);
    }

    /**
     * Guarda el estado completo de una partida en la base de datos.
     * @param p partida a guardar
     */
    public void guardarBBDD(Partida p) {
        String sql = "INSERT INTO partidas (turnos, jugador_actual, finalizada) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE turnos=?, jugador_actual=?, finalizada=?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getTurnos());
            ps.setInt(2, p.getJugadorActual());
            ps.setBoolean(3, p.isFinalizada());
            ps.setInt(4, p.getTurnos());
            ps.setInt(5, p.getJugadorActual());
            ps.setBoolean(6, p.isFinalizada());
            ps.executeUpdate();

            guardarJugadores(con, p);
        } catch (SQLException e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
        }
    }

    /**
     * Guarda los jugadores y sus inventarios.
     */
    private void guardarJugadores(Connection con, Partida p) throws SQLException {
        String sql = "INSERT INTO jugadores (nombre, color, posicion, partida_id) VALUES (?, ?, ?, 1) " +
                     "ON DUPLICATE KEY UPDATE posicion=?";
        for (Jugador j : p.getJugadores()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, j.getNombre());
                ps.setString(2, j.getColor());
                ps.setInt(3, j.getPosicion());
                ps.setInt(4, j.getPosicion());
                ps.executeUpdate();
            }
        }
    }

    /**
     * Carga una partida guardada desde la base de datos.
     * @param id identificador de la partida
     * @return la partida cargada
     */
    public Partida cargarBBDD(int id) {
        Partida partida = new Partida();
        String sql = "SELECT * FROM partidas WHERE id = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                partida.setTurnos(rs.getInt("turnos"));
                partida.setJugadorActual(rs.getInt("jugador_actual"));
                partida.setFinalizada(rs.getBoolean("finalizada"));
                cargarJugadores(con, partida, id);
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar partida: " + e.getMessage());
        }
        return partida;
    }

    /**
     * Carga los jugadores de una partida desde la base de datos.
     */
    private void cargarJugadores(Connection con, Partida partida, int idPartida) throws SQLException {
        String sql = "SELECT * FROM jugadores WHERE partida_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPartida);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pinguino p = new Pinguino(rs.getString("nombre"), rs.getString("color"));
                p.setPosicion(rs.getInt("posicion"));
                partida.anadirJugador(p);
            }
        }
    }
}
