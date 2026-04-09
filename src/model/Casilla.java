package model;


public abstract class Casilla {

    private int posicion;
    public Casilla(int posicion) {
        this.posicion = posicion;
    }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public abstract void realizarAccion(Partida partida, Jugador jugador);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + posicion + "]";
    }
}
