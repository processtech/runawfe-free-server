package ru.runa.wfe.execution.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;

/**
 * DAO for {@link Token}.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public class TokenDAO extends GenericDAO<Token> {

    // public Token findTokenByProcessIdAndNodeIdNotNull(Long processId, String
    // nodeId) {
    // List<Token> tokens =
    // getHibernateTemplate().find("from Token t where t.nodeId=? and t.process.id=?",
    // nodeId, processId);
    // if (tokens.size() > 1) {
    // throw new
    // InternalApplicationException(String.format("Multiple tokens found for %s:%s",
    // nodeId, processId));
    // }
    // if (tokens.size() == 0) {
    // throw new
    // InternalApplicationException(String.format("No token found for %s:%s",
    // nodeId, processId));
    // }
    // return tokens.get(0);
    // }

    public List<Token> findActiveTokens(NodeType nodeType) {
        return getHibernateTemplate().find("from Token where nodeType=? and endDate is null", nodeType);
    }

    public List<Token> findActiveTokens(ru.runa.wfe.execution.Process process) {
        return getHibernateTemplate().find("from Token where process=? and endDate is null", process);
    }

}
