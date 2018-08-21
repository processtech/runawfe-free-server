package ru.runa.wfe.execution.dao;

import java.util.Collection;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.QCurrentToken;
import ru.runa.wfe.lang.NodeType;

/**
 * DAO for {@link CurrentToken}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class CurrentTokenDao extends GenericDao<CurrentToken> {

    public List<CurrentToken> findByNodeTypeAndExecutionStatusIsActive(NodeType nodeType) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.nodeType.eq(nodeType).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))).fetch();
    }

    public List<CurrentToken> findByProcessAndExecutionStatusIsNotEnded(CurrentProcess process) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<CurrentToken> findByProcessAndExecutionStatus(CurrentProcess process, ExecutionStatus status) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.eq(status))).fetch();
    }

    public List<CurrentToken> findByProcessAndNodeIdAndExecutionStatusIsFailed(CurrentProcess process, String nodeId) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process).and(t.nodeId.eq(nodeId)).and(t.executionStatus.eq(ExecutionStatus.FAILED)))
                .fetch();
    }

    public List<CurrentToken> findByProcessAndNodeIdAndExecutionStatusIsEndedAndAbleToReactivateParent(CurrentProcess process, String nodeId) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process)
                        .and(t.nodeId.eq(nodeId))
                        .and(t.executionStatus.eq(ExecutionStatus.ENDED))
                        .and(t.ableToReactivateParent.isTrue()))
                .fetch();

    }

    public List<CurrentToken> findByMessageSelectorIsNullAndExecutionStatusIsActive() {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.nodeType.eq(NodeType.RECEIVE_MESSAGE).and(t.messageSelector.isNull()).and(t.executionStatus.eq(ExecutionStatus.ACTIVE)))
                .fetch();
    }

    public List<CurrentToken> findByMessageSelectorAndExecutionStatusIsActive(String messageSelector) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.messageSelector.eq(messageSelector).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))).fetch();
    }

    public List<CurrentToken> findByMessageSelectorInAndExecutionStatusIsActive(Collection<String> messageSelectors) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.messageSelector.in(messageSelectors).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))).fetch();
    }
}
