package model;

/**
 * Clase abstracta que representa un ítem del inventario.
 */
public abstract class Item {

    /** Nombre del ítem. */
    private String nombre;

    /** Cantidad del ítem. */
    private int cantidad;

    /**
     * Constructor de Item.
     * @param nombre  nombre del ítem
     * @param cantidad cantidad inicial
     */
    public Item(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    @Override
    public String toString() {
        return nombre + " x" + cantidad;
    }
}
