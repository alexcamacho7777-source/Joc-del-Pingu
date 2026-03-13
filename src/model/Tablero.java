package model;

import java.util.ArrayList;
import java.util.Random;

public class Tablero {

    private ArrayList<Casilla> casillas;

    public Tablero() {
        casillas = new ArrayList<>();
        generarTablero();
    }

    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }

    public void setCasillas(ArrayList<Casilla> casillas) {
        this.casillas = casillas;
    }

    public void generarTablero() {

        Random r = new Random();

        for (int i = 0; i < 50; i++) {

            int tipo = r.nextInt(6);

            if (tipo == 0) {
                casillas.add(new Normal(i));
            } 
            else if (tipo == 1) {
                casillas.add(new Oso(i));
            } 
            else if (tipo == 2) {
                casillas.add(new Agujero(i));
            } 
            else if (tipo == 3) {
                casillas.add(new Trineo(i));
            } 
            else if (tipo == 4) {
                casillas.add(new Evento(i));
            } 
            else {
                casillas.add(new SueloQuebradizo(i));
            }
        }
    }

    public Casilla obtenerCasilla(int posicion) {

        if (posicion >= 0 && posicion < casillas.size()) {
            return casillas.get(posicion);
        }

        return null;
    }

    public void actualizarTablero(Partida partida) {

        for (Jugador j : partida.getJugadores()) {

            Casilla c = obtenerCasilla(j.getPosicion());

            if (c != null) {
                c.realizarAccion(partida, j);
            }
        }
    }
}