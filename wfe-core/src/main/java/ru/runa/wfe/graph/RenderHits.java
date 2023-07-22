package ru.runa.wfe.graph;

import java.awt.Color;

public class RenderHits {
    private final Color color;
    private final boolean active;
    private final boolean passed;

    public RenderHits(Color color) {
        this(color, false, false);
    }

    public RenderHits(Color color, boolean passed) {
        this(color, passed, false);
    }

    public RenderHits(Color color, boolean passed, boolean active) {
        this.color = color;
        this.passed = passed;
        this.active = active;
    }

    public Color getColor() {
        return color;
    }

    public boolean isPassed() {
        return passed;
    }

    public boolean isActive() {
        return active;
    }
}
