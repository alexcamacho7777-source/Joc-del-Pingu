package model;

/**
 * Representa la CPU controlada por IA (la Foca).
 * Si pasa por la casilla de un jugador, le hace perder la mitad del inventario.
 * Si coincide con un jugador, lo envía al forat anterior.
 */
public class Foca extends Jugador {

    /** Indica si la foca está sobornada (bloqueada 2 turnos). */
    private boolean sobornada;

    /** Turnos que la foca permanece bloqueada. */
    private int turnosBloqueada;

    /**
     * Constructor de Foca.
     */
    public Foca() {
        super("Foca (CPU)", "Gris");
        this.sobornada = false;
        this.turnosBloqueada = 0;
    }

    public boolean isSobornada() { return sobornada; }
    public void setSobornada(boolean sobornada) { this.sobornada = sobornada; }

    public int getTurnosBloqueada() { return turnosBloqueada; }
    public void setTurnosBloqueada(int turnosBloqueada) { this.turnosBloqueada = turnosBloqueada; }

    /**
     * La foca apaliza al jugador: lo envía al forat anterior.
     * @param p pingüino a golpear
     */
    public void apalizarJugador(Pinguino p) {
        // Lógica delegada al GestorJugador / GestorPartida
    }

    /**
     * La foca golpea a un jugador con la cola: lo envía al forat anterior.
     * @param p pingüino objetivo
     */
    public void golpearJugador(Pinguino p) {
        apalizarJugador(p);
    }

    /**
     * Comprueba si la foca está sobornada.
     * @return true si está sobornada
     */
    public boolean esSobornada() {
        return sobornada;
    }

    /**
     * Activa el soborno de la foca durante 2 turnos.
     */
    public void activarSoborno() {
        this.sobornada = true;
        this.turnosBloqueada = 2;
    }

    /**
     * Decrementa el contador de bloqueo al inicio de cada turno.
     */
    public void reducirBloqueo() {
        if (turnosBloqueada > 0) {
            turnosBloqueada--;
            if (turnosBloqueada == 0) sobornada = false;
        }
    }
}
