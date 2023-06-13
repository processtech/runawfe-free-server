package ru.runa.wfe.graph.image.figure;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.GraphImageHelper;
import ru.runa.wfe.graph.image.util.ActionUtils;
import ru.runa.wfe.graph.image.util.ExtraGraphics;
import ru.runa.wfe.graph.image.util.GraphicsMath;
import ru.runa.wfe.lang.Bendpoint;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public class TransitionFigure {

    private static BufferedImage BPMN_ACTION_ICON;
    static {
        try {
            BPMN_ACTION_ICON = ImageIO.read(ClassLoaderUtil.getAsStreamNotNull("image/bpmn/action.png", TransitionFigure.class));
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

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
        if (transition.getFrom().getParsedProcessDefinition().isGraphActionsEnabled()) {
            this.actionsCount = GraphImageHelper.getNodeActionsCount(transition);
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
            if (figureFrom.getNode().getNodeType() == NodeType.FORK || figureFrom.getNode().getNodeType() == NodeType.JOIN
                    || transition.isTimerTransition()) {
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
        graphics.setColor(DrawProperties.getBackgroundColor());
        graphics.setStroke(new BasicStroke(DrawProperties.FIGURE_CLEAN_WIDTH));
        graphics.drawPolygon(xSmPoints, ySmPoints, xSmPoints.length);

        if (figureFrom.useEdgingOnly) {
            // Cleaning old transitions
            graphics.setStroke(new BasicStroke(DrawProperties.TRANSITION_CLEAN_WIDTH));
            graphics.setColor(DrawProperties.getBackgroundColor());
            graphics.drawPolyline(xPoints, yPoints, xPoints.length);
        }

        graphics.setStroke(new BasicStroke(DrawProperties.TRANSITION_DRAW_WIDTH));
        graphics.setColor(color);

        if (actionsCount > 0 && !isJpdlCanvas() && transition.getParsedProcessDefinition().isGraphActionsEnabled()) {
            for (int i = 1; i <= actionsCount; i++) {
                Point point = getConnectionMidpoint(start, end, i * .1);
                graphics.drawImage(BPMN_ACTION_ICON, null, point.x - BPMN_ACTION_ICON.getWidth() / 2, point.y - BPMN_ACTION_ICON.getHeight() / 2);
            }
        }

        if (smoothLines) {
            extragraphics.drawSmoothPolyline(xPoints, yPoints, xPoints.length);
        } else {
            graphics.drawPolyline(xPoints, yPoints, xPoints.length);
        }

        if (actionsCount > 0 && isJpdlCanvas()) {
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
            drawExclusiveSymbol(graphics, start, xPoints, yPoints, color);
        }

        graphics.setColor(color);
        graphics.fillPolygon(xSmPoints, ySmPoints, xSmPoints.length);

        if (!figureFrom.useEdgingOnly && !transition.getName().startsWith("tr")) {
            String drawString = transition.isTimerTransition() ? timerInfo : transition.getName();
            Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(drawString, graphics);
            int padding = 1;
            int xStart;
            int yStart;
            if (figureFrom.getNode().getNodeType() == NodeType.FORK) {
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

    private void drawExclusiveSymbol(Graphics2D graphics, Point start, int[] xPoints, int[] yPoints, Color color) {
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

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    protected boolean isJpdlCanvas() {
        return transition.getParsedProcessDefinition().getLanguage().equals(Language.JPDL);
    }

    private Point getConnectionMidpoint(Point start, Point end, double part) {
        Point ret = null;
        Point[] pointsArray = new Point[bendpoints.size() + 2];
        {
            pointsArray[0] = start;
            int i = 1;
            for (Iterator<Point> iter = bendpoints.iterator(); iter.hasNext();) {
                pointsArray[i] = new Point();
                pointsArray[i++].setLocation(iter.next());
            }
            pointsArray[i] = end;
        }
        double completeDistance = getDistance(pointsArray);
        double absDistanceToRelPoint = completeDistance * part;
        double distanceSum = 0;
        for (int i = 0; i < pointsArray.length - 1; i++) {
            double oldDistanceSum = distanceSum;
            Point currentPoint = pointsArray[i];
            Point nextPoint = pointsArray[i + 1];
            double additionalDistanceToNext = getDistance(currentPoint, nextPoint);
            distanceSum += additionalDistanceToNext;
            if (distanceSum >= absDistanceToRelPoint) {
                double thisRelative = ((completeDistance * part) - oldDistanceSum) / additionalDistanceToNext;
                ret = getMidpoint(currentPoint.x, currentPoint.y, nextPoint.x, nextPoint.y, thisRelative);
                break;
            }
        }
        return ret;
    }

    private static double getDistance(Point start, Point end) {
        int xDist = end.x - start.x;
        int yDist = end.y - start.y;
        double ret = Math.sqrt((xDist * xDist) + (yDist * yDist));
        return ret;
    }

    private static double getDistance(Point[] points) {
        double ret = 0;
        for (int i = 0; i < points.length - 1; i++) {
            Point currentPoint = points[i];
            Point nextPoint = points[i + 1];
            ret += getDistance(currentPoint, nextPoint);
        }
        return ret;
    }

    private static Point getMidpoint(int startX, int startY, int endX, int endY, double d) {
        int midX = (int) Math.round((startX + d * (endX - startX)));
        int midY = (int) Math.round((startY + d * (endY - startY)));
        return new Point(midX, midY);
    }

}
