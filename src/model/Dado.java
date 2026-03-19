package model;

import java.util.Random;

/**
 * Ítem: Dado. Puede ser normal, rápido (5-10) o lento (1-3).
 */
public class Dado extends Item {

    /** Valor mínimo del dado. */
    private int min;

    /** Valor máximo del dado. */
    private int max;

    /**
     * Constructor de Dado.
     * @param nombre   nombre descriptivo del dado
     * @param cantidad cantidad de dados
     * @param min      valor mínimo al tirar
     * @param max      valor máximo al tirar
     */
    public Dado(String nombre, int cantidad, int min, int max) {
        super(nombre, cantidad);
        this.min = min;
        this.max = max;
    }

    public int getMin() { return min; }
    public void setMin(int min) { this.min = min; }

    public int getMax() { return max; }
    public void setMax(int max) { this.max = max; }

    /**
     * Tira el dado y devuelve un valor aleatorio entre min y max.
     * @param r instancia de Random
     * @return resultado del lanzamiento
     */
    public int tirar(Random r) {
        return min + r.nextInt(max - min + 1);
    }
}
