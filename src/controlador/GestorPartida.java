package controlador;

import model.*;

import java.util.Random;

/**
 * CONTROLADOR CENTRAL DE LA PARTIDA.
 * GESTIONA L'ORDRE DES TORNS, EL MOVIMENT DELS JUGADORS, ELS EVENTS ALEATORIS
 * I LA SINCRONITZACIÓ AMB LA BASE DE DADES.
 */
public class GestorPartida {

    // PARTIDA EN EXECUCIÓ ACTUALMENT
    private Partida partida;

    // CONTROLADOR DE LA LÒGICA DEL TAULELL I LES CASELLES
    private GestorTablero gestorTablero;

    // CONTROLADOR DE LES ACCIONS ESPECÍFIQUES DELS JUGADORS
    private GestorJugador gestorJugador;

    // CONTROLADOR PER A LA PERSISTÈNCIA DE DADES A ORACLE
    private GestorBBDD gestorBBDD;

    // GENERADOR DE NÚMEROS ALEATORIS PER A DAUS I EVENTS
    private Random random;

    /**
     * CONSTRUCTOR QUE INICIALITZA ELS GESTORS DE LÒGICA AUXILIARS.
     */
    public GestorPartida() {
        this.gestorTablero = new GestorTablero();
        this.gestorJugador = new GestorJugador();
        this.random = new Random();
    }

    // ── MÈTODES D'ACCÉS (GETTERS I SETTERS) ──────────────────────────────────

    public Partida getPartida() { return partida; }
    public void setPartida(Partida partida) { this.partida = partida; }

    public GestorTablero getGestorTablero() { return gestorTablero; }
    public GestorJugador getGestorJugador() { return gestorJugador; }
    public GestorBBDD getGestorBBDD() { return gestorBBDD; }
    public void setGestorBBDD(GestorBBDD gestorBBDD) { this.gestorBBDD = gestorBBDD; }
    public Random getRandom() { return random; }

    // ── GESTIÓ DEL CICLE DE VIDA DE LA PARTIDA ───────────────────────────────

    /**
     * CREA UNA NOVA PARTIDA BUIDA I REGISTRA L'INICI AL LOG D'EVENTS.
     */
    public void nuevaPartida() {
        partida = new Partida();
        partida.setRandom(random);
        partida.anadirEvento("PARTIDA COMENÇADA.");
    }

    /**
     * GESTIONA LA TIRADA D'UN DAU ESPECÍFIC PER A UN JUGADOR.
     * ACTUALITZA LA POSICIÓ I ASSEGURA QUE NO SE SUPERI EL FINAL DEL TAULELL.
     */
    public int tirarDado(Jugador j, Dado dado) {
        int resultado = dado.tirar(random);
        j.moverPosicion(resultado);
        int maxPos = partida.getTablero().getTotalCasillas() - 1;
        if (j.getPosicion() > maxPos) j.setPosicion(maxPos);
        return resultado;
    }

    /**
     * EXECUTA EL TORN COMPLET DEL JUGADOR ACTUAL (DINS DEL FLUX DE LÒGICA PURA).
     */
    public void ejecutarTurnoCompleto() {
        if (partida != null && !partida.isFinalizada()) {
            Jugador jugadorActual = partida.getJugadorActualObj();
            if (jugadorActual != null) {
                procesarTurnoJugador(jugadorActual, jugadorActual);
            }
        }
    }

    /**
     * LÒGICA CENTRAL DEL TORN D'UN JUGADOR.
     * CONTROLA EL BLOQUEIG, EL MOVIMENT, LES ACCIONS DE CASELLA I LES INTERACCIONS.
     */
    public void procesarTurnoJugador(Jugador jugadorTurno, Jugador jugadorActivo) {
        // 1. VERIFICACIÓ DE SI EL JUGADOR HA DE PERDRE EL TORN
        if (partida.getJugadorPierdeTurno() != null && partida.getJugadorPierdeTurno().equals(jugadorTurno)) {
            partida.setJugadorPierdeTurno(null);
            partida.anadirEvento(jugadorTurno.getNombre() + " PERD AQUEST TORN.");
            partida.siguienteTurno();
        } else {
            boolean bloqueado = false;
            // 2. GESTIÓ DEL BLOQUEIG ESPECÍFIC DE LA FOCA
            if (jugadorTurno instanceof Foca foca) {
                if (foca.getTurnosBloqueada() > 0) {
                    foca.reducirBloqueo();
                    partida.anadirEvento("LA FOCA CONTINUA BLOQUEJADA (" + foca.getTurnosBloqueada() + " TORNS RESTANTS).");
                    partida.siguienteTurno();
                    bloqueado = true;
                }
            }

            if (!bloqueado) {
                // 3. MOVIMENT: TIRADA DE DAU (CONSIDERANT IA SI CAL)
                int pasos = tirarDadoParaJugador(jugadorTurno);
                int posAnterior = jugadorTurno.getPosicion();
                jugadorTurno.moverPosicion(pasos);

                // LIMITACIÓ A L'ÚLTIMA CASELLA DEL TAULELL
                int maxPos = partida.getTablero().getTotalCasillas() - 1;
                if (jugadorTurno.getPosicion() > maxPos) {
                    jugadorTurno.setPosicion(maxPos);
                }
                int posNueva = jugadorTurno.getPosicion();

                partida.anadirEvento(jugadorTurno.getNombre() + " AVANÇA " + pasos + " CASELLES.");

                // 4. LÒGICA DE LA FOCA: ROBA ÍTEMS SI PASSA PER SOBRE D'UN JUGADOR
                if (jugadorTurno instanceof Foca foca) {
                    procesarPasoDeFoca(foca, posAnterior, posNueva);
                }

                // 5. EXECUCIÓ DE L'EFECTE DE LA CASELLA D'ATERRIZATGE
                Casilla casilla = partida.getTablero().getCasilla(jugadorTurno.getPosicion());
                gestorTablero.ejecutarCasilla(partida, jugadorTurno, casilla);

                // 6. COMPROVACIÓ D'INTERACCIONS (ATERRIZAR SOBRE UN ALTRE JUGADOR)
                comprobarInteraccionesEnCasilla(jugadorTurno);

                // 7. ACTUALITZACIÓ DE L'ESTAT I CANVI DE TORN
                actualizarEstadoTablero();
                partida.siguienteTurno();
            }
        }
    }

    /**
     * DETERMINA EL NÚMERO DE CASELLES A MOURE.
     * SI ÉS IA, DECIDEIX SI UTILITZA UN DAU ESPECIAL DE L'INVENTARI.
     */
    public int tirarDadoParaJugador(Jugador j) {
        if (j instanceof Pinguino p) {
            if (p.isEsIA()) {
                Item it = decidirDadoIA(p);
                if (it instanceof Dado d) {
                    return usarDadoEspecial(p, d);
                }
            }
        }
        return 1 + random.nextInt(6);
    }

    /**
     * LÒGICA DE DECISIÓ PER A LA INTEL·LIGÈNCIA ARTIFICIAL.
     */
    private Item decidirDadoIA(Pinguino p) {
        int r = random.nextInt(100);
        if (r < 40) { // 40% DE PROBABILITAT D'USAR DAU ESPECIAL
            return p.getInv().getItem(Dado.class);
        }
        return null;
    }

    /**
     * UTILITZA UN DAU ESPECIAL I EN REDUEIX LA QUANTITAT DE L'INVENTARI.
     */
    public int usarDadoEspecial(Pinguino p, Dado d) {
        int resultado = d.tirar(random);
        d.setCantidad(d.getCantidad() - 1);
        if (d.getCantidad() <= 0) p.getInv().quitarItem(d);
        return resultado;
    }

    /**
     * PERMET A LA IA REALITZAR ACCIONS PROACTIVES AVANS DE MOURE'S.
     */
    public void realizarAccionesIA(Jugador j) {
        if (!(j instanceof Pinguino p) || !p.isEsIA()) return;
        // (LÒGICA AMPLIABLE PER A ESTRATÈGIES DE LA IA)
    }

    /**
     * GESTIONA ELS ENCONTRES DINS D'UNA MATEIXA CASELLA.
     * INCLOU LA LÒGICA DE SUBORN DE LA FOCA I LA GUERRA DE BOLES DE NEU.
     */
    public void comprobarInteraccionesEnCasilla(Jugador p) {
        for (Jugador otro : partida.getJugadores()) {
            if (otro != p && otro.getPosicion() == p.getPosicion()) {
                if (otro instanceof Foca foca) {
                    if (p instanceof Pinguino pin && pin.isEsIA()) {
                        gestorJugador.focaInteraccion(pin, foca);
                        if (!foca.isSobornada()) {
                            int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(p.getPosicion());
                            p.setPosicion(anteriorAgujero);
                            partida.anadirEvento("LA FOCA COLPEJA A " + p.getNombre() + " I L'ENVIA AL FORAT ANTERIOR.");
                        }
                    }
                } else if (otro instanceof Pinguino p2 && p instanceof Pinguino p1) {
                    if (p1.isEsIA() || p2.isEsIA()) {
                        gestorJugador.pinguinoGuerraQuema(p1, p2);
                    }
                }
            }
            
            // SI LA FOCA ACABA EL SEU MOVIMENT SOBRE UN JUGADOR
            if (p instanceof Foca foca && !foca.isSobornada()) {
                if (otro.getPosicion() == foca.getPosicion() && otro instanceof Pinguino p2 && p2.isEsIA()) {
                    int anteriorAgujero = partida.getTablero().buscarAgujeroAnterior(p2.getPosicion());
                    p2.setPosicion(anteriorAgujero);
                    partida.anadirEvento("LA FOCA CAU SOBRE " + p2.getNombre() + " I EL COLPEJA AL FORAT ANTERIOR.");
                }
            }
        }
    }

    /**
     * PROCESSA EL ROBATORI D'ÍTEMS QUAN LA FOCA PASSA PER SOBRE D'UN JUGADOR.
     * EL JUGADOR PERD LA MEITAT DEL SEU INVENTARI SI NO ESTÀ A LA CASELLA D'INICI.
     */
    public java.util.Map<Pinguino, java.util.List<String>> procesarPasoDeFoca(Foca foca, int posAnterior, int posNueva) {
        java.util.Map<Pinguino, java.util.List<String>> robos = new java.util.HashMap<>();
        if (foca.isSobornada()) return robos;
        
        int start = Math.min(posAnterior, posNueva);
        int end = Math.max(posAnterior, posNueva);

        for (Jugador j : partida.getJugadores()) {
            if (j instanceof Pinguino p) {
                if (p.getPosicion() != 0 && p.getPosicion() > start && p.getPosicion() < end) {
                    java.util.List<String> perdidos = perderMitadInventario(p);
                    if (!perdidos.isEmpty()) {
                        robos.put(p, perdidos);
                        partida.anadirEvento("LA FOCA HA ROBAT A " + p.getNombre() + ".");
                    }
                }
            }
        }
        return robos;
    }

    /**
     * ELIMINA LA MEITAT DELS ÍTEMS DE L'INVENTARI D'UN JUGADOR DE FORMA ALEATÒRIA.
     */
    private java.util.List<String> perderMitadInventario(Jugador p) {
        java.util.List<String> perdidos = new java.util.ArrayList<>();
        if (p.getInv() == null) return perdidos;
        
        int total = p.getInv().totalItems();
        int perder = total / 2;
        
        for (int i = 0; i < perder; i++) {
            Item stack = p.getInv().quitarUnidadAleatoria(random);
            if (stack != null) perdidos.add(stack.getNombre());
        }
        return perdidos;
    }

    /**
     * VERIFICA SI ALGUN JUGADOR HA ARRIBAT A LA CASELLA FINAL I DETERMINA EL GUANYADOR.
     */
    public void actualizarEstadoTablero() {
        int meta = partida.getTablero().getTotalCasillas() - 1;
        for (Jugador j : partida.getJugadores()) {
            if (j.getPosicion() >= meta && !(j instanceof Foca)) {
                partida.setGanador(j);
                partida.setFinalizada(true);
                partida.anadirEvento("¡" + j.getNombre() + " HA GUANYAT LA PARTIDA!");
            }
        }
    }

    /**
     * AVANÇA EL TORN AL SEGÜENT JUGADOR SEGONS L'ORDRE ESTABLERT.
     */
    public void siguienteTurno() {
        partida.siguienteTurno();
    }

    /**
     * RETORNA L'OBJECTE DE PARTIDA ACTUAL.
     */
    public Partida getPartidaActual() {
        return partida;
    }

    /**
     * GUARDA L'ESTAT ACTUAL DE LA PARTIDA A LA BASE DE DADES ORACLE.
     */
    public boolean guardarPartida() {
        if (gestorBBDD != null && partida != null) {
            boolean ok = gestorBBDD.guardarBBDD(partida);
            if (ok) partida.anadirEvento("PARTIDA GUARDADA CORRECTAMENT.");
            return ok;
        }
        return false;
    }

    /**
     * CARREGA UNA PARTIDA EXISTENT DES DE LA BASE DE DADES UTILITZANT EL SEU IDENTIFICADOR.
     */
    public void cargarPartida(int id) {
        if (gestorBBDD != null) {
            partida = gestorBBDD.cargarBBDD(id);
            partida.anadirEvento("PARTIDA CARREGADA (ID=" + id + ").");
        }
    }
}
