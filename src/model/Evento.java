package model;

import java.util.Random;

public class Evento extends Casilla {

    private String[] eventos;

    public Evento(int posicion) {
        super(posicion);

        eventos = new String[]{
            "Obtienes un pez",
            "Obtienes bolas de nieve",
            "Obtienes dado rapido",
            "Obtienes dado lento"
        };
    }

    @Override
    public void realizarAccion(Partida partida, Jugador jugador) {

        Random ran = new Random();
        String evento = eventos[ran.nextInt(6)];

        if (evento.equals("pez")) {

            if (jugador instanceof Pinguino) {
                Pinguino p = (Pinguino) jugador;
                p.añadirItem(new Pez("pez", 1));
                System.out.println(p.getNombre() + " obtiene un pez");
            }

        } else if (evento.equals("bolas")) {

            if (jugador instanceof Pinguino) {
                Pinguino p = (Pinguino) jugador;

                int cantidad = ran.nextInt(3) + 1;

                p.añadirItem(new BolaDeNieve("bola", cantidad));

                System.out.println(p.getNombre() + " obtiene " + cantidad + " bolas de nieve");
            }

        } else if (evento.equals("rapido")) {

            if (jugador instanceof Pinguino) {
                Pinguino p = (Pinguino) jugador;

                p.añadirItem(new Dado("dadoRapido", 1, 5, 10));

                System.out.println(p.getNombre() + " obtiene un dado rapido");
            }

        } else if (evento.equals("lento")) {

            if (jugador instanceof Pinguino) {
                Pinguino p = (Pinguino) jugador;

                p.añadirItem(new Dado("dadoLento", 1, 1, 3));

                System.out.println(p.getNombre() + " obtiene un dado lento");
            }

        } else if (evento.equals("pierdeTurno")) {

            System.out.println(jugador.getNombre() + " pierde un turno");

        } else if (evento.equals("pierdeItem")) {

            if (jugador instanceof Pinguino) {

                Pinguino p = (Pinguino) jugador;

                if (!p.getInventario().getLista().isEmpty()) {

                    Item item = p.getInventario().getLista().get(0);
                    p.quitarItem(item);

                    System.out.println(p.getNombre() + " pierde un item");
                }
            }

        } else if (evento.equals("motos")) {

            System.out.println("Evento motos de nieve");

            jugador.moverPosicion(3);
        }
    }
}