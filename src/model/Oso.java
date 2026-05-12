package model;

/**
 * REPRESENTA UNA CASELLA DE TIPUS 'OS'.
 * ÈS UNA CASELLA DE PERILL ON UN OS POLAR ESPANTA ELS JUGADORS, FENT-LOS RETROCEDIR.
 */
public class Oso extends Casilla {

    /**
     * CONSTRUCTOR PER DEFINIR LA POSICIÓ DE LA CASELLA DE L'OS.
     */
    public Oso(int posicion) {
        super(posicion);
    }

    /**
     * EXECUTA L'ACCIÓ DEL PERILL DE L'OS.
     * PER ALS JUGADORS IA EL RETROCÉS ÉS IMMEDIAT; PER ALS HUMANS ES GESTIONA VISUALMENT.
     */
    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {
        if (jugador instanceof Pinguino p) {
            Item pez = p.getInv().getItem(Pez.class);
            if (pez != null) {
                // El jugador té un peix, pot subornar l'ós
                pez.setCantidad(pez.getCantidad() - 1);
                if (pez.getCantidad() <= 0) p.getInv().quitarItem(pez); // Consumir 1 peix correctament
                partida.anadirEvento("🦴 " + p.getNombre().toUpperCase() + " HA SUBORNAT L'ÓS AMB UN PEIX I SE SALVA!");
            } else {
                // No té peix, torna a l'inici
                p.setPosicion(0);
                partida.anadirEvento("🐻 " + p.getNombre().toUpperCase() + " HA ESTAT ATACAT PER L'ÓS I TORNA A L'INICI!");
            }
        } else if (jugador instanceof Foca f) {
            // La foca ara també és espantada per l'ós i torna a l'inici
            f.setPosicion(0);
            partida.anadirEvento("🐻 LA FOCA HA ESTAT ESPANTADA PER L'ÓS I TORNA A L'INICI!");
        }
    }
}
