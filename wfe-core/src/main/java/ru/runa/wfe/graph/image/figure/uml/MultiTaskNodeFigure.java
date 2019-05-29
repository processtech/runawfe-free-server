package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;

public class MultiTaskNodeFigure extends MultiSubprocessFigure {

    public MultiTaskNodeFigure() {
        drawSubprocessImage = false;
    }

    @Override
    public Rectangle getRectangle() {
        if (!node.isGraphMinimizedView()) {
            return new Rectangle(coords[0] + DrawProperties.GRID_SIZE, coords[1], coords[2] - 2 * DrawProperties.GRID_SIZE,
                    coords[3] - DrawProperties.GRID_SIZE);
        }
        return super.getRectangle();
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        super.draw(graphics, cleanMode);
        if (!useEdgingOnly) {
            Rectangle b = getRectangle();
            graphics.drawString("*", b.x + b.width - 20, b.y + b.height - 10);
        }
    }

}
