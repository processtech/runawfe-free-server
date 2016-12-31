/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.graph.image.figure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.GraphImageHelper;
import ru.runa.wfe.graph.image.util.ActionUtils;
import ru.runa.wfe.graph.image.util.ExtraGraphics;
import ru.runa.wfe.graph.image.util.GraphicsMath;
import ru.runa.wfe.lang.Bendpoint;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class TransitionFigureBase {
    protected String timerInfo;

    protected AbstractFigure figureFrom;
    protected AbstractFigure figureTo;

    protected Transition transition;
    protected int actionsCount;
    protected final List<Integer> failedActions = Lists.newArrayList();
    private boolean exclusive;
    protected RenderHits renderHits;
    protected boolean smoothLines;
    private final List<Point> bendpoints = Lists.newArrayList();

    public void init(Transition transition, AbstractFigure figureFrom, AbstractFigure figureTo, boolean smoothLines) {
        Preconditions.checkNotNull(transition, "transition");
        Preconditions.checkNotNull(figureFrom, "figureFrom");
        Preconditions.checkNotNull(figureTo, "figureTo");
        this.transition = transition;
        this.figureFrom = figureFrom;
        this.figureTo = figureTo;
        if (transition.getFrom().getProcessDefinition().isGraphActionsEnabled()) {
            this.actionsCount = GraphImageHelper.getNodeActionsCount(transition);
        }
        if (transition.isTimerTransition()) {
            timerInfo = transition.getFrom().getTimerActions(false).get(0).getDueDate();
        }
        this.smoothLines = smoothLines;
        for (Bendpoint bendpoint : transition.getBendpoints()) {
            Point point = new Point(bendpoint.getX(), bendpoint.getY());
            if (bendpoints.size() > 0 && Objects.equal(point, bendpoints.get(bendpoints.size() - 1))) {
                // remove duplicated bendpoints
                continue;
            }
            bendpoints.add(point);
        }
    }

    public Transition getTransition() {
        return transition;
    }

    public AbstractFigure getFigureFrom() {
        return figureFrom;
    }

    public AbstractFigure getFigureTo() {
        return figureTo;
    }

    public RenderHits getRenderHits() {
        return renderHits;
    }

    public void setRenderHits(RenderHits renderHits) {
        this.renderHits = renderHits;
    }

    public void setTimerInfo(String timerInfo) {
        this.timerInfo = timerInfo;
    }

    protected double[] getReferencePoint(Rectangle rectFrom, Rectangle rectTo) {
        return new double[] { rectTo.getCenterX(), rectTo.getCenterY() };
    }

    public void draw(Graphics2D graphics, Color color) {
        Rectangle rectFrom = figureFrom.getRectangle();
        Rectangle rectTo = figureTo.getRectangle();
        ExtraGraphics extragraphics = new ExtraGraphics(graphics);
        double secondX;
        double secondY;
        if (bendpoints.size() > 0) {
            Point bendPoint = bendpoints.get(0);
            secondX = bendPoint.x;
            secondY = bendPoint.y;
        } else {
            double[] secondCoors = getReferencePoint(rectFrom, rectTo);
            secondX = secondCoors[0];
            secondY = secondCoors[1];
        }
        List<Point> points = Lists.newArrayListWithExpectedSize(bendpoints.size() + 2);
        Point start = figureFrom.getTransitionPoint(transition, secondX, secondY);
        points.add(start);
        Point bendPoint = null;
        for (int i = 0; i < bendpoints.size(); i++) {
            bendPoint = bendpoints.get(i);
            if (!Objects.equal(bendPoint, points.get(points.size() - 1))) {
                points.add(bendPoint);
            }
        }
        if (bendPoint == null) {
            if (figureFrom.getType() == NodeType.FORK || figureFrom.getType() == NodeType.JOIN || transition.isTimerTransition()) {
                bendPoint = start;
            } else {
                bendPoint = new Point((int) rectFrom.getCenterX(), (int) rectFrom.getCenterY());// start;
            }
        }
        Point end = figureTo.getTransitionPoint(null, bendPoint.x, bendPoint.y);
        if (!Objects.equal(end, points.get(points.size() - 1)) || points.size() == 1) {
            points.add(end);
        }

        int[] xPoints = new int[points.size()];
        int[] yPoints = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            xPoints[i] = points.get(i).x;
            yPoints[i] = points.get(i).y;
        }
        if (figureFrom.useEdgingOnly) {
            // Cleaning old transitions
            graphics.setStroke(new BasicStroke(DrawProperties.TRANSITION_CLEAN_WIDTH));
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.drawPolyline(xPoints, yPoints, xPoints.length);
        }

        graphics.setStroke(new BasicStroke(DrawProperties.TRANSITION_DRAW_WIDTH));
        graphics.setColor(color);

        if (smoothLines) {
            extragraphics.drawSmoothPolyline(xPoints, yPoints, xPoints.length);
        } else {
            graphics.drawPolyline(xPoints, yPoints, xPoints.length);
        }

        if (actionsCount > 0) {
            Point p = new Point(xPoints[1], yPoints[1]);
            boolean fromTimer = transition.isTimerTransition();
            if (ActionUtils.areActionsFitInLine(actionsCount, start, p, fromTimer, exclusive)) {
                for (int i = 0; i < actionsCount; i++) {
                    Point loc = ActionUtils.getActionLocationOnTransition(i, start, p, fromTimer, exclusive);
                    graphics.setColor(DrawProperties.getBackgroundColor());
                    graphics.fillOval(loc.x, loc.y, ActionUtils.ACTION_SIZE + 3, ActionUtils.ACTION_SIZE + 3);
                    if (failedActions.contains(i)) {
                        graphics.setColor(Color.RED);
                        graphics.drawString("x", loc.x + 3, loc.y + 3);
                    }
                    graphics.setColor(color);
                    graphics.drawOval(loc.x, loc.y, ActionUtils.ACTION_SIZE, ActionUtils.ACTION_SIZE);
                }
            }
        }

        if (exclusive) {
            Point from = new Point(start);
            double angle = GraphicsMath.getAngle(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
            if (transition.isTimerTransition()) {
                from.x += DrawProperties.GRID_SIZE * Math.cos(angle);
                from.y += DrawProperties.GRID_SIZE * Math.sin(angle);
            }
            double delta = 2 * DrawProperties.TRANSITION_SM_ANGLE;
            double hypotenuse = 8;
            int xLeft = (int) Math.round(from.x + hypotenuse * Math.cos(angle - delta));
            int xRight = (int) Math.round(from.x + hypotenuse * Math.cos(angle + delta));
            int xEnd = (int) Math.round(from.x + 2 * hypotenuse * Math.cos(angle));
            int yLeft = (int) Math.round(from.y - hypotenuse * Math.sin(angle - delta));
            int yRight = (int) Math.round(from.y - hypotenuse * Math.sin(angle + delta));
            int yEnd = (int) Math.round(from.y - 2 * hypotenuse * Math.sin(angle));
            int[] xSmPoints = new int[] { from.x, xLeft, xEnd, xRight };
            int[] ySmPoints = new int[] { from.y, yLeft, yEnd, yRight };
            if (renderHits.isPassed()) {
                graphics.fillPolygon(xSmPoints, ySmPoints, xSmPoints.length);
            } else {
                graphics.setColor(DrawProperties.getBackgroundColor());
                graphics.fillPolygon(xSmPoints, ySmPoints, xSmPoints.length);
                graphics.setColor(color);
                graphics.drawPolygon(xSmPoints, ySmPoints, xSmPoints.length);
            }
        }

        double angle = GraphicsMath.getAngle(xPoints[xPoints.length - 1], yPoints[yPoints.length - 1], xPoints[xPoints.length - 2],
                yPoints[yPoints.length - 2]);
        double delta = DrawProperties.TRANSITION_SM_ANGLE;
        double hypotenuse = DrawProperties.TRANSITION_SM_L / Math.cos(delta);
        int xLeft = (int) Math.round(end.x + hypotenuse * Math.cos(angle - delta));
        int xRight = (int) Math.round(end.x + hypotenuse * Math.cos(angle + delta));
        int yLeft = (int) Math.round(end.y - hypotenuse * Math.sin(angle - delta));
        int yRight = (int) Math.round(end.y - hypotenuse * Math.sin(angle + delta));
        int[] xSmPoints = new int[] { end.x, xLeft, xRight };
        int[] ySmPoints = new int[] { end.y, yLeft, yRight };
        graphics.fillPolygon(xSmPoints, ySmPoints, xSmPoints.length);

        if (!figureFrom.useEdgingOnly && !transition.getName().startsWith("tr")) {
            String drawString = transition.isTimerTransition() ? timerInfo : transition.getName();
            Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(drawString, graphics);
            int padding = 1;
            int xStart = 0;
            int yStart = 0;
            if (figureFrom.getType() == NodeType.FORK) {
                xStart = (int) (xPoints[xPoints.length - 2] + xPoints[xPoints.length - 1] - textBounds.getWidth()) / 2;
                yStart = (int) (yPoints[yPoints.length - 2] + yPoints[yPoints.length - 1] - textBounds.getHeight()) / 2;
            } else {
                xStart = (int) (xPoints[0] + xPoints[1] - textBounds.getWidth()) / 2;
                yStart = (int) (yPoints[0] + yPoints[1] - textBounds.getHeight()) / 2;
            }

            Color orig = graphics.getColor();
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.fillRect(xStart - 2 * padding, yStart - padding, (int) (textBounds.getWidth() + 1 + 2 * padding),
                    (int) (textBounds.getHeight() + 1 + 2 * padding));
            graphics.setColor(orig);
            if (xStart < 1) {
                xStart = 1;
            }
            graphics.setColor(DrawProperties.getTextColor());
            graphics.drawString(drawString, xStart, (int) (yStart + textBounds.getHeight() - padding));
        }
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }
}
