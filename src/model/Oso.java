package model;

/**
 * REPRESENTA UNA CASELLA DE TIPUS 'OS'.
 * ÈS UNA CASELLA DE PERILL ON UN OS POLAR ESPANTA ELS JUGADORS, FENT-LOS RETROCEDIR.
 */
public class Oso extends Casilla {

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DE LA CASELLA DE L'OS.
     */
    public Oso(int posicion) {
        super(posicion);
    }

    /**
     * EXECUTA L'ACCIÓ DEL PERILL DE L'OS.
     * PER ALS JUGADORS IA EL RETROCÉS ÉS IMMEDIAT; PER ALS HUMANS ES GESTIONA VISUALMENT.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (jugador instanceof Pinguino p && p.isEsIA()) {
             p.setPosicion(0);
             partida.anadirEvento(p.getNombre().toUpperCase() + " (IA) HA CAIGUT EN L'OS I TORNA A L'INICI.");
        }
    }
}
