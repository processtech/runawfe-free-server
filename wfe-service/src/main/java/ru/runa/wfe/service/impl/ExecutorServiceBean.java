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
import lombok.NonNull;
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
    public void update(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executor") @NonNull Executor executor) {
        executorLogic.update(user, executor);
    }

    @Override
    @WebResult(name = "result")
    public List<? extends Executor> getExecutors(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getExecutors(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public int getExecutorsCount(@WebParam(name = "user") @NonNull User user, @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getExecutorsCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public Actor getActorCaseInsensitive(@WebParam(name = "login") @NonNull String login) {
        return executorLogic.getActorCaseInsensitive(login);
    }

    @Override
    @WebResult(name = "result")
    public Executor getExecutorByName(@WebParam(name = "user") @NonNull User user, @WebParam(name = "name") @NonNull String name) {
        return executorLogic.getExecutor(user, name);
    }

    @Override
    @WebResult(name = "result")
    public void remove(@WebParam(name = "user") @NonNull User user, @WebParam(name = "ids") @NonNull List<Long> ids) {
        executorLogic.remove(user, ids);
    }

    @Override
    @WebResult(name = "result")
    public <T extends Executor> T create(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executor") @NonNull T executor) {
        return executorLogic.create(user, executor);
    }

    @Override
    @WebResult(name = "result")
    public void addExecutorsToGroup(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executorIds") @NonNull List<Long> executorIds,
            @WebParam(name = "groupId") @NonNull Long groupId) {
        executorLogic.addExecutorsToGroup(user, executorIds, groupId);
    }

    @Override
    @WebResult(name = "result")
    public void addExecutorToGroups(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executorId") @NonNull Long executorId,
            @WebParam(name = "groupIds") @NonNull List<Long> groupIds) {
        executorLogic.addExecutorToGroups(user, executorId, groupIds);
    }

    @Override
    @WebResult(name = "result")
    public List<Executor> getGroupChildren(@WebParam(name = "user") @NonNull User user, @WebParam(name = "group") @NonNull Group group,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getGroupChildren(user, group, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public int getGroupChildrenCount(@WebParam(name = "user") @NonNull User user, @WebParam(name = "group") @NonNull Group group,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        return executorLogic.getGroupChildrenCount(user, group, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public List<Actor> getGroupActors(@WebParam(name = "user") @NonNull User user, @WebParam(name = "group") @NonNull Group group) {
        return executorLogic.getGroupActors(user, group);
    }

    @Override
    @WebResult(name = "result")
    public void removeExecutorsFromGroup(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executorIds") @NonNull List<Long> executorIds,
            @WebParam(name = "groupId") @NonNull Long groupId) {
        executorLogic.removeExecutorsFromGroup(user, executorIds, groupId);
    }

    @Override
    @WebResult(name = "result")
    public void removeExecutorFromGroups(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executorId") @NonNull Long executorId,
            @WebParam(name = "groupIds") @NonNull List<Long> groupIds) {
        executorLogic.removeExecutorFromGroups(user, executorId, groupIds);
    }

    @Override
    @WebResult(name = "result")
    public void setPassword(@WebParam(name = "user") @NonNull User user, @WebParam(name = "actor") @NonNull Actor actor,
            @WebParam(name = "password") @NonNull String password) {
        executorLogic.setPassword(user, actor, password);
    }

    @Override
    @WebResult(name = "result")
    public void setStatus(@WebParam(name = "user") @NonNull User user, @WebParam(name = "actor") @NonNull Actor actor,
            @WebParam(name = "active") boolean active) {
        executorLogic.setStatus(user, actor, active, true);
    }

    @Override
    @WebResult(name = "result")
    public List<Group> getExecutorGroups(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executor") @NonNull Executor executor,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        }
        return executorLogic.getExecutorGroups(user, executor, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public int getExecutorGroupsCount(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executor") @NonNull Executor executor,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "excluded") boolean excluded) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        }
        return executorLogic.getExecutorGroupsCount(user, executor, batchPresentation, excluded);
    }

    @Override
    @WebResult(name = "result")
    public List<Executor> getAllExecutorsFromGroup(@WebParam(name = "user") @NonNull User user, @WebParam(name = "group") @NonNull Group group) {
        return executorLogic.getAllExecutorsFromGroup(user, group);
    }

    @Override
    @WebResult(name = "result")
    public boolean isExecutorInGroup(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executor") @NonNull Executor executor,
            @WebParam(name = "group") @NonNull Group group) {
        return executorLogic.isExecutorInGroup(user, executor, group);
    }

    @Override
    @WebResult(name = "result")
    public boolean isExecutorExist(@WebParam(name = "user") @NonNull User user, @WebParam(name = "executorName") @NonNull String executorName) {
        return executorLogic.isExecutorExist(user, executorName);
    }

    @Override
    @WebResult(name = "result")
    public Executor getExecutor(@WebParam(name = "user") @NonNull User user, @WebParam(name = "id") @NonNull Long id) {
        return executorLogic.getExecutor(user, id);
    }

    @Override
    @WebResult(name = "result")
    public Actor getActorByCode(@WebParam(name = "user") @NonNull User user, @WebParam(name = "code") @NonNull Long code) {
        return executorLogic.getActorByCode(user, code);
    }

    @Override
    @WebResult(name = "result")
    public boolean isAdministrator(@WebParam(name = "user") @NonNull User user) {
        return executorLogic.isAdministrator(user);
    }
}
