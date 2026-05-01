package model;


public class Oso extends Casilla {


    public Oso(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (jugador instanceof Pinguino p) {
            Item pez = p.getInv().getItem(Pez.class);
            if (pez != null && pez.getCantidad() >= 2) {
                // Soborno
                pez.setCantidad(pez.getCantidad() - 2);
                if (pez.getCantidad() <= 0) p.getInv().quitarItem(pez);
                partida.anadirEvento(p.getNombre() + " ha subornat l'os amb 2 peixos.");
            } else {
                p.setPosicion(0);
                partida.anadirEvento(p.getNombre() + " no té prou peixos (necessita 2) i torna a l'inici.");
            }
        }
    }
}
