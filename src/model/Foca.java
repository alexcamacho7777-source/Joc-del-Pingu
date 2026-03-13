package model;

public class Foca extends Jugador {

    private boolean soborno;

    public Foca(String nombre, String color) {
        super(nombre, color);
        soborno = false;
    }

    public boolean isSoborno() {
        return soborno;
    }

    public void setSoborno(boolean soborno) {
        this.soborno = soborno;
    }

    public void aplastarJugador(Pinguino p) {
        System.out.println(getNombre() + " aplasta a " + p.getNombre());
        p.setPosicion(0);

    }

    public void golpearJugador(Pinguino p) {
    	 System.out.println("La foca golpea a " + p.getNombre());
         int nuevaPos = p.getPosicion() - 2;
         if (nuevaPos < 0) nuevaPos = 0;
         p.setPosicion(nuevaPos);    }

    public void esSobornado() {
        System.out.println("La foca ha sido sobornada con un pez");
        soborno = true;
    }
}