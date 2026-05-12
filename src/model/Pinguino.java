package model;

/**
 * REPRESENTA UN PERSONATGE PINGÜÍ DINS DEL JOC.
 * ÈS L'ENTITAT PRINCIPAL QUE PODEN CONTROLAR ELS JUGADORS HUMANS O LA IA.
 */
public class Pinguino extends Jugador {

    /**
     * CONSTRUCTOR BÀSIC PER A UN NOU PINGÜÍ.
     */
    public Pinguino(String nombre, String color) {
        super(nombre, color);
    }

    private Dado dadoEquipado;

    /**
     * CONSTRUCTOR DETALLAT PER CARREGAR UN PINGÜÍ AMB POSICIÓ I INVENTARI EXISTENT.
     */
    public Pinguino(String nombre, String color, int posicion, Inventario inv) {
        super(nombre, color);
        this.setPosicion(posicion);
        this.setInv(inv);
        this.dadoEquipado = null;
    }

    public Dado getDadoEquipado() { return dadoEquipado; }
    public void setDadoEquipado(Dado d) { this.dadoEquipado = d; }

    /**
     * AFEGEIX UN ELEMENT A L'INVENTARI DEL PINGÜÍ.
     */
    public void anadirItem(Item i) {
        getInv().anadirItem(i);
    }

    /**
     * ELIMINA UN ELEMENT DE L'INVENTARI DEL PINGÜÍ.
     */
    public void quitarItem(Item i) {
        getInv().quitarItem(i);
    }

    @Override
    public String toString() {
        return super.toString() + " | INVENTARI: " + getInv();
    }
}
