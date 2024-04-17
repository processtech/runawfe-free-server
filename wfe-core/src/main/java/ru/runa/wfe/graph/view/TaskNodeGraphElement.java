package ru.runa.wfe.graph.view;

import com.google.common.base.Objects;
import lombok.Getter;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

/**
 * Represents an task state graph element.
 */
@Getter
public class TaskNodeGraphElement extends NodeGraphElement {

    private static final long serialVersionUID = 1L;

    /**
     * Flag, equals true, if task state is minimized; false otherwise.
     */
    private boolean minimized;

    /**
     * Swimlane name of this task element.
     */
    private String swimlaneName;

    private String botTaskHandlerClassName = "";

    private String botTaskHandlerConfiguration = "";

    @Override
    public void initialize(Node node, int[] graphConstraints) {
        super.initialize(node, graphConstraints);
        TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
        minimized = node.isGraphMinimizedView();
        if (null != taskDefinition.getSwimlane()) {
            swimlaneName = taskDefinition.getSwimlane().getName();
        }
    }

    public void initializeBotTaskInfo(String botName, String botTaskHandlerClassName, String botTaskHandlerConfiguration) {
        if (!Objects.equal(swimlaneName, botName)) {
            swimlaneName += " / " + botName;
        }
        this.botTaskHandlerClassName = botTaskHandlerClassName;
        this.botTaskHandlerConfiguration = botTaskHandlerConfiguration;
    }

}
