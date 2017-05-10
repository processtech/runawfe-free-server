package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import ru.runa.wfe.graph.DrawProperties;
import ru.runa.wfe.graph.image.GraphImageHelper;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.util.ActionUtils;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;

public abstract class AbstractUmlFigure extends AbstractFigure {
    protected boolean hasTimer;
    protected int actionsCount;

    @Override
    public void initFigure(Node node, boolean useEgdingOnly) {
        super.initFigure(node, useEgdingOnly);
        List<CreateTimerAction> timerActions = CreateTimerAction.getNodeTimerActions(node, false);
        this.hasTimer = timerActions.size() > 0;
        if (node.getProcessDefinition().isGraphActionsEnabled()) {
            this.actionsCount = GraphImageHelper.getNodeActionsCount(node);
        }
    }

    protected void drawActions(Graphics2D graphics) {
        if (actionsCount > 0) {
            Color color = graphics.getColor();
            if (useEdgingOnly) {
                int shiftX = (ActionUtils.ACTION_DELIM + 2) + actionsCount * (ActionUtils.ACTION_SIZE + (ActionUtils.ACTION_DELIM + 3));
                int shiftY = ActionUtils.ACTION_SIZE + 6;
                graphics.setColor(DrawProperties.getBackgroundColor());
                graphics.fillRect(getRectangle().x + getRectangle().width - 4 - shiftX, getRectangle().y + getRectangle().height - 4 - shiftY, shiftX, shiftY);
            }
            for (int i = 0; i < actionsCount; i++) {
                Point loc = ActionUtils.getActionLocationOnNode(i, coords, this instanceof MultiTaskNodeFigure, this instanceof TaskNodeFigure);
                loc.translate(-1, -1);
                graphics.setColor(color);
                graphics.drawOval(loc.x, loc.y, ActionUtils.ACTION_SIZE, ActionUtils.ACTION_SIZE);
            }
        }
    }

}
