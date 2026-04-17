package model;


public class Agujero extends Casilla {

    public Agujero(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(getPosicion());
        jugador.setPosicion(anteriorAgujero);
        partida.anadirEvento(jugador.getNombre() + " ha caigut en un forat i va a la posició " + anteriorAgujero + ".");
    }
}
