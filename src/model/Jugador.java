package model;

public abstract class Jugador {

    protected int posicion;
    protected String nombre;
    protected String color;

    public Jugador(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.posicion = 0;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getColor() {
        return color;
    }

    public void moverPosicion(int pasos) {
        posicion += pasos;
    }
}