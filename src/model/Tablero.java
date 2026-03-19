package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Representa el tablero del juego con sus casillas.
 * El tablero tiene al menos 50 casillas generadas aleatoriamente.
 */
public class Tablero {

    /** Lista de casillas del tablero. */
    private ArrayList<Casilla> casillas;

    /** Número de casillas del tablero. */
    public static final int TOTAL_CASILLAS = 50;

    /**
     * Constructor de Tablero. Genera las casillas aleatoriamente.
     */
    public Tablero() {
        this.casillas = new ArrayList<>();
        generarTablero(new Random());
    }

    /**
     * Constructor con semilla para pruebas reproducibles.
     * @param r instancia de Random
     */
    public Tablero(Random r) {
        this.casillas = new ArrayList<>();
        generarTablero(r);
    }

    public ArrayList<Casilla> getCasillas() { return casillas; }
    public void setCasillas(ArrayList<Casilla> casillas) { this.casillas = casillas; }

    /**
     * Genera las casillas del tablero aleatoriamente.
     * La primera y la última son siempre normales.
     */
    private void generarTablero(Random r) {
        casillas.add(new Normal(0)); // inicio
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            casillas.add(crearCasillaAleatoria(i, r));
        }
        casillas.add(new Normal(TOTAL_CASILLAS - 1)); // meta
    }

    /**
     * Crea una casilla aleatoria según probabilidades.
     */
    private Casilla crearCasillaAleatoria(int pos, Random r) {
        int tipo = r.nextInt(10);
        return switch (tipo) {
            case 0 -> new Oso(pos);
            case 1 -> new Agujero(pos);
            case 2 -> new Trineo(pos);
            case 3 -> new Evento(pos);
            case 4 -> new SueloQuebradizo(pos);
            default -> new Normal(pos);
        };
    }

    /**
     * Devuelve la casilla en la posición indicada.
     * @param pos índice
     * @return la casilla correspondiente
     */
    public Casilla getCasilla(int pos) {
        if (pos < 0) return casillas.get(0);
        if (pos >= casillas.size()) return casillas.get(casillas.size() - 1);
        return casillas.get(pos);
    }

    /**
     * Busca el agujero anterior a la posición dada.
     * @param posActual posición actual
     * @return índice del agujero anterior, o 0 si no hay ninguno
     */
    public int buscarAgujeroAnterior(int posActual) {
        for (int i = posActual - 1; i >= 0; i--) {
            if (casillas.get(i) instanceof Agujero) return i;
        }
        return 0;
    }

    /**
     * Busca el trineo siguiente a la posición dada.
     * @param posActual posición actual
     * @return índice del siguiente trineo, o -1 si no hay ninguno
     */
    public int buscarSiguienteTrineo(int posActual) {
        for (int i = posActual + 1; i < casillas.size(); i++) {
            if (casillas.get(i) instanceof Trineo) return i;
        }
        return -1;
    }

    /**
     * Actualiza el estado visual del tablero (reservado para JavaFX).
     */
    public void actualizarTablero() {
        // Implementación en la vista
    }

    /**
     * Devuelve el número total de casillas.
     */
    public int getTotalCasillas() {
        return casillas.size();
    }
}
