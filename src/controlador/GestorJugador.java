package controlador;

import model.*;

/**
 * CONTROLADOR QUE GESTIONA LES ACCIONS I INTERACCIONS ESPECÍFIQUES DELS JUGADORS.
 * INCLOU LA LÒGICA DE COMBAT (GUERRA DE BOLES) I LA INTERACCIÓ AMB LA FOCA.
 */
public class GestorJugador {

    /**
     * ACTUALITZA EL NOM D'UN JUGADOR I EL MOU SEGONS LA TIRADA DEL DAU.
     * ASSEGURA QUE NO SE SUPERI EL LÍMIT DEL TAULELL.
     */
    public void jugadorSetNuevo(Jugador j, int pase, String nombre, Tablero tablero) {
        j.setNombre(nombre);
        j.moverPosicion(pase);
        int max = tablero.getTotalCasillas() - 1;
        if (j.getPosicion() > max) {
            j.setPosicion(max);
        }
    }

    /**
     * GESTIONA EL COMBAT ENTRE DOS PINGÜINS (GUERRA DE BOLES DE NEU).
     * EL JUGADOR AMB MÉS BOLES GUANYA I AVANÇA LA DIFERÈNCIA DE PROJECTILS.
     * TOTS DOS JUGADORS CONSUMEIXEN TOTES LES SEVES BOLES DURANT L'ENFRONTAMENT.
     */
    public void pinguinoGuerraQuema(Pinguino p1, Pinguino p2) {
        Item b1item = p1.getInv().getItem(BolaDeNieve.class);
        Item b2item = p2.getInv().getItem(BolaDeNieve.class);

        int b1 = 0;
        int b2 = 0;

        // OBTENCIÓ DE QUANTITATS I CONSUM TOTAL DE PROJECTILS
        if (b1item != null) {
            b1 = b1item.getCantidad();
            p1.getInv().quitarItem(b1item);
        }
        
        if (b2item != null) {
            b2 = b2item.getCantidad();
            p2.getInv().quitarItem(b2item);
        }

        int diferencia = b1 - b2;
        
        // DETERMINACIÓ DEL GUANYADOR I APLICACIÓ DE L'AVANÇ
        if (diferencia > 0) {
            p1.moverPosicion(diferencia);
            if (p1.getPosicion() > 49) {
                p1.setPosicion(49);
            }
        } else if (diferencia < 0) {
            p2.moverPosicion(-diferencia);
            if (p2.getPosicion() > 49) {
                p2.setPosicion(49);
            }
        }
    }

    /**
     * GESTIONA LA INTERACCIÓ D'UN PINGÜÍ AMB LA FOCA.
     * SI EL JUGADOR TÉ UN PEIX, SUBORNA LA FOCA I LA BLOQUEJA DURANT 2 TORNS.
     */
    public void focaInteraccion(Pinguino p, Foca foca) {
        Item pez = p.getInv().getItem(Pez.class);
        if (pez != null) {
            pez.setCantidad(pez.getCantidad() - 1);
            if (pez.getCantidad() <= 0) {
                p.getInv().quitarItem(pez);
            }
            foca.activarSoborno();
        }
    }
}
