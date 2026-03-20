package controlador;

import model.*;
import vista.PantallaMenu;
import vista.PantallaPartida;

/**
 * Punt d'entrada principal del Joc del Pingüino.
 * Coordina la inicialització de la partida i el flux del joc.
 * <p>
 * En una implementació JavaFX aquest mètode llançaria la {@code Application}.
 * En mode consola, executa una partida de demostració completa.
 */
public class main {

    /**
     * Mètode principal.
     *
     * @param args arguments de línia de comandes (no usats)
     */
    public static void main(String[] args) {
        Main m = new Main();
        m.jugar();
    }

    /**
     * Inicia i executa una partida de demostració per consola.
     * Crea el controlador principal, afegeix jugadors i simula fins a
     * 200 torns o la fi de la partida.
     */
    public void jugar() {
        // --- Controladors ---
        GestorPartida gestorPartida = new GestorPartida();
        gestorPartida.nuevaPartida(null);

        Partida partida = gestorPartida.getPartidaActual();

        // --- Vistes ---
        PantallaMenu menu = new PantallaMenu(gestorPartida);
        PantallaPartida pantalla = new PantallaPartida(gestorPartida);

        // Mostrar menú
        menu.menu();

        // --- Jugadors ---
        Pinguino j1 = new Pinguino("Jugador 1", "Blau");
        Pinguino j2 = new Pinguino("Jugador 2", "Vermell");
        Pinguino j3 = new Pinguino("Jugador 3", "Verd");
        Foca foca   = new Foca();

        // Ítems inicials d'exemple
        j1.getInv().anadirItem(new BolaDenieve(2));
        j2.getInv().anadirItem(new Pez(1));
        j3.getInv().anadirItem(new Dado("Dado Normal", 1, 1, 6));

        partida.anadirJugador(j1);
        partida.anadirJugador(j2);
        partida.anadirJugador(j3);
        partida.anadirJugador(foca);

        // Iniciar vista de partida
        pantalla.iniciarPartida();

        // --- Bucle de joc ---
        int maxTorns = 200;
        int torn = 0;
        while (!partida.isFinalizada() && torn < maxTorns) {
            pantalla.botonTirarDado();
            torn++;
        }

        // --- Resultat final ---
        System.out.println("\n══════════════════════════════════");
        if (partida.isFinalizada()) {
            System.out.println("  FI DEL JOC");
            System.out.println("  Guanyador: " + partida.getGanador());
        } else {
            System.out.println("  Partida aturada per límit de torns (" + maxTorns + ").");
        }
        System.out.println("══════════════════════════════════");
    }
}
