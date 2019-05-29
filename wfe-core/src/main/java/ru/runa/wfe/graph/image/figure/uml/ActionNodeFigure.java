package ru.runa.wfe.graph.image.figure.uml;

import java.awt.Rectangle;

public class ActionNodeFigure extends TaskNodeFigure {

    @Override
    public Rectangle getRectangle() {
        return new Rectangle(coords[0], coords[1], coords[2], coords[3]);
    }

}
