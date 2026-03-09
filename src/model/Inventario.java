package model;

import java.util.ArrayList;

public class Inventario {

    private ArrayList<Item> items;

    public Inventario() {
        items = new ArrayList<>();
    }

    public void anadirItem(Item i) {
        items.add(i);
    }

    public void quitarItem(Item i) {
        items.remove(i);
    }

    public ArrayList<Item> getItems() {
        return items;
    }
}