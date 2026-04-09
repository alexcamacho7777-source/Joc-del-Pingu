package controlador;

import model.*;

import java.sql.*;

public class GestorBBDD {

    private static final String URL_DEFAULT =
            "jdbc:oracle:thin:@//192.168.26.3:1521/XEPDB1";
    private static final String USER_DEFAULT  = "DW2526_GR03_PINGU";
    private static final String PASS_DEFAULT  = "AACGFAM";

    private String urlBBDD;
    private String username;
    private String password;

    public GestorBBDD() {
        this.urlBBDD  = URL_DEFAULT;
        this.username = USER_DEFAULT;
        this.password = PASS_DEFAULT;
    }

    public GestorBBDD(String urlBBDD, String username, String password) {
        this.urlBBDD  = urlBBDD;
        this.username = username;
        this.password = password;
    }

    public String getUrlBBDD() { return urlBBDD; }
    public void setUrlBBDD(String urlBBDD) { this.urlBBDD = urlBBDD; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    private Connection getConexion() throws SQLException {
        return DriverManager.getConnection(urlBBDD, username, password);
    }

    public void guardarBBDD(Partida p) {
        String sql = "MERGE INTO partidas dst " +
                     "USING (SELECT 1 FROM dual) src ON (dst.id = 1) " +
                     "WHEN MATCHED THEN " +
                     "  UPDATE SET turnos=?, jugador_actual=?, finalizada=? " +
                     "WHEN NOT MATCHED THEN " +
                     "  INSERT (turnos, jugador_actual, finalizada) VALUES (?, ?, ?)";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getTurnos());
            ps.setInt(2, p.getJugadorActual());
            ps.setInt(3, p.isFinalizada() ? 1 : 0);
            ps.setInt(4, p.getTurnos());
            ps.setInt(5, p.getJugadorActual());
            ps.setInt(6, p.isFinalizada() ? 1 : 0);
            ps.executeUpdate();
            guardarJugadores(con, p);
        } catch (SQLException e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
        }
    }

    private void guardarJugadores(Connection con, Partida p) throws SQLException {
        String sql = "MERGE INTO jugadores dst " +
                     "USING (SELECT ? AS nombre FROM dual) src ON (dst.nombre = src.nombre AND dst.partida_id = 1) " +
                     "WHEN MATCHED THEN " +
                     "  UPDATE SET posicion=? " +
                     "WHEN NOT MATCHED THEN " +
                     "  INSERT (nombre, color, posicion, partida_id) VALUES (?, ?, ?, 1)";
        for (Jugador j : p.getJugadores()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, j.getNombre());
                ps.setInt(2, j.getPosicion());
                ps.setString(3, j.getNombre());
                ps.setString(4, j.getColor());
                ps.setInt(5, j.getPosicion());
                ps.executeUpdate();
            }
        }
    }

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
