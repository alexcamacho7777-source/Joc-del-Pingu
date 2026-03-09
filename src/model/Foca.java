package model;

public class Foca extends Jugador {

    private boolean soborno;

    public Foca(String nombre, String color, boolean soborno) {
        super(nombre, color);
        this.soborno = soborno;
    }

    public boolean isSoborno() {
        return soborno;
    }

    public void setSoborno(boolean soborno) {
        this.soborno = soborno;
    }

    public void aplastarJugador(Pinguino p) {
        System.out.println(getNombre() + " aplasta a " + p.getNombre());
    }

    public void golpearJugador(Pinguino p) {
        System.out.println(getNombre() + " golpea a " + p.getNombre());
    }

    public void esSobornado() {
        soborno = true;
        System.out.println(getNombre() + " ha sido sobornado");
    }
}