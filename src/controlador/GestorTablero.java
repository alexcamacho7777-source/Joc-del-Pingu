package controlador;

import model.*;


public class GestorTablero {

   
    public void ejecutarCasilla(Partida partida, Jugador j, Casilla c) {
        if (j != null) c.realizarAccion(partida, j);
    }

  
     
    public boolean comprobarTurno(Partida partida) {
        return !partida.isFinalizada() && !partida.getJugadores().isEmpty();
    }
}
