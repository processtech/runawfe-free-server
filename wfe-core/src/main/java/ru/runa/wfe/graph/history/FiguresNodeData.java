package ru.runa.wfe.graph.history;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.history.graph.HistoryGraphNode;

/**
 * Node custom data with figures, requires to paint history graph.
 */
public class FiguresNodeData {
    /**
     * Key within node custom data to store figures information.
     */
    static final String DATA_KEY = "FIGURES";

    /**
     * Flag, equals true, if figures already initialized and false otherwise.
     */
    private boolean isFiguresInitialized = false;
    /**
     * Figure to draw this node.
     */
    private AbstractFigure figure;
    /**
     * Figures for leaving transition.
     */
    private final List<TransitionFigure> transitions = new ArrayList<TransitionFigure>();

    public boolean isFiguresInitializeRequired() {
        boolean initialized = isFiguresInitialized;
        isFiguresInitialized = true;
        return !initialized;
    }

    public AbstractFigure getFigure() {
        return figure;
    }

    public void setFigure(AbstractFigure figure) {
        this.figure = figure;
    }

    public List<TransitionFigure> getTransitions() {
        return transitions;
    }

    public void addTransition(TransitionFigure transition) {
        transitions.add(transition);
    }

    /**
     * Get figures data information or creates and stores it in node if not
     * available.
     * 
     * @param node
     *            Node to get data.
     * @return Returns figures data.
     */
    public static FiguresNodeData getOrCreate(HistoryGraphNode node) {
        FiguresNodeData data = (FiguresNodeData) node.getCustomData().get(DATA_KEY);
        if (data == null) {
            data = new FiguresNodeData();
            node.getCustomData().put(DATA_KEY, data);
        }
        return data;
    }

    /**
     * Get figures data information or throws exception if no data available.
     * 
     * @param node
     *            Node to get data.
     * @return Returns figures data.
     */
    public static FiguresNodeData getOrThrow(HistoryGraphNode node) {
        FiguresNodeData data = (FiguresNodeData) node.getCustomData().get(DATA_KEY);
        if (data == null) {
            throw new InternalApplicationException("figure data is not available.");
        }
        return data;
    }
}
