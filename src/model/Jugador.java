package model;

public abstract class Jugador {

    private int posicion;
    private String nombre;
    private String color;
    private boolean esIA;
    private Inventario inv;

    public Jugador(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.posicion = 0;
        this.esIA = false;
        this.inv = new Inventario();
    }

    public boolean isEsIA() { return esIA; }
    public void setEsIA(boolean esIA) { this.esIA = esIA; }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Inventario getInv() { return inv; }
    public void setInv(Inventario inv) { this.inv = inv; }

    public void moverPosicion(int p) {
        this.posicion += p;
        if (this.posicion < 0) this.posicion = 0;
    }

    @Override
    public String toString() {
        return nombre + " (" + color + ") - Pos: " + posicion;
    }
}
