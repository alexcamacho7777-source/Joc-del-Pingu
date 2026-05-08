package model;

/**
 * REPRESENTA L'OBJECTE 'PEIX'.
 * ÈS L'ÍTEM DE NEGOCIACIÓ UTILITZAT PER SUBORNAR LA FOCA O PER PROTEGIR-SE DE L'OS POLAR.
 */
public class Pez extends Item {

    /**
     * CONSTRUCTOR PER DEFINIR UN STACK DE PEIXOS.
     */
    public Pez(int cantidad) {
        super("PEIX", cantidad);
    }
}
