package model;

public class SueloQuebradizo extends Casilla {

    public SueloQuebradizo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        System.out.println("El suelo está quebradizo.");

    }
}