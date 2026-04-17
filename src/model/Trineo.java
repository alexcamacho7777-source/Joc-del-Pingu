package model;

public class Trineo extends Casilla {

 
    public Trineo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        Tablero tablero = partida.getTablero();
        int siguienteTrineo = tablero.buscarSiguienteTrineo(getPosicion());
        if (siguienteTrineo != -1) {
            jugador.setPosicion(siguienteTrineo);
            partida.anadirEvento(jugador.getNombre() + " ha agafat un trineu i avança a la posició " + siguienteTrineo + ".");
        } else {
            partida.anadirEvento(jugador.getNombre() + " ja està a l'últim trineu, no es mou.");
        }

        
        System.out.println("No hi ha més trineus, no passa res.");
    }
}
