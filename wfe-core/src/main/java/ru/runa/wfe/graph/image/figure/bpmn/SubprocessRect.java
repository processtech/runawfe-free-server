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
        if (!useEdgingOnly) {
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
