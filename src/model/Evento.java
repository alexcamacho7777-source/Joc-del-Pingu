package model;

import java.util.Random;

public class Evento extends Casilla {

    private String[] eventos;

    public Evento(int posicion) {
        super(posicion);

        eventos = new String[]{
            "Obtienes un pez",
            "Obtienes bolas de nieve",
            "Obtienes dado rapido",
            "Obtienes dado lento"
        };
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        Random r = new Random();

        int e = r.nextInt(eventos.length);

        System.out.println("Evento: " + eventos[e]);

    }
}