package ru.runa.wfe.graph.image.figure.bpmn;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;

public class TaskNodeFigure extends AbstractBpmnFigure {
    private static final Color BORDER_COLOR = Color.BLUE;

    @Override
    public void fill(Graphics2D graphics) {
        Rectangle rect = getTextBoundsRectangle();
        graphics.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        if (!renderHits.isPassed()) {
            graphics.setColor(BORDER_COLOR);
        }
        Rectangle rect = getTextBoundsRectangle();
        graphics.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        drawTextInfo(graphics, 1 + DrawProperties.GRID_SIZE / 2);
    }

    @Override
    public Rectangle getTextBoundsRectangle() {
        Rectangle r = getRectangle();
        if (!node.isGraphMinimizedView()) {
            r.grow(-DrawProperties.GRID_SIZE / 2, -DrawProperties.GRID_SIZE / 2);
        }
        return r;
    }

}
