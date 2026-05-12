package controlador;

import model.*;

/**
 * CONTROLADOR QUE GESTIONA LES ACCIONS I INTERACCIONS ESPECÍFIQUES DELS JUGADORS.
 * Aquesta classe conté la lògica de "combat" i les regles de contacte entre 
 * els diferents personatges del joc. S'encarrega de resoldre els enfrontaments 
 * quan dos jugadors cauen en la mateixa casella o quan algú es troba amb la foca.
 * 
 * @author Alex Camacho
 * @version 1.4
 */
public class GestorJugador {

    /**
     * ACTUALITZA L'ESTAT D'UN JUGADOR (NOM I POSICIÓ).
     * @param j El jugador a modificar.
     * @param pase El número de caselles a moure (tirada de dau).
     * @param nombre El nom (identificador) del jugador.
     * @param tablero El taulell per conèixer els límits de les caselles.
     */
    public void jugadorSetNuevo(Jugador j, int pase, String nombre, Tablero tablero) {
        j.setNombre(nombre);
        j.moverPosicion(pase);
        // Protecció de rang: assegurem que el jugador no surti del taulell
        int max = tablero.getTotalCasillas() - 1;
        if (j.getPosicion() > max) {
            j.setPosicion(max);
        }
    }

    /**
     * GESTIONA EL COMBAT ENTRE DOS PINGÜINS (GUERRA DE BOLES DE NEU).
     * Regla: El jugador que té més boles de neu guanya l'enfrontament. 
     * El guanyador avança un número de caselles igual a la diferència de boles.
     * Independentment del resultat, tots dos jugadors perden tota la seva munició.
     * @param p1 El pinguí que acaba d'arribar o que ja hi era.
     * @param p2 L'altre pinguí involucrat en el combat.
     */
    public void pinguinoGuerraQuema(Pinguino p1, Pinguino p2) {
        // Obtenim els ítems de boles de neu de cada inventari
        Item b1item = p1.getInv().getItem(BolaDeNieve.class);
        Item b2item = p2.getInv().getItem(BolaDeNieve.class);

        int b1 = 0;
        int b2 = 0;

        // OBTENCIÓ DE QUANTITATS I CONSUM TOTAL DE PROJECTILS (Es buida el stack)
        if (b1item != null) {
            b1 = b1item.getCantidad();
            p1.getInv().quitarItem(b1item);
        }
        
        if (b2item != null) {
            b2 = b2item.getCantidad();
            p2.getInv().quitarItem(b2item);
        }

        // Calculem el resultat del combat
        int diferencia = b1 - b2;
        
        // DETERMINACIÓ DEL GUANYADOR I APLICACIÓ DE L'AVANÇ EXTRA
        if (diferencia > 0) {
            // P1 ha guanyat (tenia més boles)
            p1.moverPosicion(diferencia);
            if (p1.getPosicion() > 49) {
                p1.setPosicion(49);
            }
        } else if (diferencia < 0) {
            // P2 ha guanyat (la diferència era negativa, per tant b2 > b1)
            p2.moverPosicion(-diferencia);
            if (p2.getPosicion() > 49) {
                p2.setPosicion(49);
            }
        }
        // En cas d'empat (diferencia == 0), cap dels dos es mou.
    }

    /**
     * GESTIONA LA INTERACCIÓ D'UN PINGÜÍ AMB LA FOCA ROBOT.
     * Regla de suborn: si el jugador utilitza un peix, la foca queda bloquejada 
     * i l'usuari no és colpejat cap enrere.
     * @param p El pinguí que interactua.
     * @param foca La foca robot (NPC).
     */
    public void focaInteraccion(Pinguino p, Foca foca) {
        Item pez = p.getInv().getItem(Pez.class);
        if (pez != null && pez.getCantidad() > 0) {
            // Consumim una unitat de peix per al suborn
            pez.setCantidad(pez.getCantidad() - 1);
            // Si era l'últim peix, l'eliminem de l'inventari
            if (pez.getCantidad() <= 0) {
                p.getInv().quitarItem(pez);
            }
            // Activem l'estat de suborn a la foca (bloqueig de moviment)
            foca.activarSoborno();
        }
    }
}
