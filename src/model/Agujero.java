package model;

/**
 * Casilla Agujero (Forat al gel): envía al jugador al agujero anterior.
 * Si es el primer agujero, retrocede al principio del tablero.
 */
public class Agujero extends Casilla {

    /**
     * Constructor de Agujero.
     * @param posicion índice de la casilla
     */
    public Agujero(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        Tablero tablero = partida.getTablero();
        int anteriorAgujero = tablero.buscarAgujeroAnterior(getPosicion());
        jugador.setPosicion(anteriorAgujero);
        partida.anadirEvento(jugador.getNombre() + " ha caído en un agujero y va a la posición " + anteriorAgujero + ".");
    }
}
