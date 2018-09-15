package ru.runa.wfe.graph.history;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.lang.SubprocessNode;

public class ProcessInstanceData {
    /**
     * Process definition.
     */
    private final ParsedProcessDefinition parsedProcessDefinition;
    /**
     * Maps from node id to node model.
     */
    private final HashMap<String, Node> processDefinitionNodes = Maps.newHashMap();
    /**
     * Nodes id, which creates additional tokens (forks, parallel gateway's).
     */
    private final HashSet<String> createTokenNodes = Sets.newHashSet();
    /**
     * Embedded subprocesses of this process definition.
     */
    private final HashSet<String> subProcesses = Sets.newHashSet();
    /**
     * Process instance tokens.
     */
    private final HashMap<Long, Token> processTokens = Maps.newHashMap();

    public ProcessInstanceData(Process instance, ParsedProcessDefinition parsedProcessDefinition) {
        addToken(instance.getRootToken());
        this.parsedProcessDefinition = parsedProcessDefinition;
        for (Node node : parsedProcessDefinition.getNodes(true)) {
            this.processDefinitionNodes.put(node.getNodeId(), node);
            if (node.getNodeType() == NodeType.FORK || node.getNodeType() == NodeType.PARALLEL_GATEWAY) {
                getCreateTokenNodes().add(node.getNodeId());
            }
            if (node.getNodeType() == NodeType.SUBPROCESS && ((SubprocessNode) node).isEmbedded()) {
                getSubProcesses().add(getEmbeddedSubprocess(((SubprocessNode) node).getSubProcessName()).getNodeId());
            }
        }
    }

    private void addToken(Token token) {
        processTokens.put(token.getId(), token);
        val children = token.getChildren();
        if (children == null) {
            return;
        }
        for (val child : children) {
            addToken(child);
        }
    }

    public ParsedProcessDefinition getParsedProcessDefinition() {
        return parsedProcessDefinition;
    }

    public Node getNode(String nodeId) {
        return processDefinitionNodes.get(nodeId);
    }

    public Node getNodeNotNull(String nodeId) {
        Node node = processDefinitionNodes.get(nodeId);
        if (node == null) {
            throw new InternalApplicationException("node with id " + nodeId + " is not found");
        }
        return node;
    }

    public ParsedSubprocessDefinition getEmbeddedSubprocess(String subProcessName) {
        return parsedProcessDefinition.getEmbeddedSubprocessByNameNotNull(subProcessName);
    }

    public HashSet<String> getCreateTokenNodes() {
        return createTokenNodes;
    }

    public HashSet<String> getSubProcesses() {
        return subProcesses;
    }

    public String checkEmbeddedSubprocess(String nodeId) {
        int dotPos = nodeId.indexOf('.');
        if (dotPos == -1) {
            return null;
        }
        String subProcessName = nodeId.substring(0, dotPos);
        return subProcesses.contains(subProcessName) ? subProcessName : null;
    }

    public Token getToken(long tokenId) {
        return processTokens.get(tokenId);
    }
}
