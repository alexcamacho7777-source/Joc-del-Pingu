package controlador;

import model.*;

import java.util.Random;

/**
 * Controlador principal de la partida.
 * Coordina los turnos, movimientos, eventos y persistencia.
 */
public class GestorPartida {

    /** Partida actualmente en curso. */
    private Partida partida;

    /** Controlador del tablero. */
    private GestorTablero gestorTablero;

    /** Controlador de jugadores. */
    private GestorJugador gestorJugador;

    /** Controlador de base de datos. */
    private GestorBBDD gestorBBDD;

    /** Generador de números aleatorios. */
    private Random random;

    /**
     * Constructor de GestorPartida.
     */
    public GestorPartida() {
        this.gestorTablero = new GestorTablero();
        this.gestorJugador = new GestorJugador();
        this.random = new Random();
    }

    // --- Getters y Setters ---

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public GestorTablero getGestorTablero() { return gestorTablero; }
    public GestorJugador getGestorJugador() { return gestorJugador; }
    public GestorBBDD getGestorBBDD() { return gestorBBDD; }
    public void setGestorBBDD(GestorBBDD gestorBBDD) { this.gestorBBDD = gestorBBDD; }
    public Random getRandom() { return random; }

    // --- Métodos de ciclo de vida ---

    /**
     * Inicia una nueva partida con los jugadores indicados.
     * Si datosOpcional es null se usa configuración por defecto.
     * @param datosOpcional datos de inicialización opcionales (puede ser null)
     */
    public void nuevaPartida(Object datosOpcional) {
        partida = new Partida();
        partida.setRandom(random);
        partida.anadirEvento("Partida iniciada.");
    }

    /** Sobrecàrrega sense arguments que usa la vista del professor */
    public void nuevaPartida() {
        nuevaPartida(null);
    }

    /** Tira el dau donat i mou el jugador. Retorna el resultat. */
    public int tirarDado(Jugador j, Dado dado) {
        int resultado = dado.tirar(random);
        j.moverPosicion(resultado);
        int maxPos = partida.getTablero().getTotalCasillas() - 1;
        if (j.getPosicion() > maxPos) j.setPosicion(maxPos);
        return resultado;
    }

    /**
     * Inicia los dados de turno para el jugador indicado.
     * @param jugador jugador que inicia dados
     */
    public void initTirarDado(Jugador jugador) {
        // Reservado para integración con la vista
    }

    /**
     * Ejecuta el turno completo del jugador actual:
     * tira el dado, mueve al jugador y aplica la acción de la casilla.
     */
    public void ejecutarTurnoCompleto() {
        if (partida == null || partida.isFinalizada()) return;

        Jugador jugadorActual = partida.getJugadorActualObj();
        if (jugadorActual == null) return;

        procesarTurnoJugador(jugadorActual, jugadorActual);
    }

    /**
     * Procesa el turno de un jugador concreto.
     * @param jugadorTurno  jugador al que le toca
     * @param jugadorActivo jugador que actúa (para la CPU puede diferir)
     */
    public void procesarTurnoJugador(Jugador jugadorTurno, Jugador jugadorActivo) {
        // Comprobar si pierde turno
        if (partida.getJugadorPierdeTurno() != null &&
            partida.getJugadorPierdeTurno().equals(jugadorTurno)) {
            partida.setJugadorPierdeTurno(null);
            partida.anadirEvento(jugadorTurno.getNombre() + " pierde este turno.");
            partida.siguienteTurno();
            return;
        }

        // Reducir bloqueo de foca si es su turno
        if (jugadorTurno instanceof Foca foca) {
            foca.reducirBloqueo();
            if (foca.isSobornada()) {
                partida.anadirEvento("La foca sigue bloqueada.");
                partida.siguienteTurno();
                return;
            }
        }

        // Tirar dado
        int pasos = tirarDadoParaJugador(jugadorTurno);
        jugadorTurno.moverPosicion(pasos);

        // Limitar a la última casilla
        int maxPos = partida.getTablero().getTotalCasillas() - 1;
        if (jugadorTurno.getPosicion() > maxPos) {
            jugadorTurno.setPosicion(maxPos);
        }

        partida.anadirEvento(jugadorTurno.getNombre() + " avanza " + pasos + " casillas. Posición: " + jugadorTurno.getPosicion());

        // Aplicar acción de casilla
        Casilla casilla = partida.getTablero().getCasilla(jugadorTurno.getPosicion());
        gestorTablero.ejecutarCasilla(partida, jugadorTurno, casilla);

        // Comprobar interacciones entre jugadores en la misma casilla
        if (jugadorTurno instanceof Pinguino p) {
            comprobarInteraccionesEnCasilla(p);
        }

        // Comprobar victoria
        actualizarEstadoTablero();

        // Siguiente turno
        partida.siguienteTurno();
    }

    /**
     * Tira el dado correspondiente al jugador.
     * Si el jugador tiene un dado especial en el inventario, lo usa;
     * de lo contrario, usa un dado normal (1-6).
     */
    private int tirarDadoParaJugador(Jugador j) {
        if (j instanceof Pinguino p) {
            Item dadoItem = p.getInv().getItem(Dado.class);
            if (dadoItem instanceof Dado dado) {
                int resultado = dado.tirar(random);
                dado.setCantidad(dado.getCantidad() - 1);
                if (dado.getCantidad() <= 0) p.getInv().quitarItem(dado);
                return resultado;
            }
        }
        // Dado normal 1-6
        return 1 + random.nextInt(6);
    }

    /**
     * Comprueba si el pingüino coincide en casilla con otro jugador o la foca
     * y aplica las reglas correspondientes.
     */
    private void comprobarInteraccionesEnCasilla(Pinguino p) {
        for (Jugador otro : partida.getJugadores()) {
            if (otro == p) continue;
            if (otro.getPosicion() == p.getPosicion()) {
                if (otro instanceof Foca foca) {
                    gestorJugador.focaInteraccion(p, foca);
                    // Si la foca lo golpea, enviar al forat anterior
                    if (!foca.isSobornada()) {
                        int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(p.getPosicion());
                        p.setPosicion(anteriorAgujero);
                        partida.anadirEvento("La foca golpea a " + p.getNombre() + " y lo envía a la posición " + anteriorAgujero + ".");
                    }
                } else if (otro instanceof Pinguino p2) {
                    // Guerra de pingüinos
                    partida.anadirEvento("¡Guerra entre " + p.getNombre() + " y " + p2.getNombre() + "!");
                    gestorJugador.pinguinoGuerraQuema(p, p2);
                }
            }
        }

        // Foca pasa por casilla de jugador (pierde la mitad del inventario)
        for (Jugador otro : partida.getJugadores()) {
            if (otro instanceof Foca foca && !foca.isSobornada()) {
                if (foca.getPosicion() == p.getPosicion()) {
                    perderMitadInventario(p);
                    partida.anadirEvento("La foca pasa por la casilla de " + p.getNombre() + " y le hace perder la mitad del inventario.");
                }
            }
        }
    }

    /**
     * Hace perder la mitad de los ítems del inventario al pingüino.
     */
    private void perderMitadInventario(Pinguino p) {
        int total = p.getInv().totalItems();
        int perder = total / 2;
        for (int i = 0; i < perder; i++) {
            p.getInv().quitarItemAleatorio(random);
        }
    }

    /**
     * Actualiza el estado del tablero y comprueba si hay un ganador.
     */
    public void actualizarEstadoTablero() {
        int meta = partida.getTablero().getTotalCasillas() - 1;
        for (Jugador j : partida.getJugadores()) {
            if (j.getPosicion() >= meta && !(j instanceof Foca)) {
                partida.setGanador(j);
                partida.setFinalizada(true);
                partida.anadirEvento("¡" + j.getNombre() + " ha ganado la partida!");
                return;
            }
        }
    }

    /**
     * Avanza al siguiente turno.
     */
    public void siguienteTurno() {
        partida.siguienteTurno();
    }

    /**
     * Devuelve la partida actual.
     * @return partida
     */
    public Partida getPartidaActual() {
        return partida;
    }

    /**
     * Guarda la partida en base de datos.
     */
    public void guardarPartida() {
        if (gestorBBDD != null && partida != null) {
            gestorBBDD.guardarBBDD(partida);
            partida.anadirEvento("Partida guardada.");
        }
    }

    /**
     * Carga una partida desde la base de datos por ID.
     * @param id identificador de la partida
     */
    public void cargarPartida(int id) {
        if (gestorBBDD != null) {
            partida = gestorBBDD.cargarBBDD(id);
            partida.anadirEvento("Partida cargada (id=" + id + ").");
        }
    }
}
