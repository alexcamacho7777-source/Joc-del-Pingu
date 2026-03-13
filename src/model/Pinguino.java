package model;

public class Pinguino extends Jugador {

    private Inventario inventario;

    public Pinguino(String nombre, String color) {
        super(nombre, color);
        inventario = new Inventario();
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void añadirItem(Item i) {
        inventario.getLista().add(i);
        System.out.println("Item añadido: " + i.getNombre());
    }

    public void quitarItem(Item i) {
        inventario.getLista().remove(i);
        System.out.println("Item eliminado: " + i.getNombre());

    }
    
    public void usarItem(Item item) {

        if (item instanceof BolaDeNieve) {

            System.out.println(getNombre() + " usa una bola de nieve");
        } 
        else if (item instanceof Pez) {
            System.out.println(getNombre() + " usa un pez");
        } 
        else if (item instanceof Dado) {
            System.out.println(getNombre() + " usa un dado");
        }

        quitarItem(item);
    }
    
    public void gestionarBatalla(Pinguino enemigo) {

        int bolasJugador = 0;
        int bolasEnemigo = 0;

        // contar bolas del jugador
        for (Item item : inventario.getLista()) {
            if (item instanceof BolaDeNieve) {
                bolasJugador += item.getCantidad();
            }
        }

        // contar bolas del enemigo
        for (Item item : enemigo.getInventario().getLista()) {
            if (item instanceof BolaDeNieve) {
                bolasEnemigo += item.getCantidad();
            }
        }

        System.out.println(getNombre() + " tiene " + bolasJugador + " bolas de nieve");
        System.out.println(enemigo.getNombre() + " tiene " + bolasEnemigo + " bolas de nieve");

        if (bolasJugador > bolasEnemigo) {

            int diferencia = bolasJugador - bolasEnemigo;
            enemigo.moverPosicion(-diferencia);

            System.out.println(getNombre() + " gana la batalla");

        } 
        else if (bolasEnemigo > bolasJugador) {

            int diferencia = bolasEnemigo - bolasJugador;
            moverPosicion(-diferencia);

            System.out.println(enemigo.getNombre() + " gana la batalla");

        } 
        else {

            System.out.println("Empate, nadie retrocede");

        }

    }
}