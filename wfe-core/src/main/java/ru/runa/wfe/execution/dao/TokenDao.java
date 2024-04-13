package ru.runa.wfe.execution.dao;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QProcess;
import ru.runa.wfe.execution.QToken;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;

/**
 * DAO for {@link Token}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class TokenDao extends GenericDao<Token> {

    public List<Token> findByNodeTypeInActiveProcesses(NodeType nodeType) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.nodeType.eq(nodeType).and(t.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.endDate.isNull()))
                .fetch();
    }

    public List<Token> findByProcessAndExecutionStatusIsNotEnded(ru.runa.wfe.execution.Process process) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<Token> findByProcessAndExecutionStatus(ru.runa.wfe.execution.Process process, ExecutionStatus status) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.eq(status))).fetch();
    }

    public List<String> findNodeIdsByProcessDefinitionIdAndExecutionStatusIsNotEnded(Long processDefinitionId) {
        val t = QToken.token;
        return queryFactory.selectDistinct(t.nodeId).from(t)
                .where(t.process.deployment.id.eq(processDefinitionId).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<Process> findProcessesForParallelGatewayUpdateValidatorCheck(Long processDefinitionId, String gatewayNodeId,
            Collection<String> nodeIds) {
        QToken t = QToken.token;
        QProcess p = QProcess.process;
        JPQLQuery<Long> innerTable1 = JPAExpressions.select(t.process.id).from(t).innerJoin(t.process, p)
                .where(p.deployment.id.eq(processDefinitionId).and((t.executionStatus.eq(ExecutionStatus.ENDED).not())).and(t.nodeId.in(nodeIds)))
                .groupBy(t.process.id).having(t.id.count().eq(0L).not());
        JPQLQuery<Long> innerTable2 = JPAExpressions.select(t.process.id).from(t).innerJoin(t.process, p)
                .where(p.deployment.id.eq(processDefinitionId).and(t.nodeId.eq(gatewayNodeId).and(t.executionStatus.eq(ExecutionStatus.ENDED))))
                .groupBy(t.process.id).having(t.id.count().eq(0L).not());
        return queryFactory.selectFrom(p).where(p.id.in(innerTable1).or(p.id.in(innerTable2))).fetch();
    }

    public List<String> findByProcessAndNodeIdsAndExecutionStatusIsNotEnded(Process process, Collection<String> nodeIds) {
        val t = QToken.token;
        return queryFactory.selectDistinct(t.nodeId).from(t)
                .where(t.process.eq(process).and(t.nodeId.in(nodeIds)).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }

    public List<Token> findByProcessAndNodeIdAndExecutionStatus(ru.runa.wfe.execution.Process process, String nodeId, ExecutionStatus status) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process).and(t.nodeId.eq(nodeId)).and(t.executionStatus.eq(status)))
                .fetch();
    }

    public List<Token> findByProcessAndNodeIdAndExecutionStatusIsEndedAndAbleToReactivateParent(ru.runa.wfe.execution.Process process, String nodeId) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process)
                        .and(t.nodeId.eq(nodeId))
                        .and(t.executionStatus.eq(ExecutionStatus.ENDED))
                        .and(t.ableToReactivateParent.isTrue()))
                .fetch();
    }

    public List<Token> findByProcessAndNodeTypeAndAbleToReactivateParent(ru.runa.wfe.execution.Process process, NodeType nodeType) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process)
                        .and(t.nodeType.eq(nodeType))
                        .and(t.ableToReactivateParent.isTrue()))
                .fetch();
    }

    public List<Token> findByMessageSelectorIsNullAndExecutionStatusIsNotEnded() {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.nodeType.eq(NodeType.RECEIVE_MESSAGE).and(t.messageSelector.isNull()).and(t.endDate.isNull()))
                .fetch();
    }

    public List<Token> findByMessageSelectorInActiveProcesses(String messageSelector) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.messageSelector.eq(messageSelector).and(t.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.endDate.isNull())).fetch();
    }

    public List<Token> findByMessageSelectorInActiveProcesses(Collection<String> messageSelectors) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.messageSelector.in(messageSelectors).and(t.executionStatus.ne(ExecutionStatus.SUSPENDED)).and(t.endDate.isNull())).fetch();
    }

    public List<Token> findByProcessAndEndDateGreaterThanOrEquals(ru.runa.wfe.execution.Process process, Date endDate) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process).and(t.endDate.eq(endDate).or(t.endDate.after(endDate))))
                .fetch();
    }

    public List<Token> findByProcessIdAndParentIsNull(Long processId) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.process.id.eq(processId).and(t.parent.isNull())).fetch();
    }

}
