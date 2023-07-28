package ru.runa.wfe.graph.image.figure.bpmn;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;

public class RoundedRect extends AbstractBpmnFigure {
    private final String imageName;

    public RoundedRect(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public void fill(Graphics2D graphics) {
        if (imageName == null) {
            Rectangle rect = getTextBoundsRectangle();
            graphics.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        }
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        if (imageName == null) {
            drawTextInfo(graphics, 1 + DrawProperties.GRID_SIZE / 2);
        } else {
            drawImageIfNoEdgingOnly(graphics, imageName);
        }
        if (renderHits.isPassed()) {
            Rectangle rect = getTextBoundsRectangle();
            graphics.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        }
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
