package model;

/**
 * REPRESENTA UNA CASELLA DE TIPUS 'ESDEVENIMENT' O 'SORPRESA'.
 * QUAN UN JUGADOR HI CAU, S'ACTIVA LA RULETA D'ESDEVENIMENTS ALEATORIS.
 */
public class Evento extends Casilla {

    private String eventos;

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DE LA CASELLA D'ESDEVENIMENT.
     */
    public Evento(int posicion) {
        super(posicion);
        this.eventos = "";
    }

    public String getEventos() { return eventos; }
    public void setEventos(String eventos) { this.eventos = eventos; }

    /**
     * REGISTRA L'ENTRADA DEL JUGADOR A LA CASELLA SORPRESA.
     * LA LÒGICA ESPECÍFICA DEL PREMI S'EXECUTA A TRAVÉS DE LA RULETA VISUAL.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        eventos = jugador.getNombre().toUpperCase() + " HA CAIGUT EN UNA CASELLA SORPRESA!";
        partida.anadirEvento(eventos);
    }
}
