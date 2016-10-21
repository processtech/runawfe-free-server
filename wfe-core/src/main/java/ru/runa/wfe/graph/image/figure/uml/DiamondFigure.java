package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.util.AngleInfo;
import ru.runa.wfe.graph.image.util.Line;
import ru.runa.wfe.graph.image.util.LineUtils;

public class DiamondFigure extends AbstractFigure {

    private Polygon createPolygon() {
        Rectangle r = getRectangle();
        Polygon polygon = new Polygon(new int[] { (int) r.getMinX(), (int) r.getCenterX(), (int) r.getMaxX(), (int) r.getCenterX() }, new int[] {
                (int) r.getCenterY(), (int) r.getMinY(), (int) r.getCenterY(), (int) r.getMaxY() }, 4);
        return polygon;
    }

    @Override
    public void fill(Graphics2D graphics) {
        graphics.fillPolygon(createPolygon());
    }

    protected boolean drawText() {
        return !useEgdingOnly;
    }

    @Override
    public void draw(Graphics2D graphics, boolean cleanMode) {
        graphics.drawPolygon(createPolygon());

        if (drawText()) {
            Rectangle r = getRectangle();
            int hOffset;
            Rectangle2D textBounds = graphics.getFontMetrics().getStringBounds(getName(), graphics);
            if (textBounds.getWidth() > r.getWidth() - 5) {
                int y = 0;
                AttributedString attributedString = new AttributedString(getName());
                attributedString.addAttribute(TextAttribute.FONT, graphics.getFont());
                AttributedCharacterIterator characterIterator = attributedString.getIterator();
                LineBreakMeasurer measurer = new LineBreakMeasurer(characterIterator, graphics.getFontRenderContext());
                while (measurer.getPosition() < characterIterator.getEndIndex()) {
                    TextLayout textLayout = measurer.nextLayout((float) (r.getWidth() / 1.4));
                    y += textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading();
                }
                hOffset = (int) ((r.getHeight() - y) / 2);
            } else {
                hOffset = (int) (r.getHeight() / 2 - DrawProperties.getFontSize());
            }
            drawTextInfo(graphics, hOffset);
        }
    }

    @Override
    protected AngleInfo getTransitionAngle(double x, double y) {
        Rectangle rect = getRectangle();
        double cx = rect.getCenterX();
        double cy = rect.getCenterY();
        AngleInfo angleInfo;
        if (x == cx) {
            angleInfo = (y - cy > 0) ? new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_IV) : new AngleInfo(Double.MAX_VALUE, AngleInfo.QUARTER_II);
        } else {
            angleInfo = new AngleInfo();
            if (y - cy > 0) {
                if (x - cx > 0) {
                    // IV
                    angleInfo.setQuarter(AngleInfo.QUARTER_IV);
                    angleInfo.setAngle((y - cy) / (x - cx));
                } else {
                    // III
                    angleInfo.setQuarter(AngleInfo.QUARTER_III);
                    angleInfo.setAngle((y - cy) / (x - cx));
                }
            } else {
                if (x - cx > 0) {
                    // I
                    angleInfo.setQuarter(AngleInfo.QUARTER_I);
                    angleInfo.setAngle((y - cy) / (x - cx));
                } else {
                    // II
                    angleInfo.setQuarter(AngleInfo.QUARTER_II);
                    angleInfo.setAngle((y - cy) / (x - cx));
                }
            }
        }
        return angleInfo;
    }

    @Override
    public Line createBorderLine(AngleInfo angle) {
        Line line = null;
        Rectangle r = getRectangle();

        switch (angle.getQuarter()) {
        case AngleInfo.QUARTER_I:
            line = LineUtils.createLine(new Point((int) r.getCenterX(), (int) r.getMinY()), new Point((int) r.getMaxX(), (int) r.getCenterY()));
            break;
        case AngleInfo.QUARTER_II:
            line = LineUtils.createLine(new Point((int) r.getCenterX(), (int) r.getMinY()), new Point((int) r.getMinX(), (int) r.getCenterY()));
            break;
        case AngleInfo.QUARTER_III:
            line = LineUtils.createLine(new Point((int) r.getCenterX(), (int) r.getMaxY()), new Point((int) r.getMinX(), (int) r.getCenterY()));
            break;
        case AngleInfo.QUARTER_IV:
            line = LineUtils.createLine(new Point((int) r.getMaxX(), (int) r.getCenterY()), new Point((int) r.getCenterX(), (int) r.getMaxY()));
            break;
        }
        return line;
    }

}
