package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EndStateFigure extends AbstractUmlFigure {

    @Override
    public void fill(Graphics2D graphics) {
        Rectangle r = getRectangle();
        graphics.fillOval(r.x, r.y, r.width, r.height);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        Rectangle r = getRectangle();
        graphics.fillOval(r.x + 3, r.y + 3, r.width - 5, r.height - 5);
        graphics.drawOval(r.x, r.y, r.width, r.height);
        drawTextInfo(graphics, 20);
    }

    @Override
    public Rectangle getTextBoundsRectangle() {
        return super.getRectangle();
    }

    @Override
    public Rectangle getRectangle() {
        Rectangle r = new Rectangle(coords[0], coords[1], coords[2], coords[3]);
        return new Rectangle((int) (r.getCenterX() - 8), (int) (r.getMinY() + 4), 16, 16);
    }
}
