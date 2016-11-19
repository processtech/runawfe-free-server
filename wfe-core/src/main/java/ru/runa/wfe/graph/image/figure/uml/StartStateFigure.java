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

public class StartStateFigure extends AbstractFigure {

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        Rectangle r = getRectangle();
        graphics.fillOval(r.x - 1, r.y - 1, r.width, r.height);
        drawTextInfo(graphics, 1);
    }

    @Override
    public Rectangle getRectangle() {
        Rectangle r = new Rectangle(coords[0], coords[1], coords[2], coords[3]);
        return new Rectangle((int) (r.getCenterX() - 7), (int) r.getMaxY() - 20, 16, 16);
    }

    @Override
    public Point getBendpoint() {
        return null;
    }
}
