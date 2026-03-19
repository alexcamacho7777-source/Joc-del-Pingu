package model;

/**
 * Casilla de Suelo Cuarteado (Tierra Trencadís):
 * - Más de 5 objetos → cae y vuelve al inicio.
 * - Hasta 5 objetos → pierde un turno.
 * - Sin objetos → pasa sin penalización.
 */
public class SueloQuebradizo extends Casilla {

    /**
     * Constructor de SueloQuebradizo.
     * @param posicion índice de la casilla
     */
    public SueloQuebradizo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (!(jugador instanceof Pinguino p)) return;

        int total = p.getInv().totalItems();

        if (total > 5) {
            p.setPosicion(0);
            partida.anadirEvento(p.getNombre() + " tiene más de 5 objetos, cae al vacío y vuelve al inicio.");
        } else if (total > 0) {
            partida.setJugadorPierdeTurno(p);
            partida.anadirEvento(p.getNombre() + " tiene objetos, pierde un turno en el suelo cuarteado.");
        } else {
            partida.anadirEvento(p.getNombre() + " no lleva objetos, pasa el suelo cuarteado sin problemas.");
        }
    }
}
