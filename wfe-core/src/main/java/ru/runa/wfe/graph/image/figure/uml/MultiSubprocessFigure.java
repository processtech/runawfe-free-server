package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;

public class MultiSubprocessFigure extends TaskNodeFigure {

    protected boolean drawSubprocessImage = true;

    @Override
    public Rectangle getRectangle() {
        if (!node.isGraphMinimizedView()) {
            return new Rectangle(coords[0] + DrawProperties.GRID_SIZE / 2, coords[1], coords[2] - DrawProperties.GRID_SIZE, coords[3]);
        }
        return super.getRectangle();
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        if (!node.isGraphMinimizedView()) {
            if (useEdgingOnly) {
                Rectangle b = getRectangle();
                Color orig = graphics.getColor();
                graphics.setColor(DrawProperties.getBackgroundColor());
                graphics.fillRect(b.x + b.width - 20, b.y + b.height - 10, 10, 8);
                graphics.setColor(orig);
            }
        }
        super.draw(graphics, cleanMode);
        if (!node.isGraphMinimizedView()) {
            Rectangle b = getRectangle();
            int yCenter = b.y + b.height / 2;
            Color orig = graphics.getColor();
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.fillRect(b.x - DrawProperties.GRID_SIZE / 2, yCenter - 3 * DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    3 * DrawProperties.GRID_SIZE);
            graphics.fillRect(b.x + b.width - DrawProperties.GRID_SIZE / 2, yCenter - 3 * DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    3 * DrawProperties.GRID_SIZE);
            graphics.setColor(orig);

            if (drawSubprocessImage) {
                graphics.drawLine(b.x + b.width - 20, b.y + b.height - 10, b.x + b.width - 10, b.y + b.height - 10);
                graphics.drawLine(b.x + b.width - 20, b.y + b.height - 10, b.x + b.width - 20, b.y + b.height - 5);
                graphics.drawLine(b.x + b.width - 15, b.y + b.height - 15, b.x + b.width - 15, b.y + b.height - 5);
                graphics.drawLine(b.x + b.width - 10, b.y + b.height - 10, b.x + b.width - 10, b.y + b.height - 5);
            }

            graphics.drawRect(b.x - DrawProperties.GRID_SIZE / 2, yCenter - 3 * DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    DrawProperties.GRID_SIZE);
            graphics.drawRect(b.x - DrawProperties.GRID_SIZE / 2, yCenter - DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    DrawProperties.GRID_SIZE);
            graphics.drawRect(b.x - DrawProperties.GRID_SIZE / 2, yCenter + DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    DrawProperties.GRID_SIZE);
            graphics.drawRect(b.x + b.width - DrawProperties.GRID_SIZE / 2, yCenter - 3 * DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    DrawProperties.GRID_SIZE);
            graphics.drawRect(b.x + b.width - DrawProperties.GRID_SIZE / 2, yCenter - DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    DrawProperties.GRID_SIZE);
            graphics.drawRect(b.x + b.width - DrawProperties.GRID_SIZE / 2, yCenter + DrawProperties.GRID_SIZE / 2, DrawProperties.GRID_SIZE,
                    DrawProperties.GRID_SIZE);
        }
    }

}
