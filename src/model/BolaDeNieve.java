package model;

/**
 * Ítem: Bola de nieve. Se usa para hacer retroceder a otros jugadores.
 */
public class BolaDeNieve extends Item {

    /**
     * Constructor de BolaDenieve.
     * @param cantidad cantidad de bolas de nieve
     */
    public BolaDeNieve(int cantidad) {
        super("Bola de Nieve", cantidad);
    }
}