package ru.runa.wfe.graph.view;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.dao.BotDao;
import ru.runa.wfe.bot.dao.BotTaskDao;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.execution.logic.BotSwimlaneInitializer;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.TaskDefinition;

public class NodeGraphElementBuilder {

    /**
     * Convert nodes to graph elements.
     *
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public static List<NodeGraphElement> createElements(BotDao botDao, BotTaskDao botTaskDao, ProcessDefinition definition) {
        List<NodeGraphElement> result = Lists.newArrayList();
        List<Node> nodes = definition.getNodes(false);
        nodes.sort(new NodeChidrenFirstComparator());

        Map<String, Bot> bots = new HashMap<>();
        Map<String, String> botTaskNames = new HashMap<>();
        Map<String, String> botTaskHandlerClassNames = new HashMap<>();
        Map<String, String> botTaskHandlerConfigurations = new HashMap<>();
        String botsXmlFileName = FileDataProvider.BOTS_XML_FILE;
        if (definition instanceof SubprocessDefinition) {
            botsXmlFileName = definition.getNodeId() + "." + botsXmlFileName;
        }
        byte[] xml = definition.getProcessFiles().get(botsXmlFileName);
        if (xml != null) {
            Document document = XmlUtils.parseWithoutValidation(xml);
            List<Element> elements = document.getRootElement().elements("task");
            for (Element element : elements) {
                String nodeId = element.attributeValue("id");
                botTaskHandlerClassNames.put(nodeId, element.attributeValue("class"));
                botTaskNames.put(nodeId, element.attributeValue("botTaskName"));
                Element configElement = element.element("config");
                botTaskHandlerConfigurations.put(nodeId,
                        new String(XmlUtils.save(configElement, OutputFormat.createPrettyPrint()), Charsets.UTF_8).trim());
            }
        }
        
        for (Node node : nodes) {
            NodeGraphElement element = createElement(node);
            result.add(element);
            if (element instanceof TaskNodeGraphElement) {
                TaskDefinition taskDefinition = ((InteractionNode) node).getFirstTaskNotNull();
                if (!taskDefinition.getSwimlane().isBotExecutor()) {
                    continue;
                }
                BotSwimlaneInitializer botSwimlaneInitializer = new BotSwimlaneInitializer();
                botSwimlaneInitializer.parse(taskDefinition.getSwimlane().getDelegation().getConfiguration());
                String botName = botSwimlaneInitializer.getBotName();
                if (!bots.containsKey(botName)) {
                    bots.put(botName, botDao.get(botName));
                }
                String botTaskHandlerClassName = botTaskHandlerClassNames.get(node.getNodeId());
                String botTaskHandlerConfiguration = botTaskHandlerConfigurations.get(node.getNodeId());
                if (botTaskHandlerClassName == null || botTaskHandlerConfiguration == null) {
                    Bot bot = bots.get(botName);
                    if (bot != null) {
                        String botTaskName = botTaskNames.containsKey(node.getNodeId()) ? botTaskNames.get(node.getNodeId()) : taskDefinition
                                .getName();
                        BotTask botTask = botTaskDao.get(bot, botTaskName);
                        if (botTask != null) {
                            botTaskHandlerClassName = botTask.getTaskHandlerClassName();
                            if (botTaskHandlerConfiguration == null) {
                                botTaskHandlerConfiguration = new String(botTask.getConfiguration(), Charsets.UTF_8);
                            }
                        }
                    }
                }
                ((TaskNodeGraphElement) element).initializeBotTaskInfo(botName, botTaskHandlerClassName, botTaskHandlerConfiguration);
            }
        }
        return result;
    }

    /**
     * Convert nodes to graph elements.
     *
     * @param definitionNodes
     *            Nodes to convert
     * @return List of graph elements for nodes.
     */
    public static NodeGraphElement createElement(Node node) {
        NodeGraphElement element;
        switch (node.getNodeType()) {
        case START_EVENT:
            element = new StartNodeGraphElement();
            break;
        case TASK_STATE:
        case MULTI_TASK_STATE:
            element = new TaskNodeGraphElement();
            break;
        case SUBPROCESS:
            element = new SubprocessNodeGraphElement();
            break;
        case MULTI_SUBPROCESS:
            element = new MultiSubprocessNodeGraphElement();
            break;
        case EXCLUSIVE_GATEWAY:
        case BUSINESS_RULE:
            element = new ExclusiveGatewayGraphElement();
            break;
        case ACTION_NODE:
            element = new ScriptNodeGraphElement();
            break;
        case RECEIVE_MESSAGE:
        case SEND_MESSAGE:
            element = new VariableContainerNodeGraphElement();
            break;
        case TIMER:
            element = new TimerNodeGraphElement();
            break;
        default:
            element = new NodeGraphElement();
        }
        int[] graphConstraints = new int[] { node.getGraphConstraints()[0], node.getGraphConstraints()[1],
                node.getGraphConstraints()[0] + node.getGraphConstraints()[2], node.getGraphConstraints()[1] + node.getGraphConstraints()[3] };
        element.initialize(node, graphConstraints);
        return element;
    }

    private static class NodeChidrenFirstComparator implements Comparator<Node> {

        @Override
        public int compare(Node o1, Node o2) {
            int p1 = o1.getParentElement() instanceof ProcessDefinition ? 1 : 0;
            int p2 = o2.getParentElement() instanceof ProcessDefinition ? 1 : 0;
            return Integer.compare(p1, p2);
        }

    }
}
