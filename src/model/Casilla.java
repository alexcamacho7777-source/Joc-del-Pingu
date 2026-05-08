package model;

/**
 * CLASSE ABSTRACTA QUE REPRESENTA UNA CASELLA DEL TAULELL.
 * TOTA CASELLA TÉ UNA POSICIÓ I UNA ACCIÓ ESPECÍFICA QUE S'EXECUTA QUAN UN JUGADOR HI CAU.
 */
public abstract class Casilla {

    private int posicion;

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DE LA CASELLA AL TAULELL.
     */
    public Casilla(int posicion) {
        this.posicion = posicion;
    }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    /**
     * MÈTODE ABSTRACTE QUE CADA TIPUS DE CASELLA HA D'IMPLEMENTAR AMB LA SEVA LÒGICA.
     * @param partida CONTEXT DE LA PARTIDA ACTUAL.
     * @param jugador JUGADOR QUE HA CAIGUT A LA CASELLA.
     */
    public abstract void realizarAccion(Partida partida, Jugador jugador);

    @Override
    public String toString() {
        return getClass().getSimpleName().toUpperCase() + "[" + posicion + "]";
    }
}
