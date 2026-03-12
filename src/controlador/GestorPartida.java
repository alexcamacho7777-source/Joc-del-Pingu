package controlador;

import java.util.Random;
import model.*;

public class GestorPartida {

    private Partida partida;
    private GestorTablero gestorTablero;
    private GestorJugador gestorJugador;
    private Random random;

    public GestorPartida(){

        partida = new Partida();
        gestorTablero = new GestorTablero();
        gestorJugador = new GestorJugador();
        random = new Random();

    }

    public void nuevaPartida(){

        partida = new Partida();
        System.out.println("Nueva partida creada");

    }

    public void tirarDado(Jugador j, Dado dado){

        int pasos = dado.tirar(random);

        gestorJugador.jugadorSeMueve(j, pasos, partida.getTablero());

    }

    public void siguienteTurno(){

        partida.siguienteTurno();

    }

    public Partida getPartida(){
        return partida;
    }

}