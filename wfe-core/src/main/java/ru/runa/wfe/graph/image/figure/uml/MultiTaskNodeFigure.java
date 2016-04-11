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
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;

public class MultiTaskNodeFigure extends MultiSubprocessFigure {

    public MultiTaskNodeFigure() {
        drawSubprocessImage = false;
    }

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(coords[0] + DrawProperties.GRID_SIZE, coords[1], coords[2] - 2 * DrawProperties.GRID_SIZE, coords[3]
                - DrawProperties.GRID_SIZE);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        super.draw(graphics, cleanMode);
        if (!useEgdingOnly) {
            Rectangle b = getRectangle();
            graphics.drawString("*", b.x + b.width - 20, b.y + b.height - 10);
        }
    }
}
