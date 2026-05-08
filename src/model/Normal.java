package model;

/**
 * REPRESENTA UNA CASELLA ESTÀNDARD O 'NORMAL'.
 * NO TÉ CAP EFECTE ESPECIAL NI MODIFICA L'ESTAT DEL JUGADOR EN CAURE-HI.
 */
public class Normal extends Casilla {

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DE LA CASELLA NORMAL.
     */
    public Normal(int posicion) {
        super(posicion);
    }

    /**
     * L'ACCIÓ D'AQUESTA CASELLA ÉS NUL·LA, JA QUE NO TÉ EFECTES.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        // NO ES REALITZA CAP ACCIÓ ESPECÍFICA
    }
}
