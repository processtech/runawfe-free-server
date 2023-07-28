package ru.runa.wfe.graph.image.figure.uml;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.graph.image.figure.AbstractFigure;
import ru.runa.wfe.graph.image.figure.AbstractFigureFactory;
import ru.runa.wfe.graph.image.figure.TransitionFigure;
import ru.runa.wfe.lang.Node;

public class UmlFigureFactory extends AbstractFigureFactory {

    @Override
    public AbstractFigure createFigure(Node node, boolean useEdgingOnly) {
        AbstractFigure figure = null;
        switch (node.getNodeType()) {
        case TASK_STATE:
            figure = new TaskNodeFigure();
            break;
        case MULTI_TASK_STATE:
            figure = new MultiTaskNodeFigure();
            break;
        case EXCLUSIVE_GATEWAY:
        case BUSINESS_RULE:
        case DECISION:
            figure = new DecisionFigure();
            break;
        case MERGE:
            figure = new ConjunctionFigure();
            break;
        case PARALLEL_GATEWAY:
        case FORK:
        case JOIN:
            figure = new ForkJoinFigure();
            break;
        case START_EVENT:
            figure = new StartStateFigure();
            break;
        case END_PROCESS:
            figure = new EndStateFigure();
            break;
        case END_TOKEN:
            figure = new EndTokenStateFigure();
            break;
        case SUBPROCESS:
            figure = new SubprocessFigure();
            break;
        case ACTION_NODE:
            figure = new ActionNodeFigure();
            break;
        case WAIT_STATE:
            figure = new WaitStateFigure();
            break;
        case MULTI_SUBPROCESS:
            figure = new MultiSubprocessFigure();
            break;
        case SEND_MESSAGE:
            figure = new SendMessageNodeFigure();
            break;
        case RECEIVE_MESSAGE:
            figure = new ReceiveMessageNodeFigure();
            break;
        default:
            throw new InternalApplicationException("Unexpected figure type found: " + node.getNodeType());
        }
        figure.initFigure(node, useEdgingOnly);
        return figure;
    }

    @Override
    public TransitionFigure createTransitionFigure() {
        return new UmlTransitionFigure();
    }
}
