package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Partida {

    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnos;
    private int jugadorActual;
    private boolean finalizada;
    private Jugador ganador;
    private List<String> logEventos;
    private Pinguino jugadorPierdeTurno;
    private Random random;


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

    // Getters y Setters 

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
    public void anadirJugador(Jugador j) {
        jugadores.add(j);
    }

    public void anadirEvento(String evento) {
        logEventos.add(evento);
    }

    public Jugador getJugadorActualObj() {
        if (jugadores.isEmpty()) return null;
        return jugadores.get(jugadorActual);
    }


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
