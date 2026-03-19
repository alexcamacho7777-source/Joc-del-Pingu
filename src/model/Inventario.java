package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la lista de ítems de un jugador.
 * Límites: máx 3 dados, máx 2 peces, máx 6 bolas de nieve.
 */
public class Inventario {

    /** Lista de ítems del inventario. */
    private List<Item> lista;

    /** Máximo de dados permitidos. */
    public static final int MAX_DADOS = 3;
    /** Máximo de peces permitidos. */
    public static final int MAX_PECES = 2;
    /** Máximo de bolas de nieve permitidas. */
    public static final int MAX_BOLAS = 6;

    /** Constructor vacío. */
    public Inventario() {
        this.lista = new ArrayList<>();
    }

    public List<Item> getLista() { return lista; }
    public void setLista(List<Item> lista) { this.lista = lista; }

    /**
     * Añade un ítem al inventario si no supera el límite correspondiente.
     * @param item ítem a añadir
     */
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

    /**
     * Quita un ítem del inventario por referencia.
     * @param item ítem a quitar
     */
    public void quitarItem(Item item) {
        lista.remove(item);
    }

    /**
     * Obtiene el primer ítem del tipo indicado, o null si no existe.
     * @param tipo clase del ítem
     * @return el ítem encontrado o null
     */
    public Item getItem(Class<? extends Item> tipo) {
        for (Item i : lista) {
            if (tipo.isInstance(i)) return i;
        }
        return null;
    }

    /**
     * Devuelve la cantidad total de ítems de un tipo concreto.
     */
    private int getCantidadDe(Class<? extends Item> tipo) {
        int total = 0;
        for (Item i : lista) {
            if (tipo.isInstance(i)) total += i.getCantidad();
        }
        return total;
    }

    /**
     * Devuelve el total de ítems en el inventario (suma de cantidades).
     */
    public int totalItems() {
        int total = 0;
        for (Item i : lista) total += i.getCantidad();
        return total;
    }

    /**
     * Quita un ítem aleatorio del inventario.
     * @param r instancia de Random
     */
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
