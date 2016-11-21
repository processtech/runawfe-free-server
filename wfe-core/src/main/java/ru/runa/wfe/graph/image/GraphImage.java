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
package ru.runa.wfe.graph.image;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import ru.runa.wfe.definition.Language;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.TransitionFigureBase;
import ru.runa.wfe.lang.ProcessDefinition;

import com.google.common.base.Throwables;

public class GraphImage {
    private static final String FORMAT = "png";
    private BufferedImage origImage = null;
    private final ProcessDefinition processDefinition;
    private final Map<TransitionFigureBase, RenderHits> transitions;
    private final Map<AbstractFigure, RenderHits> nodes;
    private final boolean useEdgingOnly = DrawProperties.useEdgingOnly();

    public GraphImage(ProcessDefinition processDefinition, Map<TransitionFigureBase, RenderHits> transitions, Map<AbstractFigure, RenderHits> nodes) {
        try {
            origImage = ImageIO.read(new ByteArrayInputStream(processDefinition.getGraphImageBytesNotNull()));
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        this.processDefinition = processDefinition;
        this.transitions = transitions;
        this.nodes = nodes;
    }

    public byte[] getImageBytes() throws IOException {
        int width = processDefinition.getGraphConstraints()[2];
        int height = processDefinition.getGraphConstraints()[3];
        BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resultImage.createGraphics();

        graphics.setFont(new Font(DrawProperties.getFontFamily(), Font.PLAIN, DrawProperties.getFontSize()));
        graphics.setColor(DrawProperties.getBackgroundColor());

        if (origImage != null && useEdgingOnly) {
            graphics.drawRenderedImage(origImage, AffineTransform.getRotateInstance(0));
        } else {
            graphics.fillRect(0, 0, width, height);
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        for (TransitionFigureBase transitionFigureBase : transitions.keySet()) {
            RenderHits hits = transitions.get(transitionFigureBase);
            transitionFigureBase.setRenderHits(hits);
            transitionFigureBase.draw(graphics, hits.getColor());
        }
        for (AbstractFigure nodeFigure : nodes.keySet()) {
            RenderHits hits = nodes.get(nodeFigure);
            int lineWidth = 1;
            if (hits.isActive()) {
                lineWidth *= 2;
            }
            if (processDefinition.getDeployment().getLanguage() == Language.BPMN2) {
                lineWidth *= 2;
            }
            nodeFigure.setRenderHits(hits);
            drawAbstractFigure(graphics, nodeFigure, hits, new BasicStroke(lineWidth));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resultImage, FORMAT, outputStream);
        return outputStream.toByteArray();
    }

    private void drawAbstractFigure(Graphics2D graphics, AbstractFigure figure, RenderHits hits, Stroke stroke) {
        if (useEdgingOnly) {
            graphics.setStroke(new BasicStroke(DrawProperties.FIGURE_CLEAN_WIDTH));
            graphics.setColor(DrawProperties.getBackgroundColor());
            figure.draw(graphics, true);
        } else {
            // background
            if (hits.isPassed()) {
                graphics.setColor(hits.isActive() ? DrawProperties.getActiveFigureBackgroundColor() : DrawProperties.getFigureBackgroundColor());
                figure.fill(graphics);
            }
        }

        graphics.setStroke(stroke);
        graphics.setColor(hits.getColor());
        figure.draw(graphics, false);
    }
}
