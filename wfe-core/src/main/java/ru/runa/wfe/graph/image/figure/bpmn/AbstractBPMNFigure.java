package ru.runa.wfe.graph.image.figure.bpmn;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;
import ru.runa.wfe.lang.bpmn2.TimerNode;

public abstract class AbstractBpmnFigure extends AbstractFigure {
    private final static Stroke DASHED_STROKE = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[] { 4 }, 2);

    @Override
    public Point getTransitionPoint(Transition transition, double x, double y) {
        // TODO 212
        if (transition != null && transition.isTimerTransition() && node.getNodeType() != NodeType.WAIT_STATE) {
            return new Point(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE);
        }
        return super.getTransitionPoint(transition, x, y);
    }

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

    protected void drawBoundaryEvents(Graphics2D graphics) {
        if (node instanceof BoundaryEventContainer) {
            for (BoundaryEvent boundaryEvent : ((BoundaryEventContainer) node).getBoundaryEvents()) {
                if (boundaryEvent instanceof TimerNode) {
                    String fileName = "image/bpmn/boundary_timer.png";
                    drawImage(graphics, fileName, coords[0] + 1, coords[1] + coords[3] - 2 * DrawProperties.GRID_SIZE);
                    if (!boundaryEvent.isBoundaryEventInterrupting()) {
                        drawStrokedCircle(graphics, coords[0] + 1, coords[1] + coords[3] - 2 * DrawProperties.GRID_SIZE);
                    }
                }
                if (boundaryEvent instanceof CatchEventNode) {
                    String type = ((CatchEventNode) boundaryEvent).getEventType().name();
                    String fileName = "image/bpmn/boundary_catch_" + type + ".png";
                    drawImage(graphics, fileName, coords[0] + coords[2] - 2 * DrawProperties.GRID_SIZE, coords[1] + coords[3] - 2
                            * DrawProperties.GRID_SIZE);
                    if (!boundaryEvent.isBoundaryEventInterrupting()) {
                        drawStrokedCircle(graphics, coords[0] + coords[2] - 2 * DrawProperties.GRID_SIZE, coords[1] + coords[3] - 2
                                * DrawProperties.GRID_SIZE);
                    }
                }
            }
        }
    }

    protected void drawStrokedCircle(Graphics2D graphics, double x, double y) {
        Stroke oldStroke = graphics.getStroke();
        graphics.setStroke(DASHED_STROKE);
        graphics.draw(new java.awt.geom.Ellipse2D.Double(x, y, 2 * DrawProperties.GRID_SIZE - 1, 2 * DrawProperties.GRID_SIZE - 1));
        graphics.setStroke(oldStroke);
    }

}
