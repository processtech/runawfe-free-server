package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;
import ru.runa.wfe.lang.Transition;

public class ReceiveMessageNodeFigure extends AbstractUmlFigure {

    @Override
    public Point getTransitionPoint(Transition transition, double x, double y) {
        if (transition != null && transition.isTimerTransition()) {
            return new Point(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE);
        }
        return super.getTransitionPoint(transition, x, y);
    }

    private Polygon createPolygon() {
        Rectangle r = getRectangle();
        int halfHeight = (int) Math.round(r.getHeight() / 2);
        int xLeft = (int) (r.getMinX() + halfHeight * Math.tan(Math.PI / 6));
        Polygon polygon = new Polygon(new int[] { xLeft, (int) r.getMinX(), (int) r.getMaxX(), (int) r.getMaxX(), (int) r.getMinX() }, new int[] {
                (int) r.getCenterY(), (int) r.getMinY(), (int) r.getMinY(), (int) r.getMaxY(), (int) r.getMaxY() }, 5);
        return polygon;
    }

    @Override
    public void fill(Graphics2D graphics) {
        graphics.fillPolygon(createPolygon());
        if (!node.isGraphMinimizedView() && hasTimer) {
            graphics.fillOval(coords[0], coords[1] + coords[3] - DrawProperties.GRID_SIZE * 2, DrawProperties.GRID_SIZE * 2,
                    DrawProperties.GRID_SIZE * 2);
        }
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        graphics.drawPolygon(createPolygon());
        if (!useEgdingOnly) {
            Rectangle r = getRectangle();
            drawTextInfo(graphics, (int) r.getHeight() / 2 - DrawProperties.getFontSize());
        }
        if (!node.isGraphMinimizedView() && hasTimer) {
            // Clean area for timer
            Color orig = graphics.getColor();
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.fillOval(coords[0], coords[1] + coords[3] - DrawProperties.GRID_SIZE * 2, DrawProperties.GRID_SIZE * 2,
                    DrawProperties.GRID_SIZE * 2);
            graphics.setColor(orig);

            // Draw timer
            graphics.drawOval(coords[0], coords[1] + coords[3] - DrawProperties.GRID_SIZE * 2, DrawProperties.GRID_SIZE * 2,
                    DrawProperties.GRID_SIZE * 2);
            graphics.drawLine(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE, coords[0]
                    + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE + 5);
            graphics.drawLine(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE, coords[0]
                    + DrawProperties.GRID_SIZE + 5, coords[1] + coords[3] - DrawProperties.GRID_SIZE - 5);
        }
    }

    @Override
    public Line createBorderLine(AngleInfo angle) {
        Rectangle r = getRectangle();
        double cutOffAngle = Math.atan((double) r.height / r.width);
        if (angle.getQuarter() == AngleInfo.QUARTER_III && Math.abs(angle.getAngle()) <= cutOffAngle) {
            int p = (int) ((r.width - r.height * Math.tan(Math.PI / 6)) / 2);
            int y = (int) r.getMinY();
            if (angle.getAngle() < 0) {
                y = (int) r.getMaxY();
            }
            return LineUtils.createLine(new Point((int) r.getCenterX() - p, (int) r.getCenterY()), new Point((int) r.getMinX(), y));
        }
        return super.createBorderLine(angle);
    }

    @Override
    public Rectangle getTextBoundsRectangle() {
        return getRectangle();
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(coords[0] + DrawProperties.GRID_SIZE, coords[1], coords[2] - DrawProperties.GRID_SIZE, coords[3]
                - DrawProperties.GRID_SIZE);
    }

}
