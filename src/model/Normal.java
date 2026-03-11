package model;

public class Normal extends Casilla {

    public Normal(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        System.out.println("Casilla normal. No ocurre nada.");
    }
}