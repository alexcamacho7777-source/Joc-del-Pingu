package model;


public class Trineo extends Casilla {

    public Trineo(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        int posActual = jugador.getPosicion();
        Tablero tablero = partida.getTablero();

        for (Casilla c : tablero.getCasillas()) {

            if (c instanceof Trineo && c.getPosicion() > posActual) {

                jugador.setPosicion(c.getPosicion());
                System.out.println(jugador.getNombre() + " avanza hasta el siguiente trineo en la posición " + c.getPosicion());
                return;

            }
        }

        // si no hay más trineos
        System.out.println("No hay más trineos, no pasa nada.");
    }
}