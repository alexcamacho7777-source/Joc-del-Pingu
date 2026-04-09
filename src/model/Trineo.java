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
            partida.anadirEvento(jugador.getNombre() + " ha cogido un trineo y avanza a la posición " + siguienteTrineo + ".");
        } else {
            partida.anadirEvento(jugador.getNombre() + " ya está en el último trineo, no se mueve.");
        }

        
        System.out.println("No hay más trineos, no pasa nada.");
    }
}
