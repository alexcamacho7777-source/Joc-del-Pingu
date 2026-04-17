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
        int evento = r.nextInt(10); // Tornem al rang original de 0 a 9

        if (evento == 0) {
            p.getInv().anadirItem(new Dado("Dau Ràpid", 1, 5, 10));
            eventos = "✨ " + p.getNombre() + " ha trobat un cristall de gel! Obté un Dau Ràpid.";
        } else if (evento <= 3) {
            p.getInv().anadirItem(new Dado("Dau Lent", 1, 1, 3));
            eventos = "🧊 " + p.getNombre() + " camina sobre gel dens. Obté un Dau Lent.";
        } else if (evento <= 5) {
            int cantidad = 1 + r.nextInt(3);
            p.getInv().anadirItem(new BolaDeNieve(cantidad));
            eventos = "❄️ " + p.getNombre() + " ha fabricat " + cantidad + " boles de neu.";
        } else if (evento <= 7) {
            p.getInv().anadirItem(new Pez(1));
            eventos = "🐟 " + p.getNombre() + " ha pescat un peix fresc!";
        } else if (evento == 8) {
            partida.setJugadorPierdeTurno(p);
            eventos = "😴 " + p.getNombre() + " es queda cansat i perd un torn.";
        } else {
            p.getInv().quitarItemAleatorio(r);
            eventos = "🦊 Un guineu àrtica ha robat un objecte de l'inventari de " + p.getNombre() + "!";
        }

        partida.anadirEvento(eventos);
    }
}
