package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tablero {

    private ArrayList<Casilla> casillas;
    private String seed = "";
    public static final int TOTAL_CASILLAS = 50;

    public Tablero() {
        this.casillas = new ArrayList<>();
        this.seed = String.valueOf(System.currentTimeMillis());
        generarTablero(new Random(Long.parseLong(seed)));
    }

    public Tablero(Random r) {
        this.casillas = new ArrayList<>();
        this.seed = "random_init";
        generarTablero(r);
    }

    public ArrayList<Casilla> getCasillas() { return casillas; }
    public void setCasillas(ArrayList<Casilla> casillas) { this.casillas = casillas; }
    
    public String getSeed() { return seed; }
    public void setSeed(String seed) { this.seed = seed; }



    private void generarTablero(Random r) {
        int targetOsoCount = r.nextInt(5) + 1; // 1 a 5 osos
        List<Integer> possibleIndices = new ArrayList<>();
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            possibleIndices.add(i);
        }
        java.util.Collections.shuffle(possibleIndices, r);
        List<Integer> osoIndices = possibleIndices.subList(0, targetOsoCount);

        casillas.add(new Normal(0)); // inicio
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            if (osoIndices.contains(i)) {
                casillas.add(new Oso(i));
            } else {
                casillas.add(crearCasillaAleatoriaSinOso(i, r));
            }
        }
        casillas.add(new Normal(TOTAL_CASILLAS - 1)); // meta
    }

    private Casilla crearCasillaAleatoriaSinOso(int pos, Random r) {
        // Probabilidades originales pero sin el Oso (que era el caso 0)
        int tipo = r.nextInt(11) + 1; // 1 a 11
        switch (tipo) {
            case 1: return new Agujero(pos);
            case 2: return new Trineo(pos);
            case 3:
            case 4:
            case 5: return new Evento(pos); 
            case 6: return new SueloQuebradizo(pos);
            default: return new Normal(pos);
        }
    }

    public Casilla getCasilla(int pos) {
        if (pos < 0) return casillas.get(0);
        if (pos >= casillas.size()) return casillas.get(casillas.size() - 1);
        return casillas.get(pos);
    }

    public int buscarAgujeroAnterior(int posActual) {
        for (int i = posActual - 1; i >= 0; i--) {
            if (casillas.get(i) instanceof Agujero) return i;
        }
        return 0;
    }

    public int buscarSiguienteTrineo(int posActual) {
        for (int i = posActual + 1; i < casillas.size(); i++) {
            if (casillas.get(i) instanceof Trineo) return i;
        }
        return -1;
    }

    public void actualizarTablero() {
    }

    public int getTotalCasillas() {
        return casillas.size();
    }
}
