package model;

import java.util.ArrayList;
import java.util.List;

/**
 * GESTIONA LA COL·LECCIÓ D'OBJECTES (INVENTARI) D'UN JUGADOR.
 * CONTROLA ELS LÍMITS DE CAPACITAT I LES OPERACIONS D'AFEGIR I ELIMINAR ÍTEMS.
 */
public class Inventario {

    private List<Item> lista;

    // LÍMITS DE CAPACITAT PER TIPUS D'OBJECTE
    public static final int MAX_DADOS = 3;
    public static final int MAX_PECES = 2;
    public static final int MAX_BOLAS = 6;

    /**
     * CONSTRUCTOR PER INICIALITZAR UN INVENTARI BUIT.
     */
    public Inventario() {
        this.lista = new ArrayList<>();
    }

    public List<Item> getLista() { return lista; }
    public void setLista(List<Item> lista) { this.lista = lista; }

    /**
     * AFEGEIX UN ÍTEM A L'INVENTARI RESPECTANT ELS LÍMITS DE CAPACITAT ESPECÍFICS.
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
     * ELIMINA UN OBJECTE COMPLET DE LA LLISTA.
     */
    public void quitarItem(Item item) {
        lista.remove(item);
    }

    /**
     * RETORNA EL PRIMER ÍTEM DE LA LLISTA QUE COINCIDEIXI AMB EL TIPUS INDICAT.
     */
    public Item getItem(Class<? extends Item> tipo) {
        Item res = null;
        boolean trobat = false;
        for (int i = 0; i < lista.size() && !trobat; i++) {
            if (tipo.isInstance(lista.get(i))) {
                res = lista.get(i);
                trobat = true;
            }
        }
        return res;
    }

    /**
     * CALCULA LA QUANTITAT TOTAL D'UNITATS D'UN TIPUS D'ÍTEM DETERMINAT.
     */
    private int getCantidadDe(Class<? extends Item> tipo) {
        int total = 0;
        for (Item i : lista) {
            if (tipo.isInstance(i)) {
                total += i.getCantidad();
            }
        }
        return total;
    }

    /**
     * RETORNA EL NOMBRE TOTAL D'UNITATS DE TOTS ELS ÍTEMS DE L'INVENTARI.
     */
    public int totalItems() {
        int total = 0;
        for (Item i : lista) {
            total += i.getCantidad();
        }
        return total;
    }

    /**
     * COMPTA ELS ÍTEMS SEGONS UNA CADENA DE TEXT (ÚTIL PER A CONSULTES DE LA INTERFÍCIE).
     */
    public int contarItems(String tipo) {
        int total = 0;
        for (Item i : lista) {
            String nomL = (i instanceof Dado) ? ((Dado) i).getNombre().toLowerCase() : "";
            
            if (tipo.equals("Peces") && i instanceof Pez) {
                total += i.getCantidad();
            } else if (tipo.equals("BolaNieve") && i instanceof BolaDeNieve) {
                total += i.getCantidad();
            } else if (i instanceof Dado) {
                if (tipo.contains("Rapido") && (nomL.contains("rapid") || nomL.contains("ràpid") || nomL.contains("rápido"))) {
                    total += i.getCantidad();
                } else if (tipo.contains("Lento") && (nomL.contains("lent") || nomL.contains("lento"))) {
                    total += i.getCantidad();
                }
            }
        }
        return total;
    }

    /**
     * ELIMINA UNA UNITAT D'UN ÍTEM ALEATORI DE L'INVENTARI.
     * SI LA QUANTITAT ARRIBA A ZERO, S'ELIMINA EL 'STACK' DE LA LLISTA.
     */
    public Item quitarUnidadAleatoria(java.util.Random r) {
        Item res = null;
        if (!lista.isEmpty()) {
            res = lista.get(r.nextInt(lista.size()));
            res.setCantidad(res.getCantidad() - 1);
            if (res.getCantidad() <= 0) {
                lista.remove(res);
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return lista.toString();
    }
}
