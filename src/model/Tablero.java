package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * REPRESENTA EL TAULELL DE JOC.
 * Aquesta classe és l'encarregada de definir l'escenari on transcorre la partida.
 * Inclou la lògica de generació procedimental de caselles, assegurant que cada 
 * partida tingui un repte diferent però equilibrat. També proporciona mètodes 
 * de cerca per a la interacció amb elements especials (forats, trineus).
 * 
 * @author Alex Camacho
 * @version 1.2
 */
public class Tablero {

    // Llista ordenada de caselles que formen el camí del joc
    private ArrayList<Casilla> casillas;
    
    // Cadena de text que s'utilitza com a base per a la generació aleatòria
    private String seed = "";
    
    // Constant que defineix la longitud estàndard del taulell
    public static final int TOTAL_CASILLAS = 50;

    /**
     * CONSTRUCTOR PER DEFECTE QUE GENERA UN TAULELL BASAT EN EL TEMPS ACTUAL.
     * Utilitza el timestamp del sistema com a seed per garantir aleatorietat.
     */
    public Tablero() {
        this.casillas = new ArrayList<>();
        this.seed = String.valueOf(System.currentTimeMillis());
        generarTablero(new Random(Long.parseLong(seed)));
    }

    /**
     * CONSTRUCTOR QUE REP UN GENERADOR ALEATORI ESPECÍFIC.
     * Permet reproduir el mateix taulell si es coneix la seed original (útil per a càrregues).
     * @param r Objecte Random ja inicialitzat.
     */
    public Tablero(Random r) {
        this.casillas = new ArrayList<>();
        this.seed = "RANDOM_INIT";
        generarTablero(r);
    }

    // ── MÈTODES D'ACCÉS (GETTERS I SETTERS) ──────────────────────────────────
    
    /** @return La llista de caselles del taulell. */
    public ArrayList<Casilla> getCasillas() { return casillas; }
    /** @param casillas Nova llista de caselles. */
    public void setCasillas(ArrayList<Casilla> casillas) { this.casillas = casillas; }
    
    /** @return La llavor (seed) utilitzada per generar el taulell. */
    public String getSeed() { return seed; }
    /** @param seed Nova llavor per a futures regeneracions. */
    public void setSeed(String seed) { this.seed = seed; }

    /**
     * GENERA LA DISPOSICIÓ DE LES CASELLES DEL TAULELL.
     * Aquest mètode implementa un algorisme de distribució que garanteix la 
     * presència d'almenys un element de cada tipus especial (Oso, Agujero, etc.)
     * per evitar taulells massa simples o impossibles.
     * @param r El generador aleatori a utilitzar.
     */
    private void generarTablero(Random r) {
        // 1. CASELLA D'INICI: Sempre és de tipus Normal (posició 0)
        casillas.add(new Normal(0)); 
        
        // 2. PREPARACIÓ D'ÍNDEXS PER A CASELLES ESPECIALS OBLIGATÒRIES
        // Creem una llista d'índexs del 1 al 48 (excloent inici i meta) i els barregem.
        List<Integer> specialIndices = new ArrayList<>();
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            specialIndices.add(i);
        }
        java.util.Collections.shuffle(specialIndices, r);
        
        // Inicialitzem la llista amb nulls per poder fer 'set' en posicions aleatòries
        casillas.addAll(new ArrayList<>(java.util.Collections.nCopies(TOTAL_CASILLAS - 2, null)));
        
        // 3. ASSIGNACIÓ DE TIPUS OBLIGATORIS (Garantim varietat)
        int idx = 0;
        casillas.set(specialIndices.get(idx++), new Oso(0));             // Un os
        casillas.set(specialIndices.get(idx++), new Agujero(0));         // Un forat
        casillas.set(specialIndices.get(idx++), new Trineo(0));         // Un trineu
        casillas.set(specialIndices.get(idx++), new Evento(0));         // Un esdeveniment (?)
        casillas.set(specialIndices.get(idx++), new SueloQuebradizo(0)); // Un terra fràgil

        // 4. OMPLIM LA RESTA DE CASELLES AMB PROBABILITATS AJUSTADES
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            if (casillas.get(i) == null) {
                // Si la posició està buida, creem una casella aleatòria
                casillas.set(i, crearCasillaAleatoriaMillorada(i, r));
            } else {
                // Si ja hi havia una obligatòria, li corregim la seva posició interna
                Casilla c = casillas.get(i);
                if (c instanceof Oso) casillas.set(i, new Oso(i));
                else if (c instanceof Agujero) casillas.set(i, new Agujero(i));
                else if (c instanceof Trineo) casillas.set(i, new Trineo(i));
                else if (c instanceof Evento) casillas.set(i, new Evento(i));
                else if (c instanceof SueloQuebradizo) casillas.set(i, new SueloQuebradizo(i));
            }
        }
        
        // 5. CASELLA DE META: Sempre és de tipus Normal (posició 49)
        casillas.add(new Normal(TOTAL_CASILLAS - 1)); 
    }

    /**
     * CREA UNA CASELLA ALEATÒRIA AMB PROBABILITATS AJUSTADES.
     * S'han reduït les probabilitats de caselles 'Normals' per fer el joc més dinàmic.
     * @param pos Posició que ocuparà la casella.
     * @param r Generador aleatori.
     * @return Instància d'una subclasse de Casilla.
     */
    private Casilla crearCasillaAleatoriaMillorada(int pos, Random r) {
        int tipo = r.nextInt(100);
        if (tipo < 15) return new Agujero(pos);      // 15% Forats
        if (tipo < 30) return new Trineo(pos);       // 15% Trineus
        if (tipo < 55) return new Evento(pos);       // 25% Esdeveniments (Ruleta)
        if (tipo < 70) return new Oso(pos);          // 15% Óssos polars
        if (tipo < 85) return new SueloQuebradizo(pos); // 15% Gel trencadís
        return new Normal(pos);                      // 15% Caselles sense efecte
    }

    /**
     * RETORNA LA CASELLA EN UNA POSICIÓ DETERMINADA AMB PROTECCIÓ DE RANG.
     * Si la posició és negativa, retorna la 0; si és massa gran, retorna l'última.
     * @param pos Índex de la casella.
     * @return Objecte Casilla corresponent.
     */
    public Casilla getCasilla(int pos) {
        if (pos < 0) {
            return casillas.get(0);
        } else if (pos >= casillas.size()) {
            return casillas.get(casillas.size() - 1);
        } else {
            return casillas.get(pos);
        }
    }

    /**
     * CERCA EL FORAT ANTERIOR MÉS PROPER DES D'UNA POSICIÓ DONADA.
     * Utilitzat quan un jugador és colpejat per la foca i ha de retrocedir.
     * @param posActual Posició des d'on es comença a buscar cap enrere.
     * @return Índex de la casella de tipus Agujero trobada o 0 (inici).
     */
    public int buscarAgujeroAnterior(int posActual) {
        int res = 0; // Per defecte tornem a l'inici si no hi ha forats previs
        boolean trobat = false;
        for (int i = posActual - 1; i >= 0 && !trobat; i--) {
            if (casillas.get(i) instanceof Agujero) {
                res = i;
                trobat = true;
            }
        }
        return res;
    }

    /**
     * CERCA EL SEGÜENT TRINEU DISPONIBLE AL TAULELL.
     * @param posActual Posició des d'on es comença a buscar cap endavant.
     * @return Índex de la casella de tipus Trineo o -1 si no n'hi ha cap més.
     */
    public int buscarSiguienteTrineo(int posActual) {
        int res = -1;
        boolean trobat = false;
        for (int i = posActual + 1; i < casillas.size() && !trobat; i++) {
            if (casillas.get(i) instanceof Trineo) {
                res = i;
                trobat = true;
            }
        }
        return res;
    }

    /** @return El número total de caselles que formen el taulell. */
    public int getTotalCasillas() {
        return casillas.size();
    }
}
