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
package ru.runa.wfe.graph.image.util;

import java.awt.Point;

public class LineUtils {

    public static Line createLine(Point start, Point end) {
        double k;
        double b = 0;
        boolean kExists = true;
        if (start.x == end.x) {
            k = start.x;
            kExists = false;
        } else {
            k = (double) (start.y - end.y) / (start.x - end.x);
            b = end.y - k * end.x;
        }
        return new Line(k, b, kExists);
    }

    public static Line createLine(Point start, double k) {
        if (k == Double.MAX_VALUE) {
            return new Line(start.x, 0, false);
        } else {
            double b = start.y - k * start.x;
            return new Line(k, b, true);
        }
    }

    public static Point getIntersectionPoint(Line line1, Line line2) {
        double x = 0;
        double y = 0;
        if (line1.isKExists() && line2.isKExists()) {
            x = (line2.getB() - line1.getB()) / (line1.getK() - line2.getK());
            y = line1.getK() * x + line1.getB();
        } else {
            if (!line1.isKExists()) {
                x = line1.getK();
                y = line2.getK() * x + line2.getB();
            } else {
                x = line2.getK();
                y = line1.getK() * x + line1.getB();
            }
        }
        return new Point((int) x, (int) y);
    }
}
