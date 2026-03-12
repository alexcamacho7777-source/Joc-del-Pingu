package model;

public class Trineo extends Casilla {

    public Trineo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        System.out.println("El jugador usa un trineo.");

        jugador.moverPosicion(3);

    }
}