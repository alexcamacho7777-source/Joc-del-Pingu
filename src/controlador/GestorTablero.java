package controlador;

import model.*;

/**
 * CONTROLADOR QUE GESTIONA LES ACCIONS SOBRE LES CASELLES DEL TAULELL.
 * ACTUA COM A INTERMEDIARI PER EXECUTAR LA LÒGICA DE CADA POSICIÓ DEL TAULELL.
 */
public class GestorTablero {

    /**
     * EXECUTA L'ACCIÓ ESPECÍFICA DE LA CASELLA ON HA CAIGUT EL JUGADOR.
     * @param partida CONTEXT DE LA PARTIDA ACTUAL.
     * @param j JUGADOR QUE REALITZA L'ACCIÓ.
     * @param c CASELLA D'ATERRIZATGE.
     */
    public void ejecutarCasilla(Partida partida, Jugador j, Casilla c) {
        if (j != null && c != null) {
            c.realizarAccion(partida, j);
        }
    }

    /**
     * COMPROVA SI LA PARTIDA ESTÀ EN CONDICIONS DE CONTINUAR ELS TORNS.
     * @param partida LA PARTIDA A VALIDAR.
     * @return CERT SI NO HA ACABAT I HI HA JUGADORS ACTIUS.
     */
    public boolean comprobarTurno(Partida partida) {
        boolean valid = false;
        if (partida != null) {
            valid = !partida.isFinalizada() && !partida.getJugadores().isEmpty();
        }
        return valid;
    }
}
