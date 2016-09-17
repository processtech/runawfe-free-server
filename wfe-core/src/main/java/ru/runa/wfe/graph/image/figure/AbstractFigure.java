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

package ru.runa.wfe.graph.image.figure;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.GraphImageHelper;
import ru.runa.wfe.graph.image.figure.uml.TaskNodeFigure;
import ru.runa.wfe.graph.image.util.ActionUtils;
import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;
import ru.runa.wfe.job.CreateTimerAction;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Synchronizable;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.Transition;

public abstract class AbstractFigure {
    private final static Log log = LogFactory.getLog(AbstractFigure.class);

    protected String nodeName;
    protected int[] coords;
    protected NodeType nodeType;

    protected String swimlane;
    protected int actionsCount;
    protected boolean async;
    protected boolean minimized;
    protected boolean hasTimer;
    protected boolean timerInterrupting = true;
    protected boolean useEgdingOnly;

    protected Map<String, TransitionFigureBase> transitions = new HashMap<String, TransitionFigureBase>();
    protected RenderHits renderHits;

    public void initFigure(Node node, boolean useEgdingOnly) {
        this.nodeName = node.getName();
        this.nodeType = node.getNodeType();
        this.coords = node.getGraphConstraints();
        List<CreateTimerAction> timerActions = node.getTimerActions(false);
        this.hasTimer = timerActions.size() > 0;
        if (this.hasTimer) {
            this.timerInterrupting = timerActions.get(0).isInterrupting();
        }
        if (node.getProcessDefinition().isGraphActionsEnabled()) {
            this.actionsCount = GraphImageHelper.getNodeActionsCount(node);
        }
        this.async = (node instanceof Synchronizable && ((Synchronizable) node).isAsync());
        this.minimized = node.isGraphMinimizedView();
        if (node instanceof InteractionNode && ((InteractionNode) node).getTasks().size() > 0) {
            TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
            if (taskDefinition.getSwimlane() != null) {
                this.swimlane = taskDefinition.getSwimlane().getName();
            }
        }
        this.useEgdingOnly = useEgdingOnly;
    }

    public String getName() {
        return nodeName;
    }

    public NodeType getType() {
        return nodeType;
    }

    public void setType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isHasTimer() {
        return hasTimer;
    }

    public RenderHits getRenderHits() {
        return renderHits;
    }

    public void setRenderHits(RenderHits renderHits) {
        this.renderHits = renderHits;
    }

    public int[] getCoords() {
        return coords;
    }

    public int getGraphX() {
        return coords[0];
    }

    public int getGraphY() {
        return coords[1];
    }

    public int getGraphWidth() {
        return coords[2];
    }

    public int getGraphHeight() {
        return coords[3];
    }

    public void addTransition(TransitionFigureBase transitionFigure) {
        transitions.put(transitionFigure.getTransition().getName(), transitionFigure);
    }

    public TransitionFigureBase getTransition(String name) {
        return transitions.get(name);
    }

    protected void drawActions(Graphics2D graphics) {
        if (actionsCount > 0) {
            Color color = graphics.getColor();
            if (useEgdingOnly) {
                int shiftX = (ActionUtils.ACTION_DELIM + 2) + actionsCount * (ActionUtils.ACTION_SIZE + (ActionUtils.ACTION_DELIM + 3));
                int shiftY = ActionUtils.ACTION_SIZE + 6;
                graphics.setColor(DrawProperties.getBackgroundColor());
                graphics.fillRect(getRectangle().x + getRectangle().width - 4 - shiftX, getRectangle().y + getRectangle().height - 4 - shiftY,
                        shiftX, shiftY);
            }
            for (int i = 0; i < actionsCount; i++) {
                Point loc = ActionUtils.getActionLocationOnNode(i, coords, getClass() == TaskNodeFigure.class);
                loc.translate(-1, -1);
                graphics.setColor(color);
                graphics.drawOval(loc.x, loc.y, ActionUtils.ACTION_SIZE, ActionUtils.ACTION_SIZE);
            }
        }
    }

    protected void drawTextInfo(Graphics2D graphics, int hOffset) {
        if (!useEgdingOnly) {
            Color color = graphics.getColor();
            graphics.setColor(DrawProperties.getTextColor());
            if (swimlane != null) {
                hOffset = drawText(graphics, "(" + swimlane + ")", hOffset);
                // additional space after swimlane label
                hOffset += 3;
            }
            drawText(graphics, getName(), hOffset);
            graphics.setColor(color);
        }
    }

    private int drawText(Graphics2D graphics, String text, int hOffset) {
        Rectangle r = getTextBoundsRectangle();
        Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(text, graphics);
        if (textBounds.getWidth() > r.getWidth() - 4) {
            int y = coords[1] + hOffset;
            AttributedString attributedString = new AttributedString(text);
            attributedString.addAttribute(TextAttribute.FONT, graphics.getFont());
            AttributedCharacterIterator characterIterator = attributedString.getIterator();
            LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, graphics.getFontRenderContext());
            while (measurer.getPosition() < characterIterator.getEndIndex()) {
                TextLayout textLayout = measurer.nextLayout((float) r.getWidth() - 4);
                y += textLayout.getAscent();
                float x = (float) (r.getCenterX() + 2 - textLayout.getBounds().getCenterX());
                textLayout.draw(graphics, x, y);
                y += textLayout.getDescent() + textLayout.getLeading();
            }
            return y - coords[1];
        } else {
            graphics.drawString(text, (float) (r.getCenterX() + 2 - textBounds.getCenterX()), (float) (coords[1] + textBounds.getHeight() + hOffset));
            return (int) (textBounds.getHeight() + hOffset + 3);
        }
    }

    protected void drawImage(Graphics2D graphics, String name) {
        drawImage(graphics, name, coords[0], coords[1]);
    }

    protected void drawImage(Graphics2D graphics, String name, double x, double y) {
        drawImage(graphics, name, x, y, !useEgdingOnly);
    }

    protected void drawImage(Graphics2D graphics, String name, double x, double y, boolean condition) {
        try {
            if (condition) {
                BufferedImage image = ImageIO.read(ClassLoaderUtil.getAsStreamNotNull(name, getClass()));
                graphics.drawRenderedImage(image, AffineTransform.getTranslateInstance(x, y));
            }
        } catch (IOException e) {
            log.error("Unable to paint image", e);
        }
    }

    public Point getBendpoint() {
        Rectangle allRect = getRectangle();
        return new Point((int) allRect.getCenterX(), (int) allRect.getCenterY());
    }

    public void fill(Graphics2D graphics) {
    }

    public abstract void draw(Graphics2D graphics, boolean cleanMode);

    public Rectangle getRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

    public Rectangle getTextBoundsRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

    protected AngleInfo getTransitionAngle(double x, double y) {
        Rectangle rect = getRectangle();
        double cx = rect.getCenterX();
        double cy = rect.getCenterY();
        if (x == cx) {
            return (y - cy > 0) ? new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_IV) : new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_II);
        } else {
            double critAngle = rect.getHeight() / rect.getWidth();
            AngleInfo angleInfo = new AngleInfo();
            angleInfo.setAngle((y - cy) / (x - cx));
            if (Math.abs(angleInfo.getAngle()) > critAngle) {
                if (y - cy > 0) {
                    // IV
                    angleInfo.setQuarter(AngleInfo.QUARTER_IV);
                } else {
                    // II
                    angleInfo.setQuarter(AngleInfo.QUARTER_II);
                }
            } else {
                if (x - cx > 0) {
                    // I
                    angleInfo.setQuarter(AngleInfo.QUARTER_I);
                } else {
                    // III
                    angleInfo.setQuarter(AngleInfo.QUARTER_III);
                }
            }
            return angleInfo;
        }
    }

    public Line createBorderLine(AngleInfo angle) {
        Line line = null;
        Rectangle r = getRectangle();

        switch (angle.getQuarter()) {
        case AngleInfo.QUARTER_I:
            line = LineUtils.createLine(new Point((int) r.getMaxX(), (int) r.getMinY()), new Point((int) r.getMaxX(), (int) r.getMaxY()));
            break;
        case AngleInfo.QUARTER_II:
            line = LineUtils.createLine(new Point((int) r.getMinX(), (int) r.getMinY()), new Point((int) r.getMaxX(), (int) r.getMinY()));
            break;
        case AngleInfo.QUARTER_III:
            line = LineUtils.createLine(new Point((int) r.getMinX(), (int) r.getMinY()), new Point((int) r.getMinX(), (int) r.getMaxY()));
            break;
        case AngleInfo.QUARTER_IV:
            line = LineUtils.createLine(new Point((int) r.getMinX(), (int) r.getMaxY()), new Point((int) r.getMaxX(), (int) r.getMaxY()));
            break;
        }
        return line;
    }

    public Point getTransitionPoint(Transition transition, double x, double y) {
        AngleInfo angle = getTransitionAngle(x, y);

        Rectangle r = getRectangle();
        double cx = r.getCenterX();
        double cy = r.getCenterY();

        Line line1 = createBorderLine(angle);
        Line line2 = LineUtils.createLine(new Point((int) cx, (int) cy), angle.getAngle());
        Point intersectionPoint = LineUtils.getIntersectionPoint(line1, line2);
        return intersectionPoint;
    }
}
