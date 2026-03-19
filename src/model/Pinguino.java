package model;

/**
 * Representa un jugador humano (pingüino).
 */
public class Pinguino extends Jugador {

    /** Inventario del pingüino. */
    private Inventario inv;

    /**
     * Constructor de Pinguino.
     * @param nombre nombre del jugador
     * @param color  color del pingüino
     */
    public Pinguino(String nombre, String color) {
        super(nombre, color);
        this.inv = new Inventario();
    }

    public Inventario getInv() { return inv; }
    public void setInv(Inventario inv) { this.inv = inv; }

    /**
     * Obtiene un ítem del inventario por tipo.
     * @param tipo clase del ítem
     * @return el ítem o null
     */
    public Item getPersonaSatisfe(Pinguino p) {
        // Alias semántico del enunciado - devuelve el inventario del pingüino
        return null;
    }

    /**
     * Añade un ítem al inventario.
     * @param i ítem a añadir
     */
    public void anadirItem(Item i) {
        inv.anadirItem(i);
    }

    /**
     * Quita un ítem del inventario.
     * @param i ítem a quitar
     */
    public void quitarItem(Item i) {
        inv.quitarItem(i);
    }

    @Override
    public String toString() {
        return super.toString() + " | Inventario: " + inv;
    }
}
