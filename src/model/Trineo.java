package model;

/**
 * REPRESENTA UNA CASELLA DE TIPUS 'TRINEU'.
 * QUAN UN JUGADOR HI CAU, AVANÇA AUTOMÀTICAMENT FINS AL SEGÜENT TRINEU DEL TAULELL.
 */
public class Trineo extends Casilla {

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DEL TRINEU.
     */
    public Trineo(int posicion) {
        super(posicion);
    }

    /**
     * EXECUTA L'ACCIÓ D'AVANÇ RÀPID.
     * CERCA EL SEGÜENT TRINEU AL TAULELL I HI DESPLAÇA EL JUGADOR.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        Tablero tablero = partida.getTablero();
        int siguienteTrineo = tablero.buscarSiguienteTrineo(getPosicion());
        
        if (siguienteTrineo != -1) {
            jugador.setPosicion(siguienteTrineo);
            partida.anadirEvento(jugador.getNombre().toUpperCase() + " HA AGAFAT UN TRINEU I AVANÇA FINS A LA CASELLA " + siguienteTrineo + ".");
        } else {
            partida.anadirEvento(jugador.getNombre().toUpperCase() + " JA ÉS A L'ÚLTIM TRINEU I NO POT AVANÇAR MÉS.");
        }
    }
}
