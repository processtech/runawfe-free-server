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
package ru.runa.wfe.graph.image.figure.bpmn;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;

public class Rhomb extends AbstractBpmnFigure {
    private final String imageName;

    public Rhomb(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        drawImageIfNoEdgingOnly(graphics, imageName);
        if (renderHits.isPassed()) {
            Rectangle r = new Rectangle(coords[0] + 6, coords[1] + 6, coords[2] - 12, coords[3] - 12);
            Polygon polygon = new Polygon(new int[] { (int) r.getMinX(), (int) r.getCenterX(), (int) r.getMaxX(), (int) r.getCenterX() }, new int[] {
                    (int) r.getCenterY(), (int) r.getMinY(), (int) r.getCenterY(), (int) r.getMaxY() }, 4);
            graphics.drawPolygon(polygon);
        }
    }

    @Override
    protected AngleInfo getTransitionAngle(double x, double y) {
        Rectangle rect = getRectangle();
        double cx = rect.getCenterX();
        double cy = rect.getCenterY();
        AngleInfo angleInfo;
        if (x == cx) {
            angleInfo = (y - cy > 0) ? new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_IV) : new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_II);
        } else {
            angleInfo = new AngleInfo();
            if (y - cy > 0) {
                if (x - cx > 0) {
                    // IV
                    angleInfo.setQuarter(AngleInfo.QUARTER_IV);
                    angleInfo.setAngle((y - cy) / (x - cx));
                } else {
                    // III
                    angleInfo.setQuarter(AngleInfo.QUARTER_III);
                    angleInfo.setAngle((y - cy) / (x - cx));
                }
            } else {
                if (x - cx > 0) {
                    // I
                    angleInfo.setQuarter(AngleInfo.QUARTER_I);
                    angleInfo.setAngle((y - cy) / (x - cx));
                } else {
                    // II
                    angleInfo.setQuarter(AngleInfo.QUARTER_II);
                    angleInfo.setAngle((y - cy) / (x - cx));
                }
            }
        }
        return angleInfo;
    }

    @Override
    public Line createBorderLine(AngleInfo angle) {
        Line line = null;
        Rectangle r = getRectangle();

        switch (angle.getQuarter()) {
        case AngleInfo.QUARTER_I:
            line = LineUtils.createLine(new Point((int) r.getCenterX(), (int) r.getMinY()), new Point((int) r.getMaxX(), (int) r.getCenterY()));
            break;
        case AngleInfo.QUARTER_II:
            line = LineUtils.createLine(new Point((int) r.getCenterX(), (int) r.getMinY()), new Point((int) r.getMinX(), (int) r.getCenterY()));
            break;
        case AngleInfo.QUARTER_III:
            line = LineUtils.createLine(new Point((int) r.getCenterX(), (int) r.getMaxY()), new Point((int) r.getMinX(), (int) r.getCenterY()));
            break;
        case AngleInfo.QUARTER_IV:
            line = LineUtils.createLine(new Point((int) r.getMaxX(), (int) r.getCenterY()), new Point((int) r.getCenterX(), (int) r.getMaxY()));
            break;
        }
        return line;
    }
}
