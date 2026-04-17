package model;

import java.util.Random;


public class Evento extends Casilla {

    private String eventos;


    public Evento(int posicion) {
        super(posicion);
        this.eventos = "";
    }

    public String getEventos() { return eventos; }
    public void setEventos(String eventos) { this.eventos = eventos; }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        // La lògica s'ha mogut a la Ruleta a la vista.
        // Aquí només registrem l'esdeveniment sense aplicar premis immediats.
        eventos = jugador.getNombre() + " ha caigut en una casella sorpresa!";
        partida.anadirEvento(eventos);
    }
}
