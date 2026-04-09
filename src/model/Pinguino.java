package model;


public class Pinguino extends Jugador {

    private Inventario inv;

    public Pinguino(String nombre, String color) {
        super(nombre, color);
        this.inv = new Inventario();
    }

    public Pinguino(String nombre, String color, int posicion, Inventario inv) {
        super(nombre, color);
        this.setPosicion(posicion);
        this.inv = inv;
    }

    public Inventario getInv() { return inv; }
    public void setInv(Inventario inv) { this.inv = inv; }

    public Item getPersonaSatisfe(Pinguino p) {
        return null;
    }


    public void anadirItem(Item i) {
        inv.anadirItem(i);
    }

    public void quitarItem(Item i) {
        inv.quitarItem(i);
    }

    @Override
    public String toString() {
        return super.toString() + " | Inventario: " + inv;
    }
}
