package ru.runa.wfe.execution.dao;

import com.google.common.base.Preconditions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.QCurrentProcess;
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

    public CurrentTokenDao() {
        super(CurrentToken.class);
    }

    public List<CurrentToken> findByNodeTypeInActiveProcesses(NodeType nodeType) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.nodeType.eq(nodeType).and(t.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.endDate.isNull())).fetch();
    }

    public List<CurrentToken> findByProcessAndExecutionStatus(CurrentProcess process, ExecutionStatus status) {
        // At the moment of refactoring, this method was never called with ENDED status, so I didn't bother to implement that case.
        Preconditions.checkArgument(status != ExecutionStatus.ENDED);

        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.eq(status))).fetch();
    }

    public List<String> findNodeIdsByProcessDefinitionIdAndExecutionStatusIsNotEnded(Long processDefinitionId) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectDistinct(t.nodeId).from(t)
                .where(t.process.definition.id.eq(processDefinitionId).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<CurrentProcess> findProcessesForParallelGatewayUpdateValidatorCheck(Long processDefinitionId, String gatewayNodeId,
            Collection<String> nodeIds) {
        QCurrentToken t = QCurrentToken.currentToken;
        QCurrentProcess p = QCurrentProcess.currentProcess;
        JPQLQuery<Long> innerTable1 = JPAExpressions.select(t.process.id).from(t).innerJoin(p).on(t.process.id.eq(p.id))
                .where(p.definition.id.eq(processDefinitionId).and((t.executionStatus.eq(ExecutionStatus.ENDED).not())).and(t.nodeId.in(nodeIds)))
                .groupBy(t.process.id).having(t.id.count().eq(0L).not());
        JPQLQuery<Long> innerTable2 = JPAExpressions.select(t.process.id).from(t).innerJoin(p).on(t.process.id.eq(p.id))
                .where(p.definition.id.eq(processDefinitionId).and(t.nodeId.eq(gatewayNodeId).and(t.executionStatus.eq(ExecutionStatus.ENDED))))
                .groupBy(t.process.id).having(t.id.count().eq(0L).not());
        return queryFactory.selectFrom(p).where(p.id.in(innerTable1).or(p.id.in(innerTable2))).fetch();
    }

    public List<String> findByProcessAndNodeIdsAndExecutionStatusIsNotEnded(CurrentProcess process, Collection<String> nodeIds) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectDistinct(t.nodeId).from(t)
                .where(t.process.eq(process).and(t.nodeId.in(nodeIds)).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<CurrentToken> findByProcessAndExecutionStatusIsNotEnded(CurrentProcess process) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<CurrentToken> findByProcessAndNodeIdAndExecutionStatus(CurrentProcess process, String nodeId, ExecutionStatus status) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.nodeId.eq(nodeId)).and(t.executionStatus.eq(status)))
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

    public List<CurrentToken> findByMessageSelectorIsNullAndExecutionStatusIsNotEnded() {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.nodeType.eq(NodeType.RECEIVE_MESSAGE).and(t.messageSelector.isNull()).and(t.endDate.isNull()))
                .fetch();
    }

    public List<CurrentToken> findByMessageSelectorInActiveProcesses(String messageSelector) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.messageSelector.eq(messageSelector).and(t.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.endDate.isNull())).fetch();
    }

    public List<CurrentToken> findByMessageSelectorInActiveProcesses(Collection<String> messageSelectors) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.messageSelector.in(messageSelectors).and(t.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.endDate.isNull())).fetch();
    }
    
    public List<CurrentToken> findByProcessAndEndDateGreaterThanOrEquals(CurrentProcess process, Date endDate) {
        QCurrentToken t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process).and(t.endDate.eq(endDate).or(t.endDate.after(endDate))))
                .fetch();
    }

    public List<CurrentToken> findByProcessAndNodeTypeAndAbleToReactivateParent(CurrentProcess process, NodeType nodeType) {
        QCurrentToken t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process)
                        .and(t.nodeType.eq(nodeType))
                        .and(t.ableToReactivateParent.isTrue()))
                .fetch();
    }

    public List<CurrentToken> findByProcessIdAndParentIsNull(Long processId) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.id.eq(processId).and(t.parent.isNull())).fetch();
    }

    public boolean existByProcessAndExecutionStatusIsNotEndedAndNodeIdPrefix(CurrentProcess process, String nodeIdPrefix) {
        val t = QCurrentToken.currentToken;
        String nodeIdPrefixPattern = nodeIdPrefix + "%";
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process).and(t.executionStatus.ne(ExecutionStatus.ENDED).and(t.nodeId.like(nodeIdPrefixPattern)))).limit(1)
                .fetchFirst() != null;
    }

}
