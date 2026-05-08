package model;

/**
 * REPRESENTA L'OBJECTE 'BOLA DE NEU'.
 * S'UTILITZA COM A MUNICCIÓ DURANT ELS COMBATS ENTRE JUGADORS (GUERRA DE BOLES).
 */
public class BolaDeNieve extends Item {

    /**
     * CONSTRUCTOR PER DEFINIR UN STACK DE BOLES DE NEU.
     */
    public BolaDeNieve(int cantidad) {
        super("BOLA DE NEU", cantidad);
    }
}