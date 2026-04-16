package model;

import java.util.ArrayList;
import java.util.List;

public class Inventario {

    private List<Item> lista;

    public static final int MAX_DADOS = 3;
    public static final int MAX_PECES = 5;
    public static final int MAX_BOLAS = 6;

    public Inventario() {
        this.lista = new ArrayList<>();
    }

    public List<Item> getLista() { return lista; }
    public void setLista(List<Item> lista) { this.lista = lista; }


    public void anadirItem(Item item) {
        if (item instanceof Dado) {
            if (getCantidadDe(Dado.class) + item.getCantidad() <= MAX_DADOS) {
                lista.add(item);
            }
        } else if (item instanceof Pez) {
            if (getCantidadDe(Pez.class) + item.getCantidad() <= MAX_PECES) {
                lista.add(item);
            }
        } else if (item instanceof BolaDeNieve) {
            if (getCantidadDe(BolaDeNieve.class) + item.getCantidad() <= MAX_BOLAS) {
                lista.add(item);
            }
        }
    }


    public void quitarItem(Item item) {
        lista.remove(item);
    }

    public Item getItem(Class<? extends Item> tipo) {
        for (Item i : lista) {
            if (tipo.isInstance(i)) return i;
        }
        return null;
    }

    private int getCantidadDe(Class<? extends Item> tipo) {
        int total = 0;
        for (Item i : lista) {
            if (tipo.isInstance(i)) total += i.getCantidad();
        }
        return total;
    }

    public int totalItems() {
        int total = 0;
        for (Item i : lista) total += i.getCantidad();
        return total;
    }

    public void quitarItemAleatorio(java.util.Random r) {
        if (!lista.isEmpty()) {
            lista.remove(r.nextInt(lista.size()));
        }
    }

    @Override
    public String toString() {
        return lista.toString();
    }
}
