package ru.runa.wfe.execution.dao;

import java.util.Collection;
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

    public List<Token> findByNodeTypeAndExecutionStatusIsActive(NodeType nodeType) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.nodeType.eq(nodeType).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))).fetch();
    }

    public List<Token> findByProcessAndExecutionStatusIsNotEnded(ru.runa.wfe.execution.Process process) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.ne(ExecutionStatus.ENDED))).fetch();
    }
    
    public List<Token> findByProcessAndMessageSelectorLikeAndExecutionStatusIsNotEnded(ru.runa.wfe.execution.Process process, String message) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process)
                        .and(t.messageSelector.like("%" + message + "%"))
                        .and(t.executionStatus.ne(ExecutionStatus.ENDED)))
                .fetch();
    }

    public List<Token> findByProcessAndExecutionStatus(ru.runa.wfe.execution.Process process, ExecutionStatus status) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.eq(status))).fetch();
    }

    public List<Token> findByProcessAndNodeIdAndExecutionStatusIsFailed(ru.runa.wfe.execution.Process process, String nodeId) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.process.eq(process).and(t.nodeId.eq(nodeId)).and(t.executionStatus.eq(ExecutionStatus.FAILED)))
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

    public List<Token> findByMessageSelectorIsNullAndExecutionStatusIsActive() {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t)
                .where(t.nodeType.eq(NodeType.RECEIVE_MESSAGE).and(t.messageSelector.isNull()).and(t.executionStatus.eq(ExecutionStatus.ACTIVE)))
                .fetch();
    }

    public List<Token> findByMessageSelectorAndExecutionStatusIsActive(String messageSelector) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.messageSelector.eq(messageSelector).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))).fetch();
    }

    public List<Token> findByMessageSelectorInAndExecutionStatusIsActive(Collection<String> messageSelectors) {
        QToken t = QToken.token;
        return queryFactory.selectFrom(t).where(t.messageSelector.in(messageSelectors).and(t.executionStatus.eq(ExecutionStatus.ACTIVE))).fetch();
    }
}
