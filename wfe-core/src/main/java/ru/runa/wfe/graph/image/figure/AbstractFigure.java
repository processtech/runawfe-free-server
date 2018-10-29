package ru.runa.wfe.graph.image.figure;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;
import ru.runa.wfe.lang.Transition;

public abstract class AbstractFigure {
    protected final Log log = LogFactory.getLog(getClass());

    protected Node node;
    protected int[] coords;
    protected String swimlaneName;
    protected boolean useEdgingOnly;

    protected Map<String, TransitionFigure> transitions = new HashMap<>();
    protected RenderHits renderHits;

    public void initFigure(Node node, boolean useEdgingOnly) {
        this.node = node;
        this.coords = node.getGraphConstraints();
        if (node instanceof InteractionNode && ((InteractionNode) node).getTasks().size() > 0) {
            TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
            if (taskDefinition.getSwimlane() != null) {
                this.swimlaneName = taskDefinition.getSwimlane().getName();
            }
        }
        this.useEdgingOnly = useEdgingOnly;
    }

    public Node getNode() {
        return node;
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

    public void addTransition(TransitionFigure transitionFigure) {
        transitions.put(transitionFigure.getTransition().getName(), transitionFigure);
    }

    public TransitionFigure getTransition(String name) {
        return transitions.get(name);
    }

    protected void drawTextInfo(Graphics2D graphics, int hOffset) {
        if (!useEdgingOnly) {
            Color color = graphics.getColor();
            graphics.setColor(DrawProperties.getTextColor());
            if (swimlaneName != null) {
                hOffset = drawText(graphics, "(" + swimlaneName + ")", hOffset);
                // additional space after swimlane label
                hOffset += 3;
            }
            drawText(graphics, node.getName(), hOffset);
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
            return y - cy > 0 ? new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_IV) : new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_II);
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
        return LineUtils.getIntersectionPoint(line1, line2);
    }
}
