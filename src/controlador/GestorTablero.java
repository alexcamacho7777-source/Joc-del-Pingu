package controlador;

import model.*;

/**
 * Controlador que gestiona las acciones sobre las casillas del tablero.
 */
public class GestorTablero {

    /**
     * Ejecuta la acción de la casilla en la que ha caído el jugador.
     * @param partida partida en curso
     * @param j       jugador que ha caído en la casilla (puede ser null si es Foca)
     * @param c       casilla en la que ha caído
     */
    public void ejecutarCasilla(Partida partida, Jugador j, Casilla c) {
        if (j != null) c.realizarAccion(partida, j);
    }

    /**
     * Comprueba si es el turno del jugador indicado.
     * @param partida partida en curso
     * @return true si la partida no ha finalizado y hay jugadores
     */
    public boolean comprobarTurno(Partida partida) {
        return !partida.isFinalizada() && !partida.getJugadores().isEmpty();
    }
}
