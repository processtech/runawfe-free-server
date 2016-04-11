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
package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.lang.Transition;

public class ForkJoinFigure extends AbstractFigure {

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        graphics.fillRect(coords[0], coords[1], coords[2], coords[3]);
    }

    public boolean isVertical() {
        return coords[2] < coords[3];
    }

    @Override
    public Point getTransitionPoint(Transition transition, double x, double y) {
        Rectangle r = getRectangle();
        if (x >= r.x && x <= r.x + r.width) {
            int referenceY;
            if (Math.abs(y - r.y) < Math.abs(y - (r.y + r.height))) {
                referenceY = r.y;
            } else {
                referenceY = r.y + r.height;
            }
            return new Point((int) x, referenceY);
        } else if (y >= r.y && y <= r.y + r.height) {
            int referenceX;
            if (Math.abs(x - r.x) < Math.abs(x - (r.x + r.width))) {
                referenceX = r.x;
            } else {
                referenceX = r.x + r.width;
            }
            return new Point(referenceX, (int) y);
        }

        return super.getTransitionPoint(transition, x, y);
    }
}
