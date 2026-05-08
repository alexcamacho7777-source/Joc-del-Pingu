package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * REPRESENTA UNA INSTÀNCIA DE PARTIDA DINS DEL JOC.
 * COORDINA ELS JUGADORS, EL TAULELL, ELS TORNS I EL REGISTRE D'ESDEVENIMENTS.
 */
public class Partida {

    private int id;
    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnos;
    private int jugadorActual;
    private boolean finalizada;
    private Jugador ganador;
    private List<String> logEventos;
    private Jugador jugadorPierdeTurno;
    private String nombre;
    private Random random;

    /**
     * CONSTRUCTOR PER DEFECTE QUE INICIALITZA ELS COMPONENTS DE LA PARTIDA.
     */
    public Partida() {
        this.tablero = new Tablero();
        this.jugadores = new ArrayList<>();
        this.turnos = 0;
        this.jugadorActual = 0;
        this.finalizada = false;
        this.ganador = null;
        this.logEventos = new ArrayList<>();
        this.random = new Random();
    }

    // GETTERS I SETTERS PER A L'ACCÉS ALS ATRIBUTS DE LA PARTIDA
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Tablero getTablero() { return tablero; }
    public void setTablero(Tablero tablero) { this.tablero = tablero; }

    public ArrayList<Jugador> getJugadores() { return jugadores; }
    public void setJugadores(ArrayList<Jugador> jugadores) { this.jugadores = jugadores; }

    public int getTurnos() { return turnos; }
    public void setTurnos(int turnos) { this.turnos = turnos; }

    public int getJugadorActual() { return jugadorActual; }
    public void setJugadorActual(int jugadorActual) { this.jugadorActual = jugadorActual; }

    public boolean isFinalizada() { return finalizada; }
    public void setFinalizada(boolean finalizada) { this.finalizada = finalizada; }

    public Jugador getGanador() { return ganador; }
    public void setGanador(Jugador ganador) { this.ganador = ganador; }

    public List<String> getLogEventos() { return logEventos; }
    public void setLogEventos(List<String> logEventos) { this.logEventos = logEventos; }

    public Jugador getJugadorPierdeTurno() { return jugadorPierdeTurno; }
    public void setJugadorPierdeTurno(Jugador jugadorPierdeTurno) { this.jugadorPierdeTurno = jugadorPierdeTurno; }

    public Random getRandom() { return random; }
    public void setRandom(Random random) { this.random = random; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * AFEGEIX UN NOU JUGADOR A LA LLISTA DE LA PARTIDA.
     */
    public void anadirJugador(Jugador j) {
        jugadores.add(j);
    }

    /**
     * REGISTRA UN NOU ESDEVENIMENT AL LOG CRONOLÒGIC DE LA PARTIDA.
     */
    public void anadirEvento(String evento) {
        logEventos.add(evento);
    }

    /**
     * RETORNA L'OBJECTE JUGADOR QUE TÉ EL TORN ACTUALMENT.
     */
    public Jugador getJugadorActualObj() {
        Jugador j = null;
        if (jugadores != null && !jugadores.isEmpty()) {
            if (jugadorActual < 0 || jugadorActual >= jugadores.size()) {
                jugadorActual = 0;
            }
            j = jugadores.get(jugadorActual);
        }
        return j;
    }

    /**
     * AVANÇA AL SEGÜENT TORN I INCREMENTA EL COMPTADOR TOTAL.
     */
    public void siguienteTurno() {
        if (jugadores.size() > 0) {
            jugadorActual = (jugadorActual + 1) % jugadores.size();
            turnos++;
        }
    }

    @Override
    public String toString() {
        return "PARTIDA{TORNS=" + turnos + ", JUGADOR_ACTUAL=" + jugadorActual +
               ", FINALITZADA=" + finalizada + ", JUGADORS=" + jugadores.size() + "}";
    }
}
