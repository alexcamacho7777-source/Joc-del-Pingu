package model;

import java.util.ArrayList;

public class Partida {

    private Tablero tablero;
    private ArrayList<Jugador> jugadores;
    private int turnos;
    private int jugadorActual;
    private boolean finalizada;
    private Jugador ganador;

    public Partida() {
        tablero = new Tablero();
        jugadores = new ArrayList<>();
        turnos = 0;
        jugadorActual = 0;
        finalizada = false;
    }

    public Jugador getJugadorActual() {
        return jugadores.get(jugadorActual);
    }

    public Tablero getTablero() {
        return tablero;
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public void siguienteTurno() {
        jugadorActual++;

        if (jugadorActual >= jugadores.size()) {
            jugadorActual = 0;
            turnos++;
        }
    }
}