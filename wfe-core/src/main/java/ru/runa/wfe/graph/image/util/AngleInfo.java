package ru.runa.wfe.graph.image.util;

public class AngleInfo {
    public static final int QUARTER_I = 1;
    public static final int QUARTER_II = 2;
    public static final int QUARTER_III = 3;
    public static final int QUARTER_IV = 4;

    private double angle;
    private int quarter;

    public AngleInfo() {
    }

    public AngleInfo(double angle, int quarter) {
        setAngle(angle);
        setQuarter(quarter);
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int quarter) {
        this.quarter = quarter;
    }
}
