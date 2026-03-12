package controlador;

import model.*;

public class GestorTablero {

    public void ejecutarCasilla(Partida partida, Pinguino p, Casilla c){

        c.realizarAccion(partida, p);

    }

    public void comprobarFinTurno(Partida partida){

        if(partida.getJugadorActual().getPosicion() >= 
           partida.getTablero().getCasillas().size() - 1){

            System.out.println("Un jugador ha llegado al final");

        }

    }

}