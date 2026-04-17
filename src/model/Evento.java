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
        if (!(jugador instanceof Pinguino p)) return;

        Random r = partida.getRandom();
        int evento = r.nextInt(10);

        if (evento == 0) {
            // Dado rápido
            p.getInv().anadirItem(new Dado("Dau Ràpid", 1, 5, 10));
            eventos = p.getNombre() + " obté un Dau Ràpid (5-10).";
        } else if (evento <= 3) {
            // Dado lento
            p.getInv().anadirItem(new Dado("Dau Lent", 1, 1, 3));
            eventos = p.getNombre() + " obté un Dau Lent (1-3).";
        } else if (evento <= 5) {
            // Bolas de nieve
            int cantidad = 1 + r.nextInt(3);
            p.getInv().anadirItem(new BolaDeNieve(cantidad));
            eventos = p.getNombre() + " obté " + cantidad + " bola(s) de neu.";
        } else if (evento <= 7) {
            // Pez
            p.getInv().anadirItem(new Pez(1));
            eventos = p.getNombre() + " obté un peix.";
        } else if (evento == 8) {
            // Perder un turno
            p.setPosicion(p.getPosicion()); 
            eventos = p.getNombre() + " perd un torn.";
            partida.setJugadorPierdeTurno(p);
        } else {
            // Perder un objeto aleatorio
            p.getInv().quitarItemAleatorio(r);
            eventos = p.getNombre() + " perd un objecte aleatori de l'inventari.";
        }

        partida.anadirEvento(eventos);
    }
}
