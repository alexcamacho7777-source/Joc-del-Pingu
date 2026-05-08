package model;

import java.util.Random;

/**
 * REPRESENTA UN OBJECTE DE TIPUS 'DAU' AMB UN RANG ESPECÍFIC.
 * PERMET DEFINIR DAUS ESPECIALS (LENTS O RÀPIDS) AMB LÍMITS DE TIRADA PERSONALITZATS.
 */
public class Dado extends Item {

    private int min;
    private int max;

    /**
     * CONSTRUCTOR PER DEFINIR UN DAU AMB ELS SEUS LÍMITS MÍNIM I MÀXIM.
     */
    public Dado(String nombre, int cantidad, int min, int max) {
        super(nombre, cantidad);
        this.min = min;
        this.max = max;
    }

    // GETTERS I SETTERS
    public int getMin() { return min; }
    public void setMin(int min) { this.min = min; }

    public int getMax() { return max; }
    public void setMax(int max) { this.max = max; }

    /**
     * REALITZA UNA TIRADA ALEATÒRIA DINS DEL RANG [MIN, MAX].
     * @param r GENERADOR ALEATORI DE LA PARTIDA.
     * @return EL RESULTAT DE LA TIRADA.
     */
    public int tirar(Random r) {
        return min + r.nextInt(max - min + 1);
    }
}
