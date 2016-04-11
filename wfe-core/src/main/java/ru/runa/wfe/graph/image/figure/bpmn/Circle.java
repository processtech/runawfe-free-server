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
import java.awt.Rectangle;

import ru.runa.wfe.graph.image.figure.AbstractFigure;

public class Circle extends AbstractFigure {
    private final String imageName;

    public Circle(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        drawImage(graphics, imageName);
        if (renderHits.isPassed()) {
            Rectangle r = getRectangle();
            if (cleanMode) {
                graphics.drawOval(r.x + 5, r.y + 5, 38, 38);
            }
            graphics.drawOval(r.x + 7, r.y + 7, 34, 34);
        }
    }
}
