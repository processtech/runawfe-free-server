package ru.runa.wfe.graph.image.figure.uml;

public class ConjunctionFigure extends DiamondFigure {

    @Override
    protected boolean drawText() {
        return super.drawText() && !node.isGraphMinimizedView();
    }

}
