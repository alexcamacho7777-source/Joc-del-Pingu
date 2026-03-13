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

    	Random ran = new Random();
		String evento = eventos[ran.nextInt(6)];

		if (evento.equals("pez")) {

		} else if (evento.equals("bolas")) {

		} else if (evento.equals("rapido")) {

		} else if (evento.equals("lento")) {

		} else if (evento.equals("pierdeTurno")) {

		} else if (evento.equals("pierdeItem")) {

		} else if (evento.equals("motos")) {

		}
	}
}