package ru.runa.wfe.graph.image.util;

/**
 * Presents line equation in form y(x) = k*x + b.
 * 
 * @author dofs
 */
public class Line {
    private final double k;
    private final double b;
    private final boolean kExists;

    public Line(double k, double b, boolean kExists) {
        this.k = k;
        this.b = b;
        this.kExists = kExists;
    }

    public double getB() {
        return b;
    }

    public double getK() {
        return k;
    }

    public boolean isKExists() {
        return kExists;
    }
}
