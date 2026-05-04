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
    public void nuevaPartida() {
        partida = new Partida();
        partida.setRandom(random);
        partida.anadirEvento("Partida començada.");
    }

    /**
     * Inicia los dados de turno para el jugador indicado.
     * @param jugador jugador que inicia dados
     */
    public void initTirarDado(Jugador jugador) {
        // Reservado para integración con la vista
    }

    /**
     * Tira el dau donat, mou el jugador i retorna el resultat.
     * Usat per PantallaJuego per moure la fitxa P1.
     *
     * @param j    Jugador que tira
     * @param dado Dau que s'utilitza
     * @return Resultat del dau
     */
    public int tirarDado(Jugador j, Dado dado) {
        int resultado = dado.tirar(random);
        j.moverPosicion(resultado);
        int maxPos = partida.getTablero().getTotalCasillas() - 1;
        if (j.getPosicion() > maxPos) j.setPosicion(maxPos);
        return resultado;
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
        // Comprovar si perd el torn
        if (partida.getJugadorPierdeTurno() != null &&
            partida.getJugadorPierdeTurno().equals(jugadorTurno)) {
            partida.setJugadorPierdeTurno(null);
            partida.anadirEvento(jugadorTurno.getNombre() + " perd aquest torn.");
            partida.siguienteTurno();
            return;
        }

        // Reduir bloqueig de foca si és el seu torn
        if (jugadorTurno instanceof Foca foca) {
            foca.reducirBloqueo();
            if (foca.isSobornada()) {
                partida.anadirEvento("La foca continua bloquejada.");
                partida.siguienteTurno();
                return;
            }
        }

        // Tirar dau
        int pasos = tirarDadoParaJugador(jugadorTurno);
        int posAnterior = jugadorTurno.getPosicion();
        jugadorTurno.moverPosicion(pasos);

        // Limitar a l'última casella
        int maxPos = partida.getTablero().getTotalCasillas() - 1;
        if (jugadorTurno.getPosicion() > maxPos) {
            jugadorTurno.setPosicion(maxPos);
        }
        int posNueva = jugadorTurno.getPosicion();

        partida.anadirEvento(jugadorTurno.getNombre() + " avança " + pasos + " caselles. Posició: " + jugadorTurno.getPosicion());

        // Lògica específica Foca si passa per sobre de jugadors
        if (jugadorTurno instanceof Foca foca) {
            procesarPasoDeFoca(foca, posAnterior, posNueva);
        }

        // Aplicar acció de casella
        Casilla casilla = partida.getTablero().getCasilla(jugadorTurno.getPosicion());
        gestorTablero.ejecutarCasilla(partida, jugadorTurno, casilla);

        // Comprovar interaccions entre jugadors a la mateixa casella
        if (jugadorTurno instanceof Pinguino p) {
            comprobarInteraccionesEnCasilla(p);
        } else if (jugadorTurno instanceof Foca foca) {
            // Re-utilitzem la lògica d'interacció si la foca cau sobre algú
            comprobarInteraccionesEnCasilla(foca); 
        }

        // Comprovar victòria
        actualizarEstadoTablero();

        // Següent torn
        partida.siguienteTurno();
    }

    /**
     * Tira el dado correspondiente al jugador.
     * Si el jugador es IA, decide qué dado usar.
     * Si es Humano, usa el dado que se le indique (por defecto normal si no hay elección).
     */
    private int tirarDadoParaJugador(Jugador j) {
        if (j instanceof Pinguino p) {
            // Lógica de elección de dado para IA
            if (p.isEsIA()) {
                Item it = decidirDadoIA(p);
                if (it instanceof Dado d) {
                    return usarDadoEspecial(p, d);
                }
            }
            // Si es humano o no eligió dado especial, dado normal
        }
        return 1 + random.nextInt(6);
    }

    private Item decidirDadoIA(Pinguino p) {
        int r = random.nextInt(100);
        if (r < 40) { // 40% de probabilidad de que la IA use un dado especial si tiene
            return p.getInv().getItem(Dado.class);
        }
        return null;
    }

    public int usarDadoEspecial(Pinguino p, Dado d) {
        int resultado = d.tirar(random);
        d.setCantidad(d.getCantidad() - 1);
        if (d.getCantidad() <= 0) p.getInv().quitarItem(d);
        return resultado;
    }

    /**
     * Permite a la IA usar ítems antes de tirar (ej: peces).
     */
    public void realizarAccionesIA(Jugador j) {
        if (!(j instanceof Pinguino p) || !p.isEsIA()) return;

        // Si está cerca de la foca y tiene peces, se los guarda para el encuentro 
        // o los usa proactivamente si implementamos soborno remoto.
        // Por ahora, el encuentro con foca ya comprueba el inventario.
    }

    /**
     * Comprueba si el pingüino coincide en casilla con otro jugador o la foca
     * y aplica las reglas correspondientes.
     */
    private void comprobarInteraccionesEnCasilla(Jugador p) {
        // 1. Interaccions al FINAL de moure
        for (Jugador otro : partida.getJugadores()) {
            if (otro == p) continue;
            if (otro.getPosicion() == p.getPosicion()) {
                if (otro instanceof Foca foca) {
                    // Si el jugador cau on hi ha la foca
                    if (p instanceof Pinguino pin) {
                        gestorJugador.focaInteraccion(pin, foca);
                    }
                    if (!foca.isSobornada()) {
                        int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(p.getPosicion());
                        p.setPosicion(anteriorAgujero);
                        partida.anadirEvento("La foca colpeja a " + p.getNombre() + " i l'envia al forat anterior (pos " + anteriorAgujero + ").");
                    }
                } else if (otro instanceof Pinguino p2 && p instanceof Pinguino p1) {
                    // Guerra de pingüins
                    gestorJugador.pinguinoGuerraQuema(p1, p2);
                }
            }
            
            // 2. Si la foca és qui s'ha mogut i cau sobre el jugador
            if (p instanceof Foca foca && !foca.isSobornada()) {
                if (otro.getPosicion() == foca.getPosicion() && otro instanceof Pinguino p2) {
                    int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(p2.getPosicion());
                    p2.setPosicion(anteriorAgujero);
                    partida.anadirEvento("La foca cau sobre " + p2.getNombre() + " i el colpeja al forat anterior!");
                }
            }
        }
    }

    /**
     * Lògica extra per quan la Foca es mou: comprova si passa per sobre de jugadors.
     */
    private void procesarPasoDeFoca(Foca foca, int posAnterior, int posNueva) {
        if (foca.isSobornada()) return;
        
        for (Jugador j : partida.getJugadores()) {
            if (j instanceof Pinguino p && !p.isEsIA()) {
                // Si la foca ha passat per la posició del jugador (entre posAnterior i posNueva)
                if (p.getPosicion() > posAnterior && p.getPosicion() <= posNueva) {
                    perderMitadInventario(p);
                    partida.anadirEvento("La foca ha passat volant per sobre de " + p.getNombre() + " i li ha robat la meitat de l'inventari!");
                }
            }
        }
    }

    /**
     * Hace perder la mitad de los ítems del inventario al pingüino.
     */
    private void perderMitadInventario(Jugador p) {
        if (p.getInv() == null) return;
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
                partida.anadirEvento("¡" + j.getNombre() + " ha guanyat la partida!");
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
     * Guarda la partida en base de dades.
     */
    public void guardarPartida() {
        if (gestorBBDD != null && partida != null) {
            gestorBBDD.guardarBBDD(partida);
            partida.anadirEvento("Partida guardada.");
        }
    }

    /**
     * Carrega una partida des de la base de dades per ID.
     * @param id identificador de la partida
     */
    public void cargarPartida(int id) {
        if (gestorBBDD != null) {
            partida = gestorBBDD.cargarBBDD(id);
            partida.anadirEvento("Partida carregada (id=" + id + ").");
        }
    }
}
