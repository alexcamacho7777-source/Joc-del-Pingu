package model;
/**
 * Casilla Trineo: avanza al jugador al siguiente trineo del tablero.
 * Si ya está en el último trineo, no pasa nada.
 */
public class Trineo extends Casilla {

    /**
     * Constructor de Trineo.
     * @param posicion índice de la casilla
     */
    public Trineo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        Tablero tablero = partida.getTablero();
        int siguienteTrineo = tablero.buscarSiguienteTrineo(getPosicion());
        if (siguienteTrineo != -1) {
            jugador.setPosicion(siguienteTrineo);
            partida.anadirEvento(jugador.getNombre() + " ha cogido un trineo y avanza a la posición " + siguienteTrineo + ".");
        } else {
            partida.anadirEvento(jugador.getNombre() + " ya está en el último trineo, no se mueve.");
        }
    }
}