package model;

public class Pinguino extends Jugador {

    private Inventario inv;

    public Pinguino(String nombre, String color) {
        super(nombre, color);
        this.inv = new Inventario();
    }

    public Inventario getInv() {
        return inv;
    }

    public void setInv(Inventario inv) {
        this.inv = inv;
    }

    public void gestionarBatalla(Pinguino p) {
        System.out.println(getNombre() + " lucha contra " + p.getNombre());
    }

    public void usarItem(Item i) {
        System.out.println(getNombre() + " usa el item " + i.getNombre());
    }

    public void anadirItem(Item i) {
        inv.anadirItem(i);
    }

    public void quitarItem(Item i) {
        inv.quitarItem(i);
    }
}