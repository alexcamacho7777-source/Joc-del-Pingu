package model;

/**
 * Casilla normal: no tiene ningún efecto especial.
 */
public class Normal extends Casilla {

    /**
     * Constructor de Normal.
     * @param posicion índice de la casilla
     */
    public Normal(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        // Sin efecto
    }
}
