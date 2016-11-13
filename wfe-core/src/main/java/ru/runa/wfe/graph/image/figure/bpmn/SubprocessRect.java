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

import ru.runa.wfe.graph.DrawProperties;

public class SubprocessRect extends RoundedRect {

    public SubprocessRect() {
        super(null);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        super.draw(graphics, cleanMode);
        if (!useEgdingOnly) {
            int d = 2;
            int xCenter = coords[0] + coords[2] / 2;
            int y = coords[1] + coords[3] - DrawProperties.GRID_SIZE;
            graphics.drawRect(xCenter - DrawProperties.GRID_SIZE / 2, y, DrawProperties.GRID_SIZE, DrawProperties.GRID_SIZE);
            graphics.drawLine(xCenter - DrawProperties.GRID_SIZE / 2 + d, y + DrawProperties.GRID_SIZE / 2, xCenter + DrawProperties.GRID_SIZE / 2
                    - d, y + DrawProperties.GRID_SIZE / 2);
            graphics.drawLine(xCenter, y + d, xCenter, y + DrawProperties.GRID_SIZE - d);
        }
    }
}
