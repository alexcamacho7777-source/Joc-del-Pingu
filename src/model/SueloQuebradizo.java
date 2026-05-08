package model;

/**
 * REPRESENTA UNA CASELLA DE TIPUS 'TERRA ESQUERDAT'.
 * L'EFECTE DEPÈN DEL PES DEL JUGADOR (NOMBRE D'OBJECTES A L'INVENTARI).
 */
public class SueloQuebradizo extends Casilla {

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DEL TERRA ESQUERDAT.
     */
    public SueloQuebradizo(int posicion) {
        super(posicion);
    }

    /**
     * EXECUTA LA LÒGICA DE FRAGILITAT DEL TERRA.
     * SI EL JUGADOR PORTA MÉS DE 5 OBJECTES, EL TERRA ES TRENCÀ I TORNA A L'INICI.
     * SI PORTA ENTRE 1 I 5, PERD UN TORN PER SORTIR-NE.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (jugador instanceof Pinguino p) {
            int total = p.getInv().totalItems();

            if (total > 5) {
                p.setPosicion(0);
                partida.anadirEvento(p.getNombre().toUpperCase() + " PORTA MASSA PES, EL TERRA ES TRENCÀ I TORNA A L'INICI.");
            } else if (total > 0) {
                partida.setJugadorPierdeTurno(p);
                partida.anadirEvento(p.getNombre().toUpperCase() + " ES QUEDA EMBOSSAT AL TERRA ESQUERDAT I PERD UN TORN.");
            } else {
                partida.anadirEvento(p.getNombre().toUpperCase() + " PASSA PEL TERRA ESQUERDAT SENSE PROBLEMES PERQUÈ NO PORTA PES.");
            }
        }
    }
}
