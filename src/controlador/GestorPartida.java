package controlador;

import model.*;
import java.util.Random;

/**
 * CONTROLADOR CENTRAL DE LA PARTIDA.
 * Aquesta classe actua com l'orquestrador de tota la lògica de negoci del joc.
 * S'encarrega d'unir el Model (Partida, Jugador, Tablero) amb la Vista, 
 * gestionant el flux de dades, el moviment, les interaccions entre personatges 
 * i la persistència a la base de dades.
 * 
 * @author Alex Camacho
 * @version 2.5
 */
public class GestorPartida {

    // Referència a la partida activa
    private Partida partida;
    
    // Gestors auxiliars especialitzats per repartir la responsabilitat
    private GestorTablero gestorTablero;
    private GestorJugador gestorJugador;
    
    // Capa de persistència amb Oracle
    private GestorBBDD gestorBBDD;
    
    // Generador aleatori centralitzat
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

    /** @return La partida que s'està gestionant actualment. */
    public Partida getPartida() { return partida; }
    /** @param partida Assigna una partida ja creada (ex: des de la vista). */
    public void setPartida(Partida partida) { this.partida = partida; }

    /** @return El gestor encarregat de les accions de les caselles. */
    public GestorTablero getGestorTablero() { return gestorTablero; }
    /** @return El gestor encarregat de les interaccions entre jugadors. */
    public GestorJugador getGestorJugador() { return gestorJugador; }
    
    /** @return El gestor de connexió a BBDD Oracle. */
    public GestorBBDD getGestorBBDD() { return gestorBBDD; }
    /** @param gestorBBDD Injecta la dependència de base de dades. */
    public void setGestorBBDD(GestorBBDD gestorBBDD) { this.gestorBBDD = gestorBBDD; }
    
    /** @return El generador aleatori utilitzat. */
    public Random getRandom() { return random; }

    // ── GESTIÓ DEL CICLE DE VIDA DE LA PARTIDA ───────────────────────────────

    /**
     * CREA UNA NOVA PARTIDA BUIDA I REGISTRA L'INICI AL LOG D'EVENTS.
     * Inicialitza el model i prepara el Random.
     */
    public void nuevaPartida() {
        partida = new Partida();
        partida.setRandom(random);
        partida.anadirEvento("S'HA INICIALITZAT UNA NOVA PARTIDA.");
    }

    /**
     * GESTIONA LA TIRADA D'UN DAU PER A UN JUGADOR.
     * @param j Jugador que tira.
     * @param dado El tipus de dau (normal o especial) a utilitzar.
     * @return El resultat numèric obtingut (1-6 o segons el dau).
     */
    public int tirarDado(Jugador j, Dado dado) {
        int resultado = dado.tirar(random);
        j.moverPosicion(resultado);
        // Protecció: no podem passar de la darrera casella
        int maxPos = partida.getTablero().getTotalCasillas() - 1;
        if (j.getPosicion() > maxPos) {
            j.setPosicion(maxPos);
        }
        return resultado;
    }

    /**
     * EXECUTA EL TORN COMPLET DEL JUGADOR ACTUAL.
     * Aquest mètode s'utilitza en el flux de lògica sense interfície gràfica 
     * o per a simulacions ràpides.
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
     * Controla de forma seqüencial tots els passos: comprovació de bloqueig, 
     * moviment, efecte de casella i interaccions amb altres jugadors.
     * @param jugadorTurno El jugador a qui toca tirar.
     * @param jugadorActivo El jugador que està realitzant l'acció (solen ser el mateix).
     */
    public void procesarTurnoJugador(Jugador jugadorTurno, Jugador jugadorActivo) {
        // 1. VERIFICACIÓ DE SI EL JUGADOR HA DE PERDRE EL TORN (Càstig de la ruleta)
        if (partida.getJugadorPierdeTurno() != null && partida.getJugadorPierdeTurno().equals(jugadorTurno)) {
            partida.setJugadorPierdeTurno(null);
            partida.anadirEvento(jugadorTurno.getNombre().toUpperCase() + " PERD EL TORN PER UN ESDEVENIMENT PREVI.");
            partida.siguienteTurno();
        } else {
            boolean bloqueado = false;
            // 2. GESTIÓ DEL BLOQUEIG ESPECÍFIC DE LA FOCA (Si ha rebut una bola de neu)
            if (jugadorTurno instanceof Foca foca) {
                // Si la foca ha arribat a la meta (NPC), ja no es mou més.
                int meta = partida.getTablero().getTotalCasillas() - 1;
                if (foca.getPosicion() >= meta) {
                    partida.anadirEvento("LA FOCA JA ÉS A LA META I NO REALITZA MÉS ACCIONS.");
                    partida.siguienteTurno();
                    return;
                }

                if (foca.getTurnosBloqueada() > 0) {
                    foca.reducirBloqueo();
                    partida.anadirEvento("LA FOCA ESTÀ CONGELADA I NO POT MOURE'S (" + foca.getTurnosBloqueada() + " TORNS RESTANTS).");
                    partida.siguienteTurno();
                    bloqueado = true;
                }
            }

            if (!bloqueado) {
                // 3. MOVIMENT: TIRADA DE DAU (CONSIDERANT IA SI CAL)
                int pasos = tirarDadoParaJugador(jugadorTurno);
                int posAnterior = jugadorTurno.getPosicion();
                jugadorTurno.moverPosicion(pasos);

                // LIMITACIÓ A L'ÚLTIMA CASELLA DEL TAULELL (META)
                int maxPos = partida.getTablero().getTotalCasillas() - 1;
                if (jugadorTurno.getPosicion() > maxPos) {
                    jugadorTurno.setPosicion(maxPos);
                }
                int posNueva = jugadorTurno.getPosicion();

                partida.anadirEvento(jugadorTurno.getNombre().toUpperCase() + " HA AVANÇAT " + pasos + " CASELLES.");

                // 4. LÒGICA DE LA FOCA: ROBA ÍTEMS SI PASSA PER SOBRE D'UN JUGADOR HUMÀ
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
     * DETERMINA EL NÚMERO DE CASELLES A MOURE SEGONS EL JUGADOR.
     * Si el jugador és una IA (CPU), el gestor pot decidir utilitzar un dau 
     * especial si el té a l'inventari.
     * @param j Jugador que es mourà.
     * @return Resultat del dau.
     */
    public int tirarDadoParaJugador(Jugador j) {
        int r = 0;
        boolean usatEspecial = false;
        
        if (j instanceof Pinguino p) {
            // 1. Prioritat: Si el jugador ha equipat un dau des de la UI o IA
            if (p.getDadoEquipado() != null) {
                r = usarDadoEspecial(p, p.getDadoEquipado());
                p.setDadoEquipado(null); // Consumit
                usatEspecial = true;
            } 
            // 2. Si és IA i no tenia dau equipat, decideix si en vol usar un
            else if (p.isEsIA()) {
                Item it = decidirDadoIA(p);
                if (it instanceof Dado d) {
                    r = usarDadoEspecial(p, d);
                    
                    // Com que l'IA no usa la UI, hem de consumir el dau aquí
                    d.setCantidad(d.getCantidad() - 1);
                    if (d.getCantidad() <= 0) p.getInv().quitarItem(d);
                    
                    usatEspecial = true;
                }
            }
        }
        
        // 3. Si no s'ha usat un dau especial, tirem el dau normal (1-6)
        if (!usatEspecial) {
            r = 1 + random.nextInt(6);
        }
        return r;
    }

    /**
     * LÒGICA DE DECISIÓ PER A LA INTEL·LIGÈNCIA ARTIFICIAL PER ESCOLLIR DAUS.
     * @param p El pinguí de la CPU.
     * @return El dau seleccionat o null si decideix usar el normal.
     */
    private Item decidirDadoIA(Pinguino p) {
        Item res = null;
        // Probabilitat del 40% d'usar un dau especial si en té un.
        int prob = random.nextInt(100);
        if (prob < 40) { 
            res = p.getInv().getItem(Dado.class);
        }
        return res;
    }

    /**
     * UTILITZA UN DAU ESPECIAL I EN REDUEIX LA QUANTITAT DE L'INVENTARI.
     * @param p Jugador que l'utilitza.
     * @param d Dau especial (ràpid o lent).
     * @return El resultat obtingut de la tirada.
     */
    public int usarDadoEspecial(Pinguino p, Dado d) {
        // Retornem directament el resultat. El consum de l'ítem ja es fa quan
        // s'equipa des de la UI o quan la IA el decideix usar.
        return d.tirar(random);
    }

    /**
     * PERMET A LA IA REALITZAR ACCIONS PROACTIVES AVANS DE MOURE'S.
     * (Reservat per a futures millores d'intel·ligència artificial).
     */
    public void realizarAccionesIA(Jugador j) {
        if (j instanceof Pinguino p) {
            if (p.isEsIA()) {
                // La IA intenta usar daus especials si els té
                Item d = p.getInv().getItem(Dado.class);
                if (d != null && random.nextInt(100) < 30) {
                   // Usat automàticament en el mètode tirarDadoParaJugador
                }
            }
        }
    }

    /**
     * Implementa les regles de contacte: suborn a la foca o guerra de boles de neu.
     * @param p Jugador que acaba d'aterrar en la casella o que és colpejat.
     */
    public void comprobarInteraccionesEnCasilla(Jugador p) {
        int meta = partida.getTablero().getTotalCasillas() - 1;
        // Llista temporal per evitar ConcurrentModificationException
        java.util.List<Jugador> copiaJugadors = new java.util.ArrayList<>(partida.getJugadores());
        
        for (Jugador otro : copiaJugadors) {
            if (otro != p && otro.getPosicion() == p.getPosicion() && p.getPosicion() > 0 && p.getPosicion() < meta) {
                
                // CAS A: INTERACCIÓ AMB LA FOCA (El jugador cau on és la foca)
                if (otro instanceof Foca foca) {
                    interactuarAmbFoca(p, foca);
                } 
                // CAS B: LA FOCA CAU ON ÉS EL JUGADOR
                else if (p instanceof Foca foca && otro instanceof Pinguino pin) {
                    interactuarAmbFoca(pin, foca);
                }
                // CAS C: INTERACCIÓ ENTRE PINGÜINS (Guerra de boles)
                else if (otro instanceof Pinguino p2 && p instanceof Pinguino p1) {
                    anadirEvento("⚔️ GUERRA DE BOLES ENTRE " + p1.getNombre().toUpperCase() + " I " + p2.getNombre().toUpperCase() + "!");
                    gestorJugador.pinguinoGuerraQuema(p1, p2);
                }
            }
        }
    }

    /** Lògica de la foca colpejant o sent alimentada */
    private void interactuarAmbFoca(Jugador p, Foca foca) {
        if (foca.isSobornada()) {
            anadirEvento("🐟 LA FOCA ESTÀ BLOQUEJADA I NO ATACA A " + p.getNombre().toUpperCase() + ".");
            return;
        }

        if (p instanceof Pinguino pin) {
            Item pez = pin.getInv().getItem(Pez.class);
            if (pez != null) {
                // El jugador té un peix, l'alimenta
                pin.getInv().quitarUnidadAleatoria(random);
                foca.activarSoborno();
                anadirEvento("🍣 " + pin.getNombre().toUpperCase() + " HA ALIMENTAT LA FOCA! QUEDA BLOQUEJADA 2 TORNS.");
            } else {
                // No té peix, la foca el colpeja
                pin.setPosicion(0);
                anadirEvento("💥 LA FOCA HA GOLPEJAT A " + pin.getNombre().toUpperCase() + " I L'ENVIA A L'INICI!");
            }
        }
    }

    /** Afegeix un missatge al log del model i a la vista si és possible */
    private void anadirEvento(String msg) {
        partida.anadirEvento(msg);
    }

    /**
     * PROCESSA EL ROBATORI D'ÍTEMS QUAN LA FOCA PASSA PER SOBRE D'UN JUGADOR.
     * Si la foca no està subornada, roba la meitat de l'inventari a qui es trobi pel camí.
     * @return Mapa amb els jugadors afectats i els ítems que han perdut.
     */
    public java.util.Map<Pinguino, java.util.List<String>> procesarPasoDeFoca(Foca foca, int posAnterior, int posNueva) {
        java.util.Map<Pinguino, java.util.List<String>> robos = new java.util.HashMap<>();
        
        if (!foca.isSobornada()) {
            int start = Math.min(posAnterior, posNueva);
            int end = Math.max(posAnterior, posNueva);

            for (Jugador j : partida.getJugadores()) {
                if (j instanceof Pinguino p) {
                    // Verifiquem si el pinguí està dins del rang de moviment de la foca
                    if (p.getPosicion() != 0 && p.getPosicion() > start && p.getPosicion() < end) {
                        java.util.List<String> perdidos = perderUnObjecte(p);
                        if (!perdidos.isEmpty()) {
                            robos.put(p, perdidos);
                            partida.anadirEvento("🦭 LA FOCA HA ROBAT UN OBJECTE A " + p.getNombre().toUpperCase() + " AL PASSAR.");
                        }
                    }
                }
            }
        }
        return robos;
    }

    /**
     * ELIMINA UN ÍTEM DE L'INVENTARI D'UN JUGADOR DE FORMA ALEATÒRIA.
     * @param p Jugador afectat.
     * @return Llista de noms d'ítems perduts per mostrar-ho al log.
     */
    private java.util.List<String> perderUnObjecte(Jugador p) {
        java.util.List<String> perdidos = new java.util.ArrayList<>();
        if (p.getInv() != null && p.getInv().totalItems() > 0) {
            Item stack = p.getInv().quitarUnidadAleatoria(random);
            if (stack != null) {
                perdidos.add(stack.getNombre().toUpperCase());
            }
        }
        return perdidos;
    }

    /**
     * VERIFICA SI ALGUN JUGADOR HA ARRIBAT A LA CASELLA FINAL I DETERMINA EL GUANYADOR.
     * Actualitza l'estat de la partida a finalitzada, registra el guanyador i guarda a BBDD.
     */
    public void actualizarEstadoTablero() {
        int meta = partida.getTablero().getTotalCasillas() - 1;
        for (Jugador j : partida.getJugadores()) {
            if (j.getPosicion() >= meta && !(j instanceof Foca)) {
                partida.setGanador(j);
                partida.setFinalizada(true);
                anadirEvento("🏆 ¡¡¡ " + j.getNombre().toUpperCase() + " HA ARRIBAT A LA META I GUANYA LA PARTIDA !!!");
                
                // GUARDEM AUTOMÀTICAMENT PERQUÈ ES REGISTRI LA VICTÒRIA A LES ESTADÍSTIQUES
                guardarPartida();
            }
        }
    }

    /**
     * AVANÇA EL TORN AL SEGÜENT JUGADOR SEGONS L'ORDRE ESTABLERT EN EL MODEL.
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
     * Requereix que el GestorBBDD estigui prèviament configurat.
     * @return true si s'ha guardat correctament.
     */
    public boolean guardarPartida() {
        boolean exit = false;
        if (gestorBBDD != null && partida != null) {
            exit = gestorBBDD.guardarBBDD(partida);
            if (exit) {
                partida.anadirEvento("PROGRÉS GUARDAT AL SERVIDOR.");
            }
        }
        return exit;
    }

    /**
     * CARREGA UNA PARTIDA EXISTENT DES DE LA BASE DE DADES UTILITZANT EL SEU IDENTIFICADOR.
     * @param id L'identificador de la partida en l'esquema Oracle.
     */
    public void cargarPartida(int id) {
        if (gestorBBDD != null) {
            partida = gestorBBDD.cargarBBDD(id);
            if (partida != null) {
                partida.anadirEvento("DADES CARREGADES CORRECTAMENT (ID=" + id + ").");
            }
        }
    }
}
