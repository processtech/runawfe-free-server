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

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.ExecutorServiceLocal;
import ru.runa.wfe.service.decl.ExecutorServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

import com.google.common.base.Preconditions;

/**
 * Implements ExecutorService as bean.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ExecutorAPI", serviceName = "ExecutorWebService")
@SOAPBinding
@SuppressWarnings("unchecked")
public class ExecutorServiceBean implements ExecutorServiceLocal, ExecutorServiceRemote {
    @Autowired
    private ExecutorLogic executorLogic;

    @Override
    @WebResult(name = "result")
    public void update(@WebParam(name = "user") User user, @WebParam(name = "executor") Executor executor) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executor != null, "executor");
        executorLogic.update(user, executor);
    }

    @Override
    @WebResult(name = "result")
    public List<? extends Executor> getExecutors(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getExecutors(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public int getExecutorsCount(@WebParam(name = "user") User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getExecutorsCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public Actor getActorCaseInsensitive(@WebParam(name = "login") String login) {
        Preconditions.checkArgument(login != null, "login");
        return executorLogic.getActorCaseInsensitive(login);
    }

    @Override
    @WebResult(name = "result")
    public Executor getExecutorByName(@WebParam(name = "user") User user, @WebParam(name = "name") String name) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(name != null, "name");
        return executorLogic.getExecutor(user, name);
    }

    @Override
    @WebResult(name = "result")
    public void remove(@WebParam(name = "user") User user, @WebParam(name = "ids") List<Long> ids) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(ids != null, "ids");
        executorLogic.remove(user, ids);
    }

    @Override
    @WebResult(name = "result")
    public <T extends Executor> T create(@WebParam(name = "user") User user, @WebParam(name = "executor") T executor) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executor != null, "executor");
        return executorLogic.create(user, executor);
    }

    @Override
    @WebResult(name = "result")
    public void addExecutorsToGroup(@WebParam(name = "user") User user, @WebParam(name = "executorIds") List<Long> executorIds,
            @WebParam(name = "groupId") Long groupId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorIds != null, "executorIds");
        Preconditions.checkArgument(groupId != null, "groupId");
        executorLogic.addExecutorsToGroup(user, executorIds, groupId);
    }

    @Override
    @WebResult(name = "result")
    public void addExecutorToGroups(@WebParam(name = "user") User user, @WebParam(name = "executorId") Long executorId,
            @WebParam(name = "groupIds") List<Long> groupIds) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorId != null, "executorId");
        Preconditions.checkArgument(groupIds != null, "groupIds");
        executorLogic.addExecutorToGroups(user, executorId, groupIds);
    }

    @Override
    @WebResult(name = "result")
    public List<Executor> getGroupChildren(@WebParam(name = "user") User user, @WebParam(name = "group") Group group,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(group != null, "group");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getGroupChildren(user, group, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public int getGroupChildrenCount(@WebParam(name = "user") User user, @WebParam(name = "group") Group group,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(group != null, "group");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getGroupChildrenCount(user, group, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public List<Actor> getGroupActors(@WebParam(name = "user") User user, @WebParam(name = "group") Group group) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(group != null, "group");
        return executorLogic.getGroupActors(user, group);
    }

    @Override
    @WebResult(name = "result")
    public void removeExecutorsFromGroup(@WebParam(name = "user") User user, @WebParam(name = "executorIds") List<Long> executorIds,
            @WebParam(name = "groupId") Long groupId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorIds != null, "executorIds");
        Preconditions.checkArgument(groupId != null, "groupId");
        executorLogic.removeExecutorsFromGroup(user, executorIds, groupId);
    }

    @Override
    @WebResult(name = "result")
    public void removeExecutorFromGroups(@WebParam(name = "user") User user, @WebParam(name = "executorId") Long executorId,
            @WebParam(name = "groupIds") List<Long> groupIds) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorId != null, "executorId");
        Preconditions.checkArgument(groupIds != null, "groupIds");
        executorLogic.removeExecutorFromGroups(user, executorId, groupIds);
    }

    @Override
    @WebResult(name = "result")
    public void setPassword(@WebParam(name = "user") User user, @WebParam(name = "actor") Actor actor, @WebParam(name = "password") String password) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(actor != null, "actor");
        Preconditions.checkArgument(password != null, "password");
        executorLogic.setPassword(user, actor, password);
    }

    @Override
    @WebResult(name = "result")
    public void setStatus(@WebParam(name = "user") User user, @WebParam(name = "actor") Actor actor, @WebParam(name = "active") boolean active) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(actor != null, "actor");
        executorLogic.setStatus(user, actor, active, true);
    }

    @Override
    @WebResult(name = "result")
    public List<Group> getExecutorGroups(@WebParam(name = "user") User user, @WebParam(name = "executor") Executor executor,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executor != null, "executor");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        }
        return executorLogic.getExecutorGroups(user, executor, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public int getExecutorGroupsCount(@WebParam(name = "user") User user, @WebParam(name = "executor") Executor executor,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executor != null, "executor");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        }
        return executorLogic.getExecutorGroupsCount(user, executor, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public List<Executor> getAllExecutorsFromGroup(@WebParam(name = "user") User user, @WebParam(name = "group") Group group) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(group != null, "group");
        return executorLogic.getAllExecutorsFromGroup(user, group);
    }

    @Override
    @WebResult(name = "result")
    public boolean isExecutorInGroup(@WebParam(name = "user") User user, @WebParam(name = "executor") Executor executor,
            @WebParam(name = "group") Group group) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executor != null, "executor");
        Preconditions.checkArgument(group != null, "group");
        return executorLogic.isExecutorInGroup(user, executor, group);
    }

    @Override
    @WebResult(name = "result")
    public boolean isExecutorExist(@WebParam(name = "user") User user, @WebParam(name = "executorName") String executorName) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(executorName != null, "executorName");
        return executorLogic.isExecutorExist(user, executorName);
    }

    @Override
    @WebResult(name = "result")
    public Executor getExecutor(@WebParam(name = "user") User user, @WebParam(name = "id") Long id) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(id != null, "id");
        return executorLogic.getExecutor(user, id);
    }

    @Override
    @WebResult(name = "result")
    public Actor getActorByCode(@WebParam(name = "user") User user, @WebParam(name = "code") Long code) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(code != null, "code");
        return executorLogic.getActorByCode(user, code);
    }

    @Override
    @WebResult(name = "result")
    public boolean isAdministrator(@WebParam(name = "user") User user) {
        Preconditions.checkArgument(user != null, "user");
        return executorLogic.isAdministrator(user);
    }

}
