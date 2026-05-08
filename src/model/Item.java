package model;

/**
 * CLASSE ABSTRACTA QUE REPRESENTA UN OBJECTE O ÍTEM DE L'INVENTARI.
 * DEFINEIX EL NOM DE L'OBJECTE I LA QUANTITAT D'UNITATS DISPONIBLES.
 */
public abstract class Item {

    private String nombre;
    private int cantidad;

    /**
     * CONSTRUCTOR PER DEFINIR UN NOU ÍTEM AMB EL SEU NOM I QUANTITAT INICIAL.
     */
    public Item(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    // GETTERS I SETTERS
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    @Override
    public String toString() {
        return nombre.toUpperCase() + " X" + cantidad;
    }
}
