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
import ru.runa.wfe.service.decl.SubstitutionServiceLocal;
import ru.runa.wfe.service.decl.SubstitutionServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.user.User;

/**
 * Created on 30.01.2006
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "SubstitutionAPI", serviceName = "SubstitutionWebService")
@SOAPBinding
public class SubstitutionServiceBean implements SubstitutionServiceLocal, SubstitutionServiceRemote {
    @Autowired
    private SubstitutionLogic substitutionLogic;

    @Override
    @WebResult(name = "result")
    public Substitution createSubstitution(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "substitution") @NonNull Substitution substitution) {
        substitutionLogic.create(user, substitution);
        return substitution;
    }

    @Override
    @WebResult(name = "result")
    public List<Substitution> getSubstitutions(@WebParam(name = "user") @NonNull User user, @WebParam(name = "actorId") @NonNull Long actorId) {
        return substitutionLogic.getSubstitutions(user, actorId);
    }

    @Override
    @WebResult(name = "result")
    public void deleteSubstitutions(@WebParam(name = "user") @NonNull User user, @WebParam(name = "substitutionIds") @NonNull List<Long> ids) {
        substitutionLogic.delete(user, ids);
    }

    @Override
    @WebResult(name = "result")
    public Substitution getSubstitution(@WebParam(name = "user") @NonNull User user, @WebParam(name = "substitutionId") @NonNull Long id) {
        return substitutionLogic.getSubstitution(user, id);
    }

    @Override
    @WebResult(name = "result")
    public void updateSubstitution(@WebParam(name = "user") @NonNull User user, @WebParam(name = "substitution") @NonNull Substitution substitution) {
        substitutionLogic.update(user, substitution);
    }

    @Override
    @WebResult(name = "result")
    public void createCriteria(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "substitutionCriteria") @NonNull SubstitutionCriteria substitutionCriteria) {
        substitutionLogic.create(user, substitutionCriteria);
    }

    @Override
    @WebResult(name = "result")
    public SubstitutionCriteria getCriteria(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "substitutionCriteriaId") @NonNull Long substitutionCriteriaId) {
        return substitutionLogic.getCriteria(user, substitutionCriteriaId);
    }

    @Override
    @WebResult(name = "result")
    public SubstitutionCriteria getCriteriaByName(@WebParam(name = "user") @NonNull User user, @WebParam(name = "name") @NonNull String name) {
        return substitutionLogic.getCriteria(user, name);
    }

    @Override
    @WebResult(name = "result")
    public List<SubstitutionCriteria> getAllCriterias(@WebParam(name = "user") @NonNull User user) {
        return substitutionLogic.getAllCriterias(user);
    }

    @Override
    @WebResult(name = "result")
    public void updateCriteria(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "substitutionCriteria") @NonNull SubstitutionCriteria substitutionCriteria) {
        substitutionLogic.update(user, substitutionCriteria);
    }

    @Override
    @WebResult(name = "result")
    public void deleteCriterias(@WebParam(name = "user") @NonNull User user,
            @NonNull @WebParam(name = "criterias") List<SubstitutionCriteria> criterias) {
        substitutionLogic.deleteCriterias(user, criterias);
    }

    @Override
    @WebResult(name = "result")
    public void deleteCriteria(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "substitutionCriteria") @NonNull SubstitutionCriteria substitutionCriteria) {
        substitutionLogic.delete(user, substitutionCriteria);
    }

    @Override
    @WebResult(name = "result")
    public List<Substitution> getSubstitutionsByCriteria(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "substitutionCriteria") @NonNull SubstitutionCriteria substitutionCriteria) {
        return substitutionLogic.getSubstitutionsByCriteria(user, substitutionCriteria);
    }
}
