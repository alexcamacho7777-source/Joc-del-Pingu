package model;

import java.util.ArrayList;

public class Tablero {

    private ArrayList<Casilla> casillas;

    public Tablero() {
        casillas = new ArrayList<>();
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public void setCasillas(ArrayList<Casilla> casillas) {
        this.casillas = casillas;
    }

    public void actualizarTablero() {
        System.out.println("Tablero actualizado");
    }
}