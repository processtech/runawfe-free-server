package ru.runa.wfe.execution.process.check;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.FrozenTokenDao;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.execution.dto.WfFrozenToken;
import ru.runa.wfe.lang.EmbeddedSubprocessEndNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.lang.Transition;
import ru.runa.wfe.lang.bpmn2.ParallelGateway;

@CommonsLog
@Component
public class FrozenProcessInParallelGatewaySeeker implements FrozenProcessSeeker {

    private final String nameId = FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.getName();
    private final String nameLabel = FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_PARALLEL_GATEWAYS.getNameLabel();

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private FrozenTokenDao frozenTokenDao;
    @Autowired
    private TokenDao tokenDao;

    @Override
    public String getNameId() {
        return nameId;
    }

    @Override
    public String getNameLabel() {
        return nameLabel;
    }

    @Override
    public List<WfFrozenToken> seek(Integer timeThreshold, Map<FrozenProcessFilter, String> filters) {
        List<WfFrozenToken> frozenTokens = new ArrayList<>();
        List<Token> gatewayTokens;
        int pageNumber = 0;
        do {
            gatewayTokens = frozenTokenDao.findByNodeIsParallelGatewayAndReactivateParentIsTrueAndFilter(pageNumber,
                    FrozenTokenDao.PAGE_SIZE, filters);
            for (Token token : gatewayTokens) {
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess());
                try {
                    if (findStateInfo((ParallelGateway) token.getNodeNotNull(processDefinition), token.getProcess().getId()).state == State.BLOCKING) {
                        frozenTokens.add(new WfFrozenToken(token, getNameLabel()));
                    }
                } catch (Exception exception) {
                    log.info(String.format("Error while seeking frozen token in %s", token));
                    frozenTokens.add(new WfFrozenToken(token, getNameLabel(), exception.getMessage()));
                }
            }
            pageNumber++;
        } while (gatewayTokens.size() == FrozenTokenDao.PAGE_SIZE);
        return frozenTokens;
    }

    private StateInfo findStateInfo(ParallelGateway gateway, Long processId) {
        StateInfo stateInfo = new StateInfo();
        List<Token> tokens = tokenDao.findByProcessIdAndParentIsNull(processId);
        for (Token token : tokens) {
            fillTokensInfo(gateway, token, stateInfo);
        }
        for (Transition transition : gateway.getArrivingTransitions()) {
            boolean transitionIsPassedByToken = false;
            for (Token token : stateInfo.arrivedTokens) {
                if (Objects.equal(transition.getNodeId(), token.getTransitionId())
                        || Objects.equal(transition.getNodeIdBackCompatibilityPre4_3_0(), token.getTransitionId())) {
                    transitionIsPassedByToken = true;
                    stateInfo.tokensToPop.add(token);
                    break;
                }
            }
            if (!transitionIsPassedByToken) {
                stateInfo.notPassedTransitions.add(transition);
            }
        }
        if (stateInfo.notPassedTransitions.isEmpty()) {
            stateInfo.state = State.LEAVING;
        } else {
            for (Transition transition : stateInfo.notPassedTransitions) {
                if (!hasTokenAlreadyArrivedThroughExactTransitionOrTransitionCanBePassed(gateway, stateInfo, transition)) {
                    stateInfo.state = State.BLOCKING;
                    break;
                }
            }
        }
        return stateInfo;
    }

    private void fillTokensInfo(ParallelGateway gateway, Token token, StateInfo stateInfo) {
        if (token.isAbleToReactivateParent()) {
            if (token.getExecutionStatus() != ExecutionStatus.ACTIVE && Objects.equal(token.getNodeId(), gateway.getNodeId())) {
                stateInfo.arrivedTokens.add(token);
            } else if (token.getExecutionStatus() != ExecutionStatus.ENDED) {
                stateInfo.activeTokens.add(token);
            }
        }
        for (Token childToken : token.getChildren()) {
            fillTokensInfo(gateway, childToken, stateInfo);
        }
    }

    private boolean hasTokenAlreadyArrivedThroughExactTransitionOrTransitionCanBePassed(ParallelGateway gateway, StateInfo stateInfo,
            Transition transition) {
        boolean tokenAlreadyArrived = false;
        for (Token token : stateInfo.activeTokens) {
            if (token.getNodeId().equals(gateway.getNodeId()) && token.getTransitionId().equals(transition.getNodeId())) {
                log.debug(String.format("Skipping %s passage validation due to active token %s arriving through it", transition, token));
                tokenAlreadyArrived = true;
                break;
            }
        }
        return tokenAlreadyArrived ? tokenAlreadyArrived : transitionCanBePassed(transition, stateInfo.getActiveTokenNodeIds(), new HashSet<Node>());
    }

    private boolean transitionCanBePassed(Transition transition, Set<String> activeTokenNodeIds, Set<Node> testedNodes) {
        Node node = transition.getFrom();
        if (testedNodes.contains(node)) {
            return false;
        }
        testedNodes.add(node);
        if (activeTokenNodeIds.contains(node.getNodeId())) {
            return true;
        }
        if (node instanceof EmbeddedSubprocessEndNode) {
            // code below needed for cases when subprocess have more than one end node, so arriving transition on node after subprocess return only
            // one of theese end nodes which results in only one branch of subprocess being tested on can be passed
            for (Node subprocessEndNode : ((SubprocessDefinition) node.getProcessDefinition()).getEndNodes()) {
                for (Transition nodeTransition : subprocessEndNode.getArrivingTransitions()) {
                    if (transitionCanBePassed(nodeTransition, activeTokenNodeIds, testedNodes)) {
                        return true;
                    }
                }
            }
        }
        for (Transition nodeTransition : node.getArrivingTransitions()) {
            if (transitionCanBePassed(nodeTransition, activeTokenNodeIds, testedNodes)) {
                return true;
            }
        }
        return false;
    }

    public enum State {
        LEAVING,
        WAITING,
        BLOCKING
    }

    private static class StateInfo {
        private State state = State.WAITING;
        private Set<Token> arrivedTokens = Sets.newHashSet();
        private Set<Token> activeTokens = Sets.newHashSet();
        private List<Token> tokensToPop = Lists.newArrayList();
        private List<Transition> notPassedTransitions = Lists.newArrayList();

        public Set<String> getActiveTokenNodeIds() {
            Set<String> result = Sets.newHashSet();
            for (Token token : activeTokens) {
                result.add(token.getNodeId());
            }
            return result;
        }
    }
}