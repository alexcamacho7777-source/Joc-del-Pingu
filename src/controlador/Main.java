package controlador;

import model.*;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // --- Configurar BBDD ---
        GestorBBDD gestorBBDD = new GestorBBDD();

        // --- Crear gestor de partida ---
        GestorPartida gestorPartida = new GestorPartida();
        gestorPartida.setGestorBBDD(gestorBBDD);

        System.out.println("=== JOC DEL PINGUÍ ===");
        System.out.println("1) Nova partida");
        System.out.println("2) Carregar partida");
        System.out.print("Opció: ");
        int opcio = sc.nextInt();
        sc.nextLine();

        if (opcio == 2) {
            System.out.print("ID de partida a carregar: ");
            int id = sc.nextInt();
            sc.nextLine();
            gestorPartida.cargarPartida(id);
        } else {
            gestorPartida.nuevaPartida(null);

            // --- Afegir jugadors ---
            System.out.print("Nombre de pingüins (1-4): ");
            int numPinguins = Math.max(1, Math.min(4, sc.nextInt()));
            sc.nextLine();

            String[] colors = {"Blau", "Vermell", "Verd", "Groc"};
            for (int i = 0; i < numPinguins; i++) {
                System.out.print("Nom del pingüí " + (i + 1) + ": ");
                String nom = sc.nextLine().trim();
                Pinguino p = new Pinguino(nom, colors[i]);
                gestorPartida.getPartida().anadirJugador(p);
            }

            // --- Afegir foca (opcional) ---
            System.out.print("Afegir foca? (s/n): ");
            if (sc.nextLine().trim().equalsIgnoreCase("s")) {
                Foca foca = new Foca("Foca", "Gris");
                gestorPartida.getPartida().anadirJugador(foca);
            }
        }

        Partida partida = gestorPartida.getPartida();

        // --- Bucle de joc ---
        System.out.println("\n--- Partida iniciada ---");
        while (!partida.isFinalizada()) {
            Jugador jugadorActual = partida.getJugadorActualObj();
            System.out.println("\nTorn de: " + jugadorActual.getNombre()
                    + " (posició: " + jugadorActual.getPosicion() + ")");
            System.out.println("[ENTER] per tirar dau, [g] per guardar, [s] per sortir");
            String accio = sc.nextLine().trim().toLowerCase();

            if (accio.equals("s")) {
                System.out.println("Sortint sense guardar...");
                break;
            } else if (accio.equals("g")) {
                gestorPartida.guardarPartida();
                System.out.println("Partida guardada.");
                continue;
            }

            gestorPartida.ejecutarTurnoCompleto();

            // Mostrar esdeveniments del torn
            for (String event : partida.getEventos()) {
                System.out.println("  > " + event);
            }
            partida.getEventos().clear();
        }

        if (partida.isFinalizada() && partida.getGanador() != null) {
            System.out.println("\n*** Ha guanyat: " + partida.getGanador().getNombre() + " ***");
        }

        // Guardar partida final automàticament
        gestorPartida.guardarPartida();
        System.out.println("Partida guardada. Fins aviat!");
        sc.close();
    }
}
