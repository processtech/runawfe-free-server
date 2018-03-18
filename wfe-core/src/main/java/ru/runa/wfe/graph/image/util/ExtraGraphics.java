package ru.runa.wfe.graph.image.util;

import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import ru.runa.wfe.graph.DrawProperties;

/**
 * Class that provides extra graphics methods such as drawSmoothPolyline
 * 
 * @author Artem Mikheev
 */
public class ExtraGraphics {
    Graphics2D g;

    public ExtraGraphics(Graphics2D g) {
        this.g = g;
    }

    /**
     * Draws a sequence of lines connected by ellipses inscribed into polylines
     * angles defined by arrays of x and y coordinates. Each pair of (x, y)
     * coordinates defines a point. The figure is not closed if the first point
     * differs from the last point.
     * 
     * @author Artem Mikheev
     * @see java.awt.Graphics#drawPolyline
     * @param xPoints
     *            - an array of x points
     * @param yPoints
     *            - an array of y points
     * @param nPoints
     *            - the total number of points
     */
    public void drawSmoothPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        int i;

        /** start of line */
        Point2D.Double f = new Point2D.Double();
        /** end of line */
        Point2D.Double t = new Point2D.Double();

        /** Points of arc's start and end */
        Point2D.Double[] p;

        if (nPoints < 2) {
            return;
        }

        f.x = xPoints[0];
        f.y = yPoints[0];

        for (i = 1; i < nPoints - 1; i++) {
            t.x = xPoints[i];
            t.y = yPoints[i];

            // Draw arc and line to it.
            p = drawInscribedArc(f, t, new Point2D.Double(xPoints[i + 1], yPoints[i + 1]), DrawProperties.getSmoothDist());
            g.draw(new Line2D.Double(f.x, f.y, p[0].x, p[0].y));

            // Point of start of the next line is point of arc's end.
            f.x = p[1].x;
            f.y = p[1].y;
        }
        // Draw last line
        g.draw(new Line2D.Double(f.x, f.y, xPoints[i], yPoints[i]));
    }

    /**
     * Function to draw arc inscribed into angle S E T that tangent lines ES and
     * ET at distance "dist" from E.
     * 
     * @param s
     *            - start point
     * @param e
     *            - inflection point
     * @param t
     *            - point to
     * @param dist
     * @return Array of two Point2D.Double with coordinates of arc's start and
     *         end points
     */
    Point2D.Double[] drawInscribedArc(Point2D.Double s, Point2D.Double e, Point2D.Double t, double dist) {

        // If segment length less than dist
        dist = Math.min(GraphicsMath.getLength(e, t), dist);
        dist = Math.min(GraphicsMath.getLength(e, s), dist);

        /** Array for result values */
        Point2D.Double[] arcEntries = new Point2D.Double[2];

        // Default values is coordinates of inflection point
        arcEntries[0] = new Point2D.Double(e.x, e.y);
        arcEntries[1] = new Point2D.Double(e.x, e.y);
        ;

        // Calculation of bisector of angle S E T
        Point2D.Double v1 = GraphicsMath.getScaledVector(e, s, 50);
        Point2D.Double v2 = GraphicsMath.getScaledVector(e, t, 50);
        Point2D.Double bisector = GraphicsMath.getMiddle(v1, v2);

        /** Angle S E T */
        double angle = GraphicsMath.getVectorSignedAngle(GraphicsMath.getVector(e, s), GraphicsMath.getVector(e, t));

        double absOfAngleInDegrees = Math.abs(Math.toDegrees(angle));
        if ( absOfAngleInDegrees > 178 || absOfAngleInDegrees < 2) {
            // It's almost a straight line...
            return arcEntries;
        }

        /** radius of arc */
        double radius = Math.abs(dist * Math.tan(angle / 2));

        double sin = GraphicsMath.getLength(v1, bisector) / 50;
        double cos = GraphicsMath.getLength(e, bisector) / 50;

        Point2D.Double arcCenter = GraphicsMath.getScaledVector(e, bisector, radius / sin);
        arcEntries[0] = GraphicsMath.getScaledVector(e, s, radius / sin * cos);
        arcEntries[1] = GraphicsMath.getScaledVector(e, t, radius / sin * cos);

        v1 = new Point2D.Double(arcEntries[0].x - arcCenter.x, arcEntries[0].y - arcCenter.y);
        v2 = new Point2D.Double(arcEntries[1].x - arcCenter.x, arcEntries[1].y - arcCenter.y);
        double angA = GraphicsMath.getVectorSignedAngle(new Point2D.Double(1, 0), v1);
        double angAb = GraphicsMath.getVectorSignedAngle(v1, v2);
        g.draw(new Arc2D.Double(arcCenter.x - radius, arcCenter.y - radius, radius * 2, radius * 2, Math.toDegrees(angA), Math.toDegrees(angAb),
                Arc2D.OPEN));
        return arcEntries;
    }

    /**
     * Draws a little arc at point 'a'. For testing.
     * 
     * @param a
     */
    void drawPoint(Point2D.Double a) {
        g.drawArc((int) a.x - 2, (int) a.y - 2, 5, 5, 0, 360);
    }
}
