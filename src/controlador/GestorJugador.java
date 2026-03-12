package controlador;

import model.*;

public class GestorJugador {

    public void jugadorSeMueve(Jugador j, int pasos, Tablero t){

        j.moverPosicion(pasos);

        if(j.getPosicion() >= t.getCasillas().size()){
            j.setPosicion(t.getCasillas().size() - 1);
        }

    }

    public void jugadorFinalizaTurno(Jugador j){
        System.out.println(j.getNombre() + " termina su turno");
    }

    public void pinguinoEvento(Pinguino p){
        System.out.println("Evento para el pingüino " + p.getNombre());
    }

    public void pinguinoGuerra(Pinguino p1, Pinguino p2){
        System.out.println(p1.getNombre() + " lucha contra " + p2.getNombre());
    }

    public void focaInteractua(Pinguino p, Foca f){
        f.golpearJugador(p);
    }

}