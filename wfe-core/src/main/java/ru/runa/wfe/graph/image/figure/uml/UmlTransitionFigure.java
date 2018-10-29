package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Rectangle;

import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;

public class UmlTransitionFigure extends TransitionFigure {

    @Override
    public void init(Transition transition, AbstractFigure figureFrom, AbstractFigure figureTo, boolean smoothLines) {
        super.init(transition, figureFrom, figureTo, smoothLines);
        if (transition.isTimerTransition()) {
            setTimerInfo(CreateTimerAction.getNodeTimerActions(transition.getFrom(), false).get(0).getDueDate());
        }
    }

    @Override
    protected double[] getReferencePoint(Rectangle rectFrom, Rectangle rectTo) {
        double x;
        double y;
        if (figureTo.getNode().getNodeType() == NodeType.FORK || figureTo.getNode().getNodeType() == NodeType.JOIN) {
            ForkJoinFigure forkJoin = (ForkJoinFigure) figureTo;
            if (!forkJoin.isVertical() && rectTo.contains(rectFrom.getCenterX(), rectTo.getCenterY())) {
                // horizontal ForkJoin
                x = rectFrom.getCenterX();
                y = rectTo.getCenterY();
            } else if (forkJoin.isVertical() && rectTo.contains(rectTo.getCenterX(), rectFrom.getCenterY())) {
                // vertical ForkJoin
                x = rectTo.getCenterX();
                y = rectFrom.getCenterY();
            } else {
                x = rectTo.getCenterX();
                y = rectTo.getCenterY();
            }
        } else {
            x = rectTo.getCenterX();
            y = rectTo.getCenterY();
        }
        return new double[] { x, y };
    }
}
