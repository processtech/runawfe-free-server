package ru.runa.wfe.service.impl;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.service.decl.RelationServiceLocal;
import ru.runa.wfe.service.decl.RelationServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Implements RelationService as bean.
 *
 * @author Konstantinov Aleksey 12.02.2012
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "RelationAPI", serviceName = "RelationWebService")
@SOAPBinding
public class RelationServiceBean implements RelationServiceLocal, RelationServiceRemote {
    @Autowired
    private RelationLogic relationLogic;

    @Override
    @WebResult(name = "result")
    public RelationPair addRelationPair(@WebParam(name = "user") @NonNull User user, @WebParam(name = "relationId") @NonNull Long relationId,
            @WebParam(name = "from") @NonNull Executor from, @WebParam(name = "to") @NonNull Executor to) {
        return relationLogic.addRelationPair(user, relationId, from, to);
    }

    @Override
    @WebResult(name = "result")
    public Relation createRelation(@WebParam(name = "user") @NonNull User user, @WebParam(name = "relation") @NonNull Relation relation) {
        return relationLogic.createRelation(user, relation);
    }

    @Override
    @WebResult(name = "result")
    public Relation updateRelation(@WebParam(name = "user") @NonNull User user, @WebParam(name = "relation") @NonNull Relation relation) {
        return relationLogic.updateRelation(user, relation);
    }

    @Override
    @WebResult(name = "result")
    public List<Relation> getRelations(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.RELATIONS.createNonPaged();
        }
        return relationLogic.getRelations(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public Relation getRelationByName(@WebParam(name = "user") @NonNull User user, @WebParam(name = "name") @NonNull String name) {
        return relationLogic.getRelation(user, name);
    }

    @Override
    @WebResult(name = "result")
    public Relation getRelation(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        return relationLogic.getRelation(user, id);
    }

    @Override
    @WebResult(name = "result")
    public List<RelationPair> getExecutorsRelationPairsRight(@WebParam(name = "user") @NonNull User user, @WebParam(name = "name") String name,
            @WebParam(name = "right") @NonNull List<? extends Executor> right) {
        return relationLogic.getExecutorRelationPairsRight(user, name, right);
    }

    @Override
    @WebResult(name = "result")
    public List<RelationPair> getExecutorsRelationPairsLeft(@WebParam(name = "user") @NonNull User user, @WebParam(name = "name") String name,
            @WebParam(name = "left") @NonNull List<? extends Executor> left) {
        return relationLogic.getExecutorRelationPairsLeft(user, name, left);
    }

    @Override
    @WebResult(name = "result")
    public List<Relation> getRelationsContainingExecutorsOnLeft(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "executor") @NonNull List<Executor> executors) {
        return relationLogic.getRelationsContainingExecutorsOnLeft(user, executors);
    }

    @Override
    @WebResult(name = "result")
    public List<Relation> getRelationsContainingExecutorsOnRight(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "executor") @NonNull List<Executor> executors) {
        return relationLogic.getRelationsContainingExecutorsOnRight(user, executors);
    }

    @Override
    @WebResult(name = "result")
    public List<RelationPair> getRelationPairs(@WebParam(name = "user") @NonNull User user, @WebParam(name = "name") @NonNull String name,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.RELATION_PAIRS.createNonPaged();
        }
        return relationLogic.getRelations(user, name, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public void removeRelationPair(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        relationLogic.removeRelationPair(user, id);
    }

    @Override
    @WebResult(name = "result")
    public void removeRelationPairs(@WebParam(name = "user") @NonNull User user, @WebParam(name = "ids") @NonNull List<Long> ids) {
        for (Long id : ids) {
            relationLogic.removeRelationPair(user, id);
        }
    }

    @Override
    @WebResult(name = "result")
    public void removeRelation(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        relationLogic.removeRelation(user, id);
    }
}
