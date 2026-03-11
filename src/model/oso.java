package model;

public class Oso extends Casilla {

    public Oso(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        System.out.println("¡Un oso ataca al jugador!");
    }
}