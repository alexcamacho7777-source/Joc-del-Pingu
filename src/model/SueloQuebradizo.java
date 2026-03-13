package model;

package model;

public class SueloQuebradizo extends Casilla {

    public SueloQuebradizo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        if (!(jugador instanceof Pinguino)) {
            return;
        }

        Pinguino p = (Pinguino) jugador;

        int objetos = p.getInventario().getLista().size();

        if (objetos > 5) {

            System.out.println(p.getNombre() + " cae en el hielo roto y vuelve al inicio");
            p.setPosicion(0);

        } 
        else if (objetos > 0) {

            System.out.println(p.getNombre() + " pierde un turno por el hielo quebradizo");

            // aquí podríais guardar que pierde turno
            // por ejemplo con una variable en Jugador

        } 
        else {

            System.out.println(p.getNombre() + " cruza el hielo sin problemas");

        }
    }
}