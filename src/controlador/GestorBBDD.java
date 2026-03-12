package controlador;

import model.Partida;

public class GestorBBDD {

    private String urlBBDD;
    private String username;
    private String password;

    public GestorBBDD(String urlBBDD, String username, String password) {

        this.urlBBDD = urlBBDD;
        this.username = username;
        this.password = password;

    }

    public void guardarBBDD(Partida p) {

        System.out.println("Guardando partida en la base de datos...");

        // Conexion a la BD

    }

    public Partida cargarBBDD(int id) {

        System.out.println("Cargando partida con ID: " + id);

        // Conexion a la BD

        return new Partida();
    }

}