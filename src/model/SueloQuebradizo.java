package model;


public class SueloQuebradizo extends Casilla {


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
