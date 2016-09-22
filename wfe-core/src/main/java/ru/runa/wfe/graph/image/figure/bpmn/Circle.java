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

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.lang.BoundaryEvent;
import ru.runa.wfe.lang.BoundaryEventContainer;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.bpmn2.CatchEventNode;
import ru.runa.wfe.lang.bpmn2.TimerNode;

public class Circle extends AbstractBpmnFigure {
    private String imageName;

    public Circle(String imageName) {
        this.imageName = imageName;
    }

    @Override
    public void initFigure(Node node, boolean useEgdingOnly) {
        super.initFigure(node, useEgdingOnly);
        if (node.getParentElement() instanceof BoundaryEventContainer) {
            // fix coords and images for boundary events
            if (node instanceof TimerNode) {
                int[] parentCoords = node.getParentElement().getGraphConstraints();
                this.coords = new int[] { parentCoords[0], parentCoords[1] + parentCoords[3] - 2 * DrawProperties.GRID_SIZE,
                        2 * DrawProperties.GRID_SIZE, 2 * DrawProperties.GRID_SIZE };
                imageName = "image/bpmn/boundary_timer.png";
            }
            if (node instanceof CatchEventNode) {
                int[] parentCoords = node.getParentElement().getGraphConstraints();
                this.coords = new int[] { parentCoords[0] + parentCoords[2] - 2 * DrawProperties.GRID_SIZE,
                        parentCoords[1] + parentCoords[3] - 2 * DrawProperties.GRID_SIZE, 2 * DrawProperties.GRID_SIZE,
                        2 * DrawProperties.GRID_SIZE - 1 };
                String type = ((CatchEventNode) node).getEventType().name();
                imageName = "image/bpmn/boundary_catch_" + type + ".png";
            }
        }
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        if (node.getParentElement() instanceof BoundaryEventContainer) {
            if (cleanMode) {
                return;
            }
            drawImage(graphics, imageName, coords[0], coords[1]);
            if (renderHits.isPassed()) {
                if (((BoundaryEvent) node).getBoundaryEventInterrupting()) {
                    graphics.drawOval(coords[0], coords[1], coords[2], coords[3]);
                } else {
                    drawStrokedCircle(graphics, coords[0], coords[1]);
                }
            }
        } else {
            drawImageIfNoEdgingOnly(graphics, imageName);
            if (renderHits.isPassed()) {
                Rectangle r = getRectangle();
                if (cleanMode) {
                    graphics.drawOval(r.x + 5, r.y + 5, 38, 38);
                }
                graphics.drawOval(r.x + 7, r.y + 7, 34, 34);
            }
        }
    }
}
