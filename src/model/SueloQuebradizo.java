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
            partida.anadirEvento(p.getNombre() + " té més de 5 objectes, cau al buit i torna a l'inici.");
        } else if (total > 0) {
            partida.setJugadorPierdeTurno(p);
            partida.anadirEvento(p.getNombre() + " té objectes, perd un torn al terra esquerdat.");
        } else {
            partida.anadirEvento(p.getNombre() + " no porta objectes, passa el terra esquerdat sense problemes.");
        }
    }
}
