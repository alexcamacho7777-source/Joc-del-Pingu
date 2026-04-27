package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Tablero {

    private ArrayList<Casilla> casillas;
    public static final int TOTAL_CASILLAS = 50;

    public Tablero() {
        this.casillas = new ArrayList<>();
        generarTablero(new Random());
    }

    public Tablero(Random r) {
        this.casillas = new ArrayList<>();
        generarTablero(r);
    }

    public ArrayList<Casilla> getCasillas() { return casillas; }
    public void setCasillas(ArrayList<Casilla> casillas) { this.casillas = casillas; }


    private void generarTablero(Random r) {
        casillas.add(new Normal(0)); // inicio
        for (int i = 1; i < TOTAL_CASILLAS - 1; i++) {
            casillas.add(crearCasillaAleatoria(i, r));
        }
        casillas.add(new Normal(TOTAL_CASILLAS - 1)); // meta
    }

    private Casilla crearCasillaAleatoria(int pos, Random r) {
        int tipo = r.nextInt(12); 
        return switch (tipo) {
            case 0 -> new Oso(pos);
            case 1 -> new Agujero(pos);
            case 2 -> new Trineo(pos);
            case 3, 4, 5 -> new Evento(pos); // Más frecuencia de eventos
            case 6 -> new SueloQuebradizo(pos);
            default -> new Normal(pos);
        };
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

	public String getSeed() {
		// TODO Auto-generated method stub
		return null;
	}
}
