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

    public String getNombre() {
    	return nombre;
    }

    public String getColor() {
        return color;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void moverPosicion(int p) {
        this.posicion += p;
    }
}