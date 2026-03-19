package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa una partida del juego del Pingüino.
 * Contiene el tablero, los jugadores, el turno actual y el estado de la partida.
 */
public class Partida {

    /** Tablero de la partida. */
    private Tablero tablero;

    /** Lista de jugadores (Pinguino o Foca). */
    private ArrayList<Jugador> jugadores;

    /** Número de turnos transcurridos. */
    private int turnos;

    /** Índice del jugador que tiene el turno actual. */
    private int jugadorActual;

    /** Indica si la partida ha finalizado. */
    private boolean finalizada;

    /** Jugador ganador (null si no ha terminado). */
    private Jugador ganador;

    /** Log de eventos ocurridos durante la partida. */
    private List<String> logEventos;

    /** Jugador que pierde el próximo turno (null si ninguno). */
    private Pinguino jugadorPierdeTurno;

    /** Generador de números aleatorios compartido. */
    private Random random;

    /**
     * Constructor de Partida.
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

    // --- Getters y Setters ---

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

    public Pinguino getJugadorPierdeTurno() { return jugadorPierdeTurno; }
    public void setJugadorPierdeTurno(Pinguino jugadorPierdeTurno) { this.jugadorPierdeTurno = jugadorPierdeTurno; }

    public Random getRandom() { return random; }
    public void setRandom(Random random) { this.random = random; }

    // --- Métodos ---

    /**
     * Añade un jugador a la partida.
     * @param j jugador a añadir
     */
    public void anadirJugador(Jugador j) {
        jugadores.add(j);
    }

    /**
     * Añade un mensaje al log de eventos.
     * @param evento mensaje del evento
     */
    public void anadirEvento(String evento) {
        logEventos.add(evento);
    }

    /**
     * Devuelve el jugador que tiene el turno actual.
     * @return jugador actual
     */
    public Jugador getJugadorActualObj() {
        if (jugadores.isEmpty()) return null;
        return jugadores.get(jugadorActual);
    }

    /**
     * Avanza al siguiente turno.
     */
    public void siguienteTurno() {
        jugadorActual = (jugadorActual + 1) % jugadores.size();
        turnos++;
    }

    @Override
    public String toString() {
        return "Partida{turnos=" + turnos + ", jugadorActual=" + jugadorActual +
               ", finalizada=" + finalizada + ", jugadores=" + jugadores.size() + "}";
    }
}
