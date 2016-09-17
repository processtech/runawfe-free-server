package ru.runa.wfe.graph.image.figure.bpmn;

import java.awt.Graphics2D;
import java.awt.Point;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;

public abstract class AbstractBPMNFigure extends AbstractFigure {

    @Override
    public Point getTransitionPoint(Transition transition, double x, double y) {
        if (transition != null && transition.isTimerTransition() && nodeType != NodeType.WAIT_STATE) {
            return new Point(coords[0] + DrawProperties.GRID_SIZE, coords[1] + coords[3] - DrawProperties.GRID_SIZE);
        }
        return super.getTransitionPoint(transition, x, y);
    }

    protected void drawTimer(Graphics2D graphics) {
        if (hasTimer && !minimized && nodeType != NodeType.WAIT_STATE) {
            String fileName;
            if (timerInterrupting) {
                fileName = "image/bpmn/boundary_timer.png";
            } else {
                fileName = "image/bpmn/boundary_timer_notinterrupting.png";
            }
            drawImage(graphics, fileName, coords[0] + 1, coords[1] + coords[3] - 2 * DrawProperties.GRID_SIZE, true);
        }
    }
}
