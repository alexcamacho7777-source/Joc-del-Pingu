package model;

import java.util.Random;

/**
 * Casilla de Interrogante (Evento): activa un evento aleatorio.
 * Posibles eventos:
 *   1 - Obtener un pez
 *   2 - Obtener 1-3 bolas de nieve
 *   3 - Obtener un dado rápido (avanza 5-10, probabilidad baja)
 *   4 - Obtener un dado lento (valores 1-3, probabilidad alta)
 *   5 - Perder un turno (nivel intermedio)
 *   6 - Perder un objeto aleatorio del inventario (nivel intermedio)
 */
public class Evento extends Casilla {

    /** Descripción del último evento activado. */
    private String eventos;

    /**
     * Constructor de Evento.
     * @param posicion índice de la casilla
     */
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
        // Pesos: dado rápido tiene probabilidad baja (1/10), dado lento alta (3/10)
        int evento = r.nextInt(10);

        if (evento == 0) {
            // Dado rápido - probabilidad baja (1/10)
            p.getInv().anadirItem(new Dado("Dado Rápido", 1, 5, 10));
            eventos = p.getNombre() + " obtiene un Dado Rápido (5-10).";
        } else if (evento <= 3) {
            // Dado lento - probabilidad alta (3/10)
            p.getInv().anadirItem(new Dado("Dado Lento", 1, 1, 3));
            eventos = p.getNombre() + " obtiene un Dado Lento (1-3).";
        } else if (evento <= 5) {
            // Bolas de nieve (2/10)
            int cantidad = 1 + r.nextInt(3);
            p.getInv().anadirItem(new BolaDeNieve(cantidad));
            eventos = p.getNombre() + " obtiene " + cantidad + " bola(s) de nieve.";
        } else if (evento <= 7) {
            // Pez (2/10)
            p.getInv().anadirItem(new Pez(1));
            eventos = p.getNombre() + " obtiene un pez.";
        } else if (evento == 8) {
            // Perder un turno (1/10)
            p.setPosicion(p.getPosicion()); // la penalización la gestiona GestorPartida
            eventos = p.getNombre() + " pierde un turno.";
            partida.setJugadorPierdeTurno(p);
        } else {
            // Perder un objeto aleatorio (1/10)
            p.getInv().quitarItemAleatorio(r);
            eventos = p.getNombre() + " pierde un objeto aleatorio del inventario.";
        }

        partida.anadirEvento(eventos);
    }
}
