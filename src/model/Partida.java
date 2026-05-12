package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * REPRESENTA UNA INSTÀNCIA DE PARTIDA DINS DEL JOC.
 * Aquesta classe actua com el contenidor principal de l'estat del joc en un 
 * moment determinat. Coordina la relació entre els jugadors, el taulell on 
 * es mouen, i manté el control del flux de torns i la finalització del joc.
 * 
 * @author Alex Camacho
 * @version 1.5
 */
public class Partida {

    // Identificador únic per a la persistència en BBDD
    private int id;
    
    // El taulell amb la disposició de les caselles
    private Tablero tablero;
    
    // Llista de participants (Pinguins i Foca)
    private ArrayList<Jugador> jugadores;
    
    // Comptador total de torns transcorreguts
    private int turnos;
    
    // Índex del jugador que té el torn actual (0 a N-1)
    private int jugadorActual;
    
    // Flag que indica si algú ha arribat a la meta
    private boolean finalizada;
    
    // Referència al jugador que ha guanyat
    private Jugador ganador;
    
    // Historial textual de tot el que succeeix en la partida
    private List<String> logEventos;
    
    // Referència temporal a un jugador que ha estat castigat sense torn
    private Jugador jugadorPierdeTurno;
    
    // Nom personalitzat per identificar la partida en el lobby
    private String nombre;
    
    // Generador aleatori compartit per a tota la lògica de la partida
    private Random random;

    /**
     * CONSTRUCTOR PER DEFECTE QUE INICIALITZA ELS COMPONENTS DE LA PARTIDA.
     * Crea un taulell nou i prepara les llistes necessàries per començar a jugar.
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

    // ── MÈTODES D'ACCÉS (GETTERS I SETTERS) ──────────────────────────────────
    
    /** @return L'ID de la partida en la base de dades. */
    public int getId() { return id; }
    /** @param id Nou ID assignat pel gestor de BBDD. */
    public void setId(int id) { this.id = id; }

    /** @return L'objecte Tablero associat. */
    public Tablero getTablero() { return tablero; }
    /** @param tablero El taulell amb les caselles ja generades. */
    public void setTablero(Tablero tablero) { this.tablero = tablero; }

    /** @return Llista completa de jugadors. */
    public ArrayList<Jugador> getJugadores() { return jugadores; }
    /** @param jugadores Nova llista de participants. */
    public void setJugadores(ArrayList<Jugador> jugadores) { this.jugadores = jugadores; }

    /** @return Número de torns totals jugats. */
    public int getTurnos() { return turnos; }
    /** @param turnos Valor manual de torns (per a càrregues). */
    public void setTurnos(int turnos) { this.turnos = turnos; }

    /** @return Índex (0-based) del jugador actual. */
    public int getJugadorActual() { return jugadorActual; }
    /** @param jugadorActual Nou índex de torn. */
    public void setJugadorActual(int jugadorActual) { this.jugadorActual = jugadorActual; }

    /** @return true si la partida ha acabat. */
    public boolean isFinalizada() { return finalizada; }
    /** @param finalizada Estat de finalització. */
    public void setFinalizada(boolean finalizada) { this.finalizada = finalizada; }

    /** @return El guanyador de la partida o null si encara no n'hi ha. */
    public Jugador getGanador() { return ganador; }
    /** @param ganador Objecte jugador que ha tocat la meta. */
    public void setGanador(Jugador ganador) { this.ganador = ganador; }

    /** @return Llista de missatges d'esdeveniments. */
    public List<String> getLogEventos() { return logEventos; }
    /** @param logEventos Nou historial de missatges. */
    public void setLogEventos(List<String> logEventos) { this.logEventos = logEventos; }

    /** @return Jugador que no podrà tirar en el seu proper torn. */
    public Jugador getJugadorPierdeTurno() { return jugadorPierdeTurno; }
    /** @param jugadorPierdeTurno Jugador castigat. */
    public void setJugadorPierdeTurno(Jugador jugadorPierdeTurno) { this.jugadorPierdeTurno = jugadorPierdeTurno; }

    /** @return El generador aleatori de la partida. */
    public Random getRandom() { return random; }
    /** @param random Instància de Random (pot tenir una seed fixa). */
    public void setRandom(Random random) { this.random = random; }

    /** @return El nom descriptiu de la partida. */
    public String getNombre() { return nombre; }
    /** @param nombre Nom assignat per l'usuari. */
    public void setNombre(String nombre) { this.nombre = nombre; }

    // ── LÒGICA DE GESTIÓ DE JUGADORS I TORNS ────────────────────────────────

    /**
     * AFEGEIX UN NOU JUGADOR A LA LLISTA DE LA PARTIDA.
     * @param j El jugador (Pinguino o Foca) a incorporar.
     */
    public void anadirJugador(Jugador j) {
        jugadores.add(j);
    }

    /**
     * REGISTRA UN NOU ESDEVENIMENT AL LOG CRONOLÒGIC DE LA PARTIDA.
     * @param evento Missatge descriptiu de l'acció.
     */
    public void anadirEvento(String evento) {
        logEventos.add(evento);
    }

    /**
     * RETORNA L'OBJECTE JUGADOR QUE TÉ EL TORN ACTUALMENT.
     * Inclou una verificació de seguretat per evitar desbordaments de l'índex.
     * @return El Jugador actiu o null si la llista és buida.
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
     * Utilitza l'operador mòdul per tornar al primer jugador quan s'arriba al final.
     */
    public void siguienteTurno() {
        if (jugadores.size() > 0) {
            jugadorActual = (jugadorActual + 1) % jugadores.size();
            turnos++;
        }
    }

    /**
     * REPRESENTACIÓ EN TEXT DE L'ESTAT DE LA PARTIDA.
     */
    @Override
    public String toString() {
        return "PARTIDA{ID=" + id + ", NOM='" + nombre + "', TORNS=" + turnos + 
               ", FINALITZADA=" + finalizada + ", JUGADORS=" + jugadores.size() + "}";
    }
}
