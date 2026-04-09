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
            p.getInv().anadirItem(new Dado("Dado Rápido", 1, 5, 10));
            eventos = p.getNombre() + " obtiene un Dado Rápido (5-10).";
        } else if (evento <= 3) {
            // Dado lento
            p.getInv().anadirItem(new Dado("Dado Lento", 1, 1, 3));
            eventos = p.getNombre() + " obtiene un Dado Lento (1-3).";
        } else if (evento <= 5) {
            // Bolas de nieve
            int cantidad = 1 + r.nextInt(3);
            p.getInv().anadirItem(new BolaDeNieve(cantidad));
            eventos = p.getNombre() + " obtiene " + cantidad + " bola(s) de nieve.";
        } else if (evento <= 7) {
            // Pez
            p.getInv().anadirItem(new Pez(1));
            eventos = p.getNombre() + " obtiene un pez.";
        } else if (evento == 8) {
            // Perder un turno
            p.setPosicion(p.getPosicion()); 
            eventos = p.getNombre() + " pierde un turno.";
            partida.setJugadorPierdeTurno(p);
        } else {
            // Perder un objeto aleatorio
            p.getInv().quitarItemAleatorio(r);
            eventos = p.getNombre() + " pierde un objeto aleatorio del inventario.";
        }

        partida.anadirEvento(eventos);
    }
}
