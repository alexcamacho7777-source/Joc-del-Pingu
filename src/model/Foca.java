package model;

public class Foca extends Jugador {

    private boolean sobornada;
    private int turnosBloqueada;

    public Foca() {
        super("Foca (CPU)", "Gris");
        this.sobornada = false;
        this.turnosBloqueada = 0;
        this.setEsIA(true);
    }

    public boolean isSobornada() { 
    	return sobornada; 
    	}
    public void setSobornada(boolean sobornada) { 
    	this.sobornada = sobornada; 
    	}
    public int getTurnosBloqueada() { 
    	return turnosBloqueada; 
    	}
    public void setTurnosBloqueada(int turnosBloqueada) { 
    	this.turnosBloqueada = turnosBloqueada; 
    	}


    public void apalizarJugador(Pinguino p) {
    }

    public void golpearJugador(Pinguino p) {
        apalizarJugador(p);
    }


    public boolean esSobornada() {
        return sobornada;
    }

    public void activarSoborno() {
        this.sobornada = true;
        this.turnosBloqueada = 2;
    }

    public void reducirBloqueo() {
        if (turnosBloqueada > 0) {
            turnosBloqueada--;
            if (turnosBloqueada == 0) sobornada = false;
        }
    }
}
