package model;

/**
 * Clase abstracta que representa una casilla del tablero.
 */
public abstract class Casilla {

    /** Posición de la casilla en el tablero. */
    private int posicion;

    /**
     * Constructor de Casilla.
     * @param posicion índice de la casilla
     */
    public Casilla(int posicion) {
        this.posicion = posicion;
    }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    /**
     * Ejecuta la acción de la casilla sobre el jugador que cae en ella.
     * @param partida  referencia a la partida en curso
     * @param jugador  jugador que ha caído en la casilla
     */
    public abstract void realizarAccion(Partida partida, Jugador jugador);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + posicion + "]";
    }
}
