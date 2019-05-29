package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

public class StartStateFigure extends AbstractUmlFigure {

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        Rectangle r = getRectangle();
        graphics.fillOval(r.x - 1, r.y - 1, r.width, r.height);
        drawTextInfo(graphics, 1);
    }

    @Override
    public Rectangle getRectangle() {
        Rectangle r = new Rectangle(coords[0], coords[1], coords[2], coords[3]);
        return new Rectangle((int) (r.getCenterX() - 7), (int) r.getMaxY() - 20, 16, 16);
    }

    @Override
    public Point getBendpoint() {
        return null;
    }
}
