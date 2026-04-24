package model;


public class Pinguino extends Jugador {

    public Pinguino(String nombre, String color) {
        super(nombre, color);
    }

    public Pinguino(String nombre, String color, int posicion, Inventario inv) {
        super(nombre, color);
        this.setPosicion(posicion);
        this.setInv(inv);
    }

    public Item getPersonaSatisfe(Pinguino p) {
        return null;
    }


    public void anadirItem(Item i) {
        getInv().anadirItem(i);
    }

    public void quitarItem(Item i) {
        getInv().quitarItem(i);
    }

    @Override
    public String toString() {
        return super.toString() + " | Inventario: " + getInv();
    }
}
