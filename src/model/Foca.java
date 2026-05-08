package model;

/**
 * REPRESENTA L'ANTAGONISTA O NPC 'FOCA' DINS DEL JOC.
 * ÈS UNA ENTITAT CONTROLADA PER LA IA QUE POT BLOQUEJAR ELS JUGADORS O SER SUBORNADA.
 */
public class Foca extends Jugador {

    private boolean sobornada;
    private int turnosBloqueada;

    /**
     * CONSTRUCTOR PER DEFECTE DE LA FOCA.
     * S'INICIALITZA COM A ENTITAT IA A LA POSICIÓ INICIAL.
     */
    public Foca() {
        super("FOCA (CPU)", "GRIS");
        this.setPosicion(0);
        this.sobornada = false;
        this.turnosBloqueada = 0;
        this.setEsIA(true);
    }

    // GETTERS I SETTERS PER A L'ESTAT DEL SUBORN
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

    /**
     * ACTIVA L'ESTAT DE SUBORN DE LA FOCA.
     * BLOQUEJA L'ENTITAT DURANT 2 TORNS EN ELS QUALS NO INTERACTUARÀ NEGATIVAMENT.
     */
    public void activarSoborno() {
        this.sobornada = true;
        this.turnosBloqueada = 2;
    }

    /**
     * REDUEIX EL COMPTADOR DE TORNS DE BLOQUEIG.
     * SI ARRIBA A ZERO, LA FOCA DEIXA D'ESTAR SUBORNADA I TORNA A SER ACTIVA.
     */
    public void reducirBloqueo() {
        if (turnosBloqueada > 0) {
            turnosBloqueada--;
            if (turnosBloqueada == 0) {
                sobornada = false;
            }
        }
    }
}
