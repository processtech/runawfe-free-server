package ru.runa.wfe.execution.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.execution.ExecutionStatus;
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
