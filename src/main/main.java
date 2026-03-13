package main;
import controlador.*;

public class main {

    public static void main(String[] args) {

        main juego = new main();
        juego.jugar();

    }

    public void jugar() {

        GestorPartida gestorPartida = new GestorPartida();

        gestorPartida.nuevaPartida();

        System.out.println("Juego iniciado");

    }
}