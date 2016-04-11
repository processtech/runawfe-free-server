/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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

import com.google.common.base.Preconditions;

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
    public Substitution createSubstitution(@WebParam(name = "user") User user, @WebParam(name = "substitution") Substitution substitution) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitution);
        substitutionLogic.create(user, substitution);
        return substitution;
    }

    @Override
    @WebResult(name = "result")
    public List<Substitution> getSubstitutions(@WebParam(name = "user") User user, @WebParam(name = "actorId") Long actorId) {
        return substitutionLogic.getSubstitutions(user, actorId);
    }

    @Override
    @WebResult(name = "result")
    public void deleteSubstitutions(@WebParam(name = "user") User user, @WebParam(name = "substitutionIds") List<Long> substitutionIds) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionIds);
        substitutionLogic.delete(user, substitutionIds);
    }

    @Override
    @WebResult(name = "result")
    public Substitution getSubstitution(@WebParam(name = "user") User user, @WebParam(name = "substitutionId") Long substitutionId) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getSubstitution(user, substitutionId);
    }

    @Override
    @WebResult(name = "result")
    public void updateSubstitution(@WebParam(name = "user") User user, @WebParam(name = "substitution") Substitution substitution) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitution);
        substitutionLogic.update(user, substitution);
    }

    @Override
    @WebResult(name = "result")
    public void createCriteria(@WebParam(name = "user") User user, @WebParam(name = "substitutionCriteria") SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.create(user, substitutionCriteria);
    }

    @Override
    @WebResult(name = "result")
    public SubstitutionCriteria getCriteria(@WebParam(name = "user") User user, @WebParam(name = "substitutionCriteriaId") Long substitutionCriteriaId) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getCriteria(user, substitutionCriteriaId);
    }

    @Override
    @WebResult(name = "result")
    public SubstitutionCriteria getCriteriaByName(@WebParam(name = "user") User user, @WebParam(name = "name") String name) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getCriteria(user, name);
    }

    @Override
    @WebResult(name = "result")
    public List<SubstitutionCriteria> getAllCriterias(@WebParam(name = "user") User user) {
        Preconditions.checkNotNull(user);
        return substitutionLogic.getAllCriterias(user);
    }

    @Override
    @WebResult(name = "result")
    public void updateCriteria(@WebParam(name = "user") User user, @WebParam(name = "substitutionCriteria") SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.update(user, substitutionCriteria);
    }

    @Override
    @WebResult(name = "result")
    public void deleteCriterias(@WebParam(name = "user") User user, @WebParam(name = "criterias") List<SubstitutionCriteria> criterias) {
        Preconditions.checkNotNull(user);
        substitutionLogic.deleteCriterias(user, criterias);
    }

    @Override
    @WebResult(name = "result")
    public void deleteCriteria(@WebParam(name = "user") User user, @WebParam(name = "substitutionCriteria") SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        substitutionLogic.delete(user, substitutionCriteria);
    }

    @Override
    @WebResult(name = "result")
    public List<Substitution> getSubstitutionsByCriteria(@WebParam(name = "user") User user,
            @WebParam(name = "substitutionCriteria") SubstitutionCriteria substitutionCriteria) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(substitutionCriteria);
        return substitutionLogic.getSubstitutionsByCriteria(user, substitutionCriteria);
    }

}
