package model;

public class Pinguino extends Jugador {

    private Inventario inventario;

    public Pinguino(String nombre, String color) {
        super(nombre, color);
        inventario = new Inventario();
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void añadirItem(Item i) {
        inventario.getLista().add(i);
    }

    public void quitarItem(Item i) {
        inventario.getLista().remove(i);
    }
}