package ru.runa.wfe.graph.history;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.BaseProcess;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.SubprocessNode;

public class ProcessInstanceData {
    /**
     * Process definition.
     */
    private final ProcessDefinition processDefinition;
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

    public ProcessInstanceData(BaseProcess instance, ProcessDefinition processDefinition) {
        addToken(instance.getRootToken());
        this.processDefinition = processDefinition;
        for (Node node : processDefinition.getNodes(true)) {
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
        Set<CurrentToken> childrens = token.getChildren();
        if (childrens == null) {
            return;
        }
        for (CurrentToken child : childrens) {
            addToken(child);
        }
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
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

    public SubprocessDefinition getEmbeddedSubprocess(String subProcessName) {
        return processDefinition.getEmbeddedSubprocessByNameNotNull(subProcessName);
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
