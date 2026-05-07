package model;

import java.util.ArrayList;
import java.util.List;

public class Inventario {

    private List<Item> lista;

    public static final int MAX_DADOS = 3;
    public static final int MAX_PECES = 2;
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

    public int contarItems(String tipo) {
        int total = 0;
        for (Item i : lista) {
            String nombreLower = i instanceof Dado ? ((Dado) i).getNombre().toLowerCase() : "";
            
            if (tipo.equals("Peces") && i instanceof Pez) {
                total += i.getCantidad();
            } else if (tipo.equals("BolaNieve") && i instanceof BolaDeNieve) {
                total += i.getCantidad();
            } else if (i instanceof Dado) {
                // Normalizar búsqueda para ignorar tildes básicas o buscar coincidencias clave
                if (tipo.contains("Rapido") && (nombreLower.contains("rapid") || nombreLower.contains("ràpid") || nombreLower.contains("rápido"))) {
                    total += i.getCantidad();
                } else if (tipo.contains("Lento") && (nombreLower.contains("lent") || nombreLower.contains("lento"))) {
                    total += i.getCantidad();
                }
            }
        }
        return total;
    }

    public Item quitarItemAleatorio(java.util.Random r) {
        if (!lista.isEmpty()) {
            return lista.remove(r.nextInt(lista.size()));
        }
        return null;
    }

    /**
     * Quita UNA unidad de un ítem aleatorio.
     * Si la cantidad llega a 0, elimina el ítem de la lista.
     */
    public Item quitarUnidadAleatoria(java.util.Random r) {
        if (lista.isEmpty()) return null;
        
        Item stack = lista.get(r.nextInt(lista.size()));
        stack.setCantidad(stack.getCantidad() - 1);
        
        if (stack.getCantidad() <= 0) {
            lista.remove(stack);
        }
        return stack;
    }

    @Override
    public String toString() {
        return lista.toString();
    }
}
