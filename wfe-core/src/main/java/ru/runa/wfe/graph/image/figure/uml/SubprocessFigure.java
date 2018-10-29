package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;

public class SubprocessFigure extends TaskNodeFigure {

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        super.draw(graphics, cleanMode);
        if (useEdgingOnly) {
            Color orig = graphics.getColor();
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.fillRect(coords[0] + coords[2] - 20, coords[1] + coords[3] - 10, 10, 10);
            graphics.setColor(orig);
        }
        if (!node.isGraphMinimizedView()) {
            graphics.drawLine(coords[0] + coords[2] - 20, coords[1] + coords[3] - 10, coords[0] + coords[2] - 10, coords[1] + coords[3] - 10);
            graphics.drawLine(coords[0] + coords[2] - 20, coords[1] + coords[3] - 10, coords[0] + coords[2] - 20, coords[1] + coords[3] - 5);
            graphics.drawLine(coords[0] + coords[2] - 15, coords[1] + coords[3] - 15, coords[0] + coords[2] - 15, coords[1] + coords[3] - 5);
            graphics.drawLine(coords[0] + coords[2] - 10, coords[1] + coords[3] - 10, coords[0] + coords[2] - 10, coords[1] + coords[3] - 5);
        }
    }

}
