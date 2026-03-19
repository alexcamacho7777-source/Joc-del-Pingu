package model;

/**
 * Clase abstracta que representa un jugador del juego.
 */
public abstract class Jugador {

    /** Posición actual en el tablero (índice de casilla). */
    private int posicion;

    /** Nombre del jugador. */
    private String nombre;

    /** Color identificativo del pingüino. */
    private String color;

    /**
     * Constructor de Jugador.
     * @param nombre nombre del jugador
     * @param color  color del pingüino
     */
    public Jugador(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.posicion = 0;
    }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    /**
     * Mueve al jugador el número de posiciones indicadas.
     * @param p número de casillas a avanzar (puede ser negativo para retroceder)
     */
    public void moverPosicion(int p) {
        this.posicion += p;
        if (this.posicion < 0) this.posicion = 0;
    }

    @Override
    public String toString() {
        return nombre + " (" + color + ") - Pos: " + posicion;
    }
}
