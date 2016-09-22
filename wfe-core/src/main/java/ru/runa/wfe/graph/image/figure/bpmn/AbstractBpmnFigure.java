package ru.runa.wfe.graph.image.figure.bpmn;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.figure.AbstractFigure;

public abstract class AbstractBpmnFigure extends AbstractFigure {
    private final static Stroke DASHED_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[] { 4 }, 2);

    protected void drawImageIfNoEdgingOnly(Graphics2D graphics, String name) {
        drawImageIfNoEdgingOnly(graphics, name, coords[0], coords[1]);
    }

    protected void drawImageIfNoEdgingOnly(Graphics2D graphics, String name, double x, double y) {
        if (!useEgdingOnly) {
            drawImage(graphics, name, x, y);
        }
    }

    protected void drawImage(Graphics2D graphics, String name, double x, double y) {
        try {
            BufferedImage image = ImageIO.read(ClassLoaderUtil.getAsStreamNotNull(name, getClass()));
            graphics.drawRenderedImage(image, AffineTransform.getTranslateInstance(x, y));
        } catch (IOException e) {
            log.error("Unable to paint image", e);
        }
    }

    protected void drawStrokedCircle(Graphics2D graphics, double x, double y) {
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(DASHED_STROKE);
        graphics.draw(new java.awt.geom.Ellipse2D.Double(x, y, 2 * DrawProperties.GRID_SIZE - 1, 2 * DrawProperties.GRID_SIZE - 1));
        graphics.setStroke(oldStroke);
    }

}
