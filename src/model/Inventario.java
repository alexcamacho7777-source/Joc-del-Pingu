package model;

import java.util.ArrayList;
import java.util.List;

/**
 * GESTIONA LA COL·LECCIÓ D'OBJECTES (INVENTARI) D'UN JUGADOR.
 * Aquesta classe controla l'emmagatzematge d'ítems que els pinguins recullen 
 * durant la partida. Implementa una lògica de limitació de capacitat (stock) 
 * segons el tipus d'objecte per garantir l'equilibri del joc.
 * 
 * @author Alex Camacho
 * @version 1.3
 */
public class Inventario {

    // Llista interna que emmagatzema els ítems (stacks)
    private List<Item> lista;

    // LÍMITS DE CAPACITAT PER TIPUS D'OBJECTE (Regles del joc)
    public static final int MAX_DADOS = 3;  // Màxim 3 daus especials en total
    public static final int MAX_PECES = 2;  // Màxim 2 peixos (usats per subornar la foca)
    public static final int MAX_BOLAS = 6;  // Màxim 6 boles de neu (per a les guerres)

    /**
     * CONSTRUCTOR PER INICIALITZAR UN INVENTARI BUIT.
     */
    public Inventario() {
        this.lista = new ArrayList<>();
    }

    // ── GETTERS I SETTERS ───────────────────────────────────────────────────
    
    /** @return La llista actual d'ítems. */
    public List<Item> getLista() { return lista; }
    /** @param lista Substitueix tot l'inventari (per a càrregues). */
    public void setLista(List<Item> lista) { this.lista = lista; }

    /**
     * AFEGEIX UN ÍTEM A L'INVENTARI RESPECTANT ELS LÍMITS DE CAPACITAT ESPECÍFICS.
     * Si l'ítem supera el límit permès per la seva categoria, no s'afegeix.
     * @param item L'objecte a afegir.
     */
    public void anadirItem(Item item) {
        if (item instanceof Dado) {
            // Verificació per a daus (Ràpid o Lent)
            if (getCantidadDe(Dado.class) + item.getCantidad() <= MAX_DADOS) {
                lista.add(item);
            }
        } else if (item instanceof Pez) {
            // Verificació per a peixos
            if (getCantidadDe(Pez.class) + item.getCantidad() <= MAX_PECES) {
                lista.add(item);
            }
        } else if (item instanceof BolaDeNieve) {
            // Verificació per a boles de neu
            if (getCantidadDe(BolaDeNieve.class) + item.getCantidad() <= MAX_BOLAS) {
                lista.add(item);
            }
        }
    }

    /**
     * ELIMINA UN OBJECTE COMPLET (STACK) DE LA LLISTA.
     * @param item L'item a treure.
     */
    public void quitarItem(Item item) {
        lista.remove(item);
    }

    /**
     * RETORNA EL PRIMER ÍTEM DE LA LLISTA QUE COINCIDEIXI AMB EL TIPUS INDICAT.
     * Útil per buscar si un jugador té un tipus d'objecte concret.
     * @param tipo Classe de l'ítem (ex: Pez.class).
     * @return L'objecte trobat o null si no n'hi ha cap.
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
     * Suma les quantitats internes de cada stack del mateix tipus.
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
     * @return Suma de totes les quantitats.
     */
    public int totalItems() {
        int total = 0;
        for (Item i : lista) {
            total += i.getCantidad();
        }
        return total;
    }

    /**
     * COMPTA ELS ÍTEMS SEGONS UNA CADENA DE TEXT.
     * Aquest mètode és el que utilitza la Vista per actualitzar els indicadors de la UI.
     * @param tipo Identificador del tipus ("Peces", "BolaNieve", "DadoRapido", "DadoLento").
     * @return La quantitat total trobada.
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
                // Filtre específic per tipus de dau mitjançant paraules clau
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
     * S'utilitza quan la foca roba la meitat de l'inventari o quan es perd per un esdeveniment.
     * @param r Generador aleatori.
     * @return L'ítem que ha estat reduït (o eliminat).
     */
    public Item quitarUnidadAleatoria(java.util.Random r) {
        Item res = null;
        if (!lista.isEmpty()) {
            res = lista.get(r.nextInt(lista.size()));
            res.setCantidad(res.getCantidad() - 1);
            // Si el stack queda buit, l'eliminem de la llista
            if (res.getCantidad() <= 0) {
                lista.remove(res);
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "INVENTARI" + lista.toString();
    }
}
