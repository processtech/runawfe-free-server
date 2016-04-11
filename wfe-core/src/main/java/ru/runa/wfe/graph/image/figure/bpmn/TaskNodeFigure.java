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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.util.ActionUtils;
import ru.runa.wfe.lang.Transition;

public class TaskNodeFigure extends AbstractFigure {
    private static final Color BORDER_COLOR = Color.BLUE;

    @Override
    public Point getTransitionPoint(Transition transition, double x, double y) {
        if (transition != null && transition.isTimerTransition()) {
            return new Point(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE);
        }
        return super.getTransitionPoint(transition, x, y);
    }

    @Override
    public void fill(Graphics2D graphics) {
        Rectangle rect = getTextBoundsRectangle();
        graphics.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        if (!renderHits.isPassed()) {
            graphics.setColor(BORDER_COLOR);
        }
        Rectangle rect = getTextBoundsRectangle();
        graphics.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 20, 20);
        drawTextInfo(graphics, 1 + DrawProperties.GRID_SIZE / 2);
        if (hasTimer && !minimized) {
            drawImage(graphics, "image/bpmn/boundary_timer.png", coords[0] + 1, coords[1] + coords[3] - 2 * DrawProperties.GRID_SIZE, true);
        }
    }

    @Override
    protected void drawActions(Graphics2D graphics) {
        if (actionsCount > 0) {
            Color color = graphics.getColor();
            graphics.setColor(DrawProperties.getBackgroundColor());
            int shiftX = ActionUtils.ACTION_DELIM + 2 + actionsCount * (ActionUtils.ACTION_SIZE + ActionUtils.ACTION_DELIM + 3);
            int shiftY = ActionUtils.ACTION_SIZE + 6;
            graphics.fillRect(getTextBoundsRectangle().x + getTextBoundsRectangle().width - 4 - shiftX, getTextBoundsRectangle().y
                    + getTextBoundsRectangle().height - 4 - shiftY, shiftX, shiftY);
            for (int i = 0; i < actionsCount; i++) {
                Point loc = ActionUtils.getActionLocationOnNode(i, getCoordsFromRectangle(getTextBoundsRectangle()), false);
                graphics.setColor(DrawProperties.getBackgroundColor());
                loc.translate(-1, -1);
                graphics.setColor(color);
                graphics.drawOval(loc.x, loc.y, ActionUtils.ACTION_SIZE, ActionUtils.ACTION_SIZE);
            }
        }
    }

    private int[] getCoordsFromRectangle(Rectangle rectangle) {
        return new int[] { rectangle.x, rectangle.y, rectangle.width, rectangle.height };
    }

    @Override
    public Rectangle getTextBoundsRectangle() {
        Rectangle r = getRectangle();
        if (!minimized) {
            r.grow(-DrawProperties.GRID_SIZE / 2, -DrawProperties.GRID_SIZE / 2);
        }
        return r;
    }

}
