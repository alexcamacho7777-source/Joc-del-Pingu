package model;

public class Pez extends Item {

    public Pez(String nombre, int cantidad) {
        super(nombre, cantidad);
    }

    public String getNombre() {
        return super.getNombre();
    }

    public void setNombre(String nombre) {
        super.setNombre(nombre);
    }

    public int getCantidad() {
        return super.getCantidad();
    }

    public void setCantidad(int cantidad) {
        super.setCantidad(cantidad);
    }
}