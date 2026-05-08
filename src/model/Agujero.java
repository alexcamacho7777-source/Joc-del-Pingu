package model;

/**
 * REPRESENTA UNA CASELLA DE TIPUS 'FORAT'.
 * QUAN UN JUGADOR HI CAU, RETROCEDEIX FINS AL FORAT ANTERIOR DEL TAULELL.
 */
public class Agujero extends Casilla {

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DEL FORAT.
     */
    public Agujero(int posicion) {
        super(posicion);
    }

    /**
     * EXECUTA L'ACCIÓ DE RETROCEDIR EL JUGADOR FINS AL FORAT ANTERIOR.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(getPosicion());
        jugador.setPosicion(anteriorAgujero);
        partida.anadirEvento(jugador.getNombre().toUpperCase() + " HA CAIGUT EN UN FORAT I RETROCEDEIX FINS A LA POSICIÓ " + anteriorAgujero + ".");
    }
}
