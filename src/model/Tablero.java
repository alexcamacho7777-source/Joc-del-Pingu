package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * REPRESENTA EL TAULELL DE JOC.
 * GESTIONA LA GENERACIÓ PROCEDIMENTAL DE LES CASELLES I LES CONSULTES DE POSICIÓ.
 */
public class Tablero {

    private ArrayList<Casilla> casillas;
    private String seed = "";
    public static final int TOTAL_CASILLAS = 50;

    /**
     * CONSTRUCTOR PER DEFECTE QUE GENERA UN TAULELL BASAT EN EL TEMPS ACTUAL.
     */
    public Tablero() {
        this.casillas = new ArrayList<>();
        this.seed = String.valueOf(System.currentTimeMillis());
        generarTablero(new Random(Long.parseLong(seed)));
    }

    /**
     * CONSTRUCTOR QUE REP UN GENERADOR ALEATORI ESPECÍFIC (PER A REPRODUIR TAULELLS).
     */
    public Tablero(Random r) {
        this.casillas = new ArrayList<>();
        this.seed = "RANDOM_INIT";
        generarTablero(r);
    }

    // GETTERS I SETTERS
    public ArrayList<Casilla> getCasillas() { return casillas; }
    public void setCasillas(ArrayList<Casilla> casillas) { this.casillas = casillas; }
    
    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }

    /**
     * GENERA LA DISPOSICIÓ DE LES CASELLES DEL TAULELL.
     * DISTRIBUEIX OSOS, FORATS, TRINEUS I ESDEVENIMENTS DE FORMA ALEATÒRIA.
     */
    private void generarTablero(Random r) {
        // CASELLA D'INICI
        casillas.add(new Normal(0)); 
        
        // Assegurem que hi hagi almenys un de cada tipus especial
        List<Integer> specialIndices = new ArrayList<>();
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            specialIndices.add(i);
        }
        java.util.Collections.shuffle(specialIndices, r);
        
        // Assignem manualment els primers índexs barrejats a cada tipus per garantir presència
        int idx = 0;
        casillas.addAll(new ArrayList<>(java.util.Collections.nCopies(TOTAL_CASILLAS - 2, null)));
        
        // Tipus obligatoris (almenys 1)
        casillas.set(specialIndices.get(idx++), new Oso(0)); // Posició temporal
        casillas.set(specialIndices.get(idx++), new Agujero(0));
        casillas.set(specialIndices.get(idx++), new Trineo(0));
        casillas.set(specialIndices.get(idx++), new Evento(0));
        casillas.set(specialIndices.get(idx++), new SueloQuebradizo(0));

        // Omplim la resta amb probabilitats millorades (menys normals)
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            if (casillas.get(i) == null) {
                casillas.set(i, crearCasillaAleatoriaMillorada(i, r));
            } else {
                // Corregim la posició interna de les obligatòries
                Casilla c = casillas.get(i);
                if (c instanceof Oso) casillas.set(i, new Oso(i));
                else if (c instanceof Agujero) casillas.set(i, new Agujero(i));
                else if (c instanceof Trineo) casillas.set(i, new Trineo(i));
                else if (c instanceof Evento) casillas.set(i, new Evento(i));
                else if (c instanceof SueloQuebradizo) casillas.set(i, new SueloQuebradizo(i));
            }
        }
        
        // CASELLA DE META
        casillas.add(new Normal(TOTAL_CASILLAS - 1)); 
    }

    /**
     * CREA UNA CASELLA ALEATÒRIA AMB PROBABILITATS AJUSTADES (MENYS NORMALS).
     */
    private Casilla crearCasillaAleatoriaMillorada(int pos, Random r) {
        int tipo = r.nextInt(100);
        if (tipo < 15) return new Agujero(pos);      // 15%
        if (tipo < 30) return new Trineo(pos);       // 15%
        if (tipo < 55) return new Evento(pos);       // 25%
        if (tipo < 70) return new Oso(pos);          // 15%
        if (tipo < 85) return new SueloQuebradizo(pos); // 15%
        return new Normal(pos);                      // 15% (Molt menys que abans)
    }

    /**
     * RETORNA LA CASELLA EN UNA POSICIÓ DETERMINADA AMB PROTECCIÓ DE RANG.
     */
    public Casilla getCasilla(int pos) {
        Casilla c;
        if (pos < 0) {
            c = casillas.get(0);
        } else if (pos >= casillas.size()) {
            c = casillas.get(casillas.size() - 1);
        } else {
            c = casillas.get(pos);
        }
        return c;
    }

    /**
     * CERCA EL FORAT ANTERIOR MÉS PROPER DES D'UNA POSICIÓ DONADA.
     */
    public int buscarAgujeroAnterior(int posActual) {
        int res = 0;
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

    public int getTotalCasillas() {
        return casillas.size();
    }
}
