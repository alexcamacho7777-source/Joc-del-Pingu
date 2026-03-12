package model;

import java.util.ArrayList;

public class Inventario {

    private ArrayList<Item> lista;

    public Inventario() {
        lista = new ArrayList<>();
    }

    public ArrayList<Item> getLista() {
        return lista;
    }

    public void setLista(ArrayList<Item> lista) {
        this.lista = lista;
    }
}