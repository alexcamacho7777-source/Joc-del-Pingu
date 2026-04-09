package model;


public class Oso extends Casilla {


    public Oso(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (jugador instanceof Pinguino p) {
            Item pez = p.getInv().getItem(Pez.class);
            if (pez != null) {
                // Soborno
                pez.setCantidad(pez.getCantidad() - 1);
                if (pez.getCantidad() <= 0) p.getInv().quitarItem(pez);
                partida.anadirEvento(p.getNombre() + " ha sobornado al oso con un pez.");
            } else {
                p.setPosicion(0);
                partida.anadirEvento(p.getNombre() + " ha sido capturado por el oso y vuelve al inicio.");
            }
        }
    }
}
