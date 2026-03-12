package model;

public class Agujero extends Casilla {

    public Agujero(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        System.out.println("El jugador cae en un agujero.");

        int nuevaPos = jugador.getPosicion() - 3;

        if(nuevaPos < 0)
            nuevaPos = 0;

        jugador.setPosicion(nuevaPos);

    }
}