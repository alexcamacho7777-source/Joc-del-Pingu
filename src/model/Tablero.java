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
        int targetOsoCount = r.nextInt(5) + 1; 
        List<Integer> possibleIndices = new ArrayList<>();
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            possibleIndices.add(i);
        }
        java.util.Collections.shuffle(possibleIndices, r);
        List<Integer> osoIndices = possibleIndices.subList(0, targetOsoCount);

        // CASELLA D'INICI
        casillas.add(new Normal(0)); 
        
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            if (osoIndices.contains(i)) {
                casillas.add(new Oso(i));
            } else {
                casillas.add(crearCasillaAleatoriaSinOso(i, r));
            }
        }
        
        // CASELLA DE META
        casillas.add(new Normal(TOTAL_CASILLAS - 1)); 
    }

    /**
     * CREA UNA CASELLA ALEATÒRIA EXCLOENT EL TIPUS 'OS'.
     */
    private Casilla crearCasillaAleatoriaSinOso(int pos, Random r) {
        Casilla c;
        int tipo = r.nextInt(11) + 1; 
        switch (tipo) {
            case 1: c = new Agujero(pos); break;
            case 2: c = new Trineo(pos); break;
            case 3:
            case 4:
            case 5: c = new Evento(pos); break;
            case 6: c = new SueloQuebradizo(pos); break;
            default: c = new Normal(pos); break;
        }
        return c;
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
