package ru.runa.wfe.graph.image;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.RenderHits;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;

public class GraphImage {
    private static final String FORMAT = "png";
    private BufferedImage image;
    private final ParsedProcessDefinition parsedProcessDefinition;
    private final Map<TransitionFigure, RenderHits> transitions;
    private final Map<AbstractFigure, RenderHits> nodes;
    private final boolean useEdgingOnly = DrawProperties.useEdgingOnly();

    @SneakyThrows
    public GraphImage(ParsedProcessDefinition parsedProcessDefinition, Map<TransitionFigure, RenderHits> transitions, Map<AbstractFigure, RenderHits> nodes) {
        if (useEdgingOnly) {
            image = ImageIO.read(new ByteArrayInputStream(parsedProcessDefinition.getGraphImageBytesNotNull()));
        }
        this.parsedProcessDefinition = parsedProcessDefinition;
        this.transitions = transitions;
        this.nodes = nodes;
    }

    public byte[] getImageBytes() throws IOException {
        int width = parsedProcessDefinition.getGraphConstraints()[2];
        int height = parsedProcessDefinition.getGraphConstraints()[3];
        if (!useEdgingOnly) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D graphics = image.createGraphics();

        graphics.setFont(new Font(DrawProperties.getFontFamily(), Font.PLAIN, DrawProperties.getFontSize()));
        graphics.setColor(DrawProperties.getBackgroundColor());

        if (!useEdgingOnly) {
            graphics.fillRect(0, 0, width, height);
        }

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        for (Map.Entry<TransitionFigure, RenderHits> entry : transitions.entrySet()) {
            entry.getKey().setRenderHits(entry.getValue());
            entry.getKey().draw(graphics, entry.getValue().getColor());
        }
        for (Map.Entry<AbstractFigure, RenderHits> entry : nodes.entrySet()) {
            int lineWidth = 1;
            if (entry.getValue().isActive()) {
                lineWidth *= 2;
            }
            if (parsedProcessDefinition.getLanguage() == Language.BPMN2) {
                lineWidth *= 2;
            }
            entry.getKey().setRenderHits(entry.getValue());
            Stroke stroke = new BasicStroke(lineWidth);
            if ((entry.getKey().getNode() instanceof SubprocessNode) && ((SubprocessNode) entry.getKey().getNode()).isTriggeredByEvent()) {
                stroke = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 6, 3 }, 0);
            }
            drawAbstractFigure(graphics, entry.getKey(), entry.getValue(), stroke);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, FORMAT, outputStream);
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
