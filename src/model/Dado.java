package model;

import java.util.Random;


public class Dado extends Item {

    private int min;
    private int max;

    public Dado(String nombre, int cantidad, int min, int max) {
        super(nombre, cantidad);
        this.min = min;
        this.max = max;
    }

    public int getMin() { 
    	return min; 
    	}
    public void setMin(int min) { 
    	this.min = min; 
    	}

    public int getMax() 
    { 
    	return max; 
    	}
    
    public void setMax(int max) { 
    	this.max = max; 
    	}
    
    public int tirar(Random r) {
        return min + r.nextInt(max - min + 1);
    }
}
