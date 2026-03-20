package model;

/**
 * Casilla Oso: envía al jugador al inicio del tablero.
 * Si el jugador tiene un pez puede sobornar al oso y evitar la penalización.
 */
public class Oso extends Casilla {

    /**
     * Constructor de Oso.
     * @param posicion índice de la casilla
     */
    public Oso(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (jugador instanceof Pinguino p) {
            Item pez = p.getInv().getItem(Pez.class);
            if (pez != null) {
                // Soborno: pierde el pez pero no retrocede
                pez.setCantidad(pez.getCantidad() - 1);
                if (pez.getCantidad() <= 0) p.getInv().quitarItem(pez);
                partida.anadirEvento(p.getNombre() + " ha sobornado al oso con un pez.");
            } else {
                p.setPosicion(0);
                partida.anadirEvento(p.getNombre() + " ha sido capturado por el oso y vuelve al inicio.");
            }
        }
    }
}
