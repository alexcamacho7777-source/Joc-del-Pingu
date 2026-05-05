package model;


public class Oso extends Casilla {


    public Oso(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        // La lógica de retorno al inicio se gestiona ahora en PantallaJuego 
        // para permitir al jugador elegir si usa un pez.
        // Si el jugador es IA, podemos mantener una lógica simple aquí.
        if (jugador instanceof Pinguino p && p.isEsIA()) {
             p.setPosicion(0);
             partida.anadirEvento(p.getNombre() + " (IA) ha caigut en l'OS i torna a l'inici.");
        }
    }
}
