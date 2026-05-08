package model;

/**
 * CLASSE ABSTRACTA QUE REPRESENTA UN JUGADOR DINS DEL JOC.
 * DEFINEIX ELS ATRIBUTS COMUNS COM EL NOM, LA POSICIÓ I L'INVENTARI.
 */
public abstract class Jugador {

    private int posicion;
    private String nombre;
    private String color;
    private boolean esIA;
    private Inventario inv;

    /**
     * CONSTRUCTOR PER INICIALITZAR UN JUGADOR AMB NOM I COLOR.
     * PER DEFECTE COMENÇA A LA POSICIÓ 0 AMB UN INVENTARI BUIT.
     */
    public Jugador(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
        this.posicion = 0;
        this.esIA = false;
        this.inv = new Inventario();
    }

    // GETTERS I SETTERS AMB ACCÉS ESTRUCTURAT
    public boolean isEsIA() { return esIA; }
    public void setEsIA(boolean esIA) { this.esIA = esIA; }

    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Inventario getInv() { return inv; }
    public void setInv(Inventario inv) { this.inv = inv; }

    /**
     * MODIFICA LA POSICIÓ DEL JUGADOR SUMANT EL VALOR INDICAT.
     * ASSEGURA QUE LA POSICIÓ MAI SIGUI NEGATIVA.
     */
    public void moverPosicion(int p) {
        this.posicion += p;
        if (this.posicion < 0) {
            this.posicion = 0;
        }
    }

    @Override
    public String toString() {
        return nombre.toUpperCase() + " (" + color.toUpperCase() + ") - POS: " + posicion;
    }
}
