package ru.runa.wfe.execution.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;

/**
 * DAO for {@link Token}.
 *
 * @author dofs
 * @since 4.0
 */
public class TokenDAO extends GenericDAO<Token> {

    public List<Token> findByNodeTypeAndExecutionStatusIsActive(NodeType nodeType) {
        return getHibernateTemplate().find("from Token where nodeType=? and executionStatus=?", nodeType, ExecutionStatus.ACTIVE);
    }

    public List<Token> findByProcessAndExecutionStatusIsNotEnded(ru.runa.wfe.execution.Process process) {
        return getHibernateTemplate().find("from Token where process=? and executionStatus!=?", process, ExecutionStatus.ENDED);
    }

    public List<Token> findByProcessAndExecutionStatus(ru.runa.wfe.execution.Process process, ExecutionStatus status) {
        return getHibernateTemplate().find("from Token where process=? and executionStatus=?", process, status);
    }

}
