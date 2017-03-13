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
package ru.runa.wfe.user.logic;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ProfileDAO;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Created on 14.03.2005
 */
public class ExecutorLogic extends CommonLogic {
    private static final Log log = LogFactory.getLog(ExecutorLogic.class);
    private List<SetStatusHandler> setStatusHandlers;

    @Autowired
    private ProfileDAO profileDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;
    @Autowired
    private SubstitutionDAO substitutionDAO;

    @Required
    public void setSetStatusHandlers(List<SetStatusHandler> setStatusHandlers) {
        this.setStatusHandlers = setStatusHandlers;
    }

    public boolean isExecutorExist(User user, String executorName) {
        if (!executorDAO.isExecutorExist(executorName)) {
            return false;
        }
        Executor executor = executorDAO.getExecutor(executorName);
        checkPermissionAllowed(user, executor, Permission.READ);
        return true;
    }

    public Executor update(User user, Executor executor) {
        checkPermissionsOnExecutor(user, executor, ExecutorPermission.UPDATE);
        return executorDAO.update(executor);
    }

    public List<? extends Executor> getExecutors(User user, BatchPresentation batchPresentation) {
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getExecutorsCount(User user, BatchPresentation batchPresentation) {
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        return compiler.getCount();
    }

    public Actor getActor(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDAO.getActor(name), Permission.READ);
    }

    public Actor getActorCaseInsensitive(String login) {
        return executorDAO.getActorCaseInsensitive(login);
    }

    public Group getGroup(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDAO.getGroup(name), Permission.READ);
    }

    public Executor getExecutor(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDAO.getExecutor(name), Permission.READ);
    }

    public boolean isAdministrator(User user) {
        return executorDAO.isAdministrator(user.getActor());
    }

    public void remove(User user, List<Long> ids) {
        List<Executor> executors = getExecutors(user, ids);
        checkPermissionsOnExecutors(user, executors, ExecutorPermission.UPDATE);
        for (Executor executor : executors) {
            remove(executor);
        }
    }

    public void remove(Executor executor) {
        if (permissionDAO.isPrivilegedExecutor(executor) || SystemExecutors.PROCESS_STARTER_NAME.equals(executor.getName())) {
            throw new AuthorizationException(executor.getName() + " can not be removed");
        }
        Set<Number> processIds = processDAO.getDependentProcessIds(executor);
        if (processIds.size() > 0) {
            throw new ExecutorParticipatesInProcessesException(executor.getName(), processIds);
        }
        if (executor instanceof Actor) {
            profileDAO.delete((Actor) executor);
        }
        permissionDAO.deleteOwnPermissions(executor);
        permissionDAO.deleteAllPermissions(executor);
        relationPairDAO.removeAllRelationPairs(executor);
        substitutionDAO.deleteAllActorSubstitutions(executor.getId());
        executorDAO.remove(executor);
    }

    public <T extends Executor> T create(User user, T executor) {
        checkPermissionAllowed(user, ASystem.INSTANCE, SystemPermission.CREATE_EXECUTOR);
        Collection<Permission> selfPermissions;
        if (executor instanceof Group) {
            selfPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);
        } else {
            selfPermissions = Lists.newArrayList(Permission.READ);
        }
        executorDAO.create(executor);
        postCreateExecutor(user, executor, selfPermissions);
        return executor;
    }

    private void postCreateExecutor(User user, Executor executor, Collection<Permission> selfPermissions) {
        Collection<Permission> p = executor.getSecuredObjectType().getNoPermission().getAllPermissions();
        permissionDAO.setPermissions(user.getActor(), p, executor);
        permissionDAO.setPermissions(executor, selfPermissions, executor);
    }

    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) {
        addExecutorsToGroupInternal(user, executors, group);
    }

    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        addExecutorsToGroupInternal(user, executors, group);
    }

    private void addExecutorsToGroupInternal(User user, List<? extends Executor> executors, Group group) {
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        checkPermissionsOnExecutor(user, group, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorsToGroup(executors, group);
    }

    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) {
        addExecutorToGroupsInternal(user, executor, groups);
    }

    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        addExecutorToGroupsInternal(user, executor, groups);
    }

    private void addExecutorToGroupsInternal(User user, Executor executor, List<Group> groups) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutors(user, groups, GroupPermission.ADD_TO_GROUP);
        executorDAO.addExecutorToGroups(executor, groups);
    }

    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        checkPermissionsOnExecutor(user, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation,
                !isExclude);
        List<Executor> executorList = compiler.getBatch();
        return executorList;
    }

    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        checkPermissionsOnExecutor(user, group, isExclude ? GroupPermission.ADD_TO_GROUP : GroupPermission.LIST_GROUP);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public List<Actor> getGroupActors(User user, Group group) {
        checkPermissionsOnExecutor(user, group, GroupPermission.LIST_GROUP);
        Set<Actor> groupActors = executorDAO.getGroupActors(group);
        return filterIdentifiable(user, Lists.newArrayList(groupActors), Permission.READ);
    }

    public List<Executor> getAllExecutorsFromGroup(User user, Group group) {
        checkPermissionsOnExecutor(user, group, GroupPermission.LIST_GROUP);
        return filterIdentifiable(user, executorDAO.getAllNonGroupExecutorsFromGroup(group), Permission.READ);
    }

    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) {
        removeExecutorsFromGroupInternal(user, executors, group);
    }

    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Group group = executorDAO.getGroup(groupId);
        removeExecutorsFromGroupInternal(user, executors, group);
    }

    private void removeExecutorsFromGroupInternal(User user, List<? extends Executor> executors, Group group) {
        checkPermissionsOnExecutor(user, group, GroupPermission.REMOVE_FROM_GROUP);
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        executorDAO.removeExecutorsFromGroup(executors, group);
    }

    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutors(user, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutors(user, groups, GroupPermission.REMOVE_FROM_GROUP);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void setPassword(User user, Actor actor, String password) {
        String passwordsRegexp = SystemProperties.getStrongPasswordsRegexp();
        if (!Strings.isNullOrEmpty(passwordsRegexp) && !Pattern.compile(passwordsRegexp).matcher(password).matches()) {
            throw new WeakPasswordException();
        }
        if (!isPermissionAllowed(user, actor, ExecutorPermission.UPDATE)) {
            if (user.getActor().equals(actor)) {
                checkPermissionAllowed(user, ASystem.INSTANCE, SystemPermission.CHANGE_SELF_PASSWORD);
            } else {
                throw new AuthorizationException(user + " hasn't permission to change password for actor " + actor);
            }
        }
        executorDAO.setPassword(actor, password);
    }

    public Actor setStatus(User user, Actor actor, boolean isActive, boolean callHandlers) {
        checkPermissionsOnExecutor(user, actor, ActorPermission.UPDATE_STATUS);
        Actor updated = executorDAO.setStatus(actor, isActive);
        if (callHandlers) {
            for (SetStatusHandler handler : setStatusHandlers) {
                try {
                    handler.onStatusChange(actor, isActive);
                } catch (Throwable e) {
                    log.warn("Exception while calling loginHandler " + handler, e);
                }
            }
        }
        return updated;
    }

    public List<Group> getExecutorGroups(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        PresentationConfiguredCompiler<Group> compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(user, executor, batchPresentation,
                !isExclude);
        List<Group> executorList = compiler.getBatch();
        return executorList;
    }

    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        PresentationConfiguredCompiler<Group> compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(user, executor, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public boolean isExecutorInGroup(User user, Executor executor, Group group) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionsOnExecutor(user, group, Permission.READ);
        return executorDAO.isExecutorInGroup(executor, group);
    }

    public Executor getExecutor(User user, Long id) {
        return checkPermissionsOnExecutor(user, executorDAO.getExecutor(id), Permission.READ);
    }

    public List<Executor> getExecutors(User user, List<Long> ids) {
        return checkPermissionsOnExecutors(user, executorDAO.getExecutors(ids), Permission.READ);
    }

    public Actor getActorByCode(User user, Long code) {
        return checkPermissionsOnExecutor(user, executorDAO.getActorByCode(code), Permission.READ);
    }

    public Group saveTemporaryGroup(Group temporaryGroup, Collection<? extends Executor> newGroupExecutors) {
        if (executorDAO.isExecutorExist(temporaryGroup.getName())) {
            temporaryGroup = (Group) executorDAO.getExecutor(temporaryGroup.getName());
            Set<Executor> oldGroupExecutors = executorDAO.getGroupChildren(temporaryGroup);
            Set<Executor> executorsToDelete = new HashSet<Executor>();
            Set<Executor> executorsToAdd = new HashSet<Executor>();
            for (Executor executor : oldGroupExecutors) {
                if (!newGroupExecutors.contains(executor)) {
                    executorsToDelete.add(executor);
                }
            }
            for (Executor executor : newGroupExecutors) {
                if (!oldGroupExecutors.contains(executor)) {
                    executorsToAdd.add(executor);
                }
            }
            executorDAO.deleteExecutorsFromGroup(temporaryGroup, executorsToDelete);
            addNewExecutorsToGroup(temporaryGroup, executorsToAdd);
        } else {
            temporaryGroup = executorDAO.create(temporaryGroup);
            addNewExecutorsToGroup(temporaryGroup, newGroupExecutors);
        }
        return temporaryGroup;
    }

    public List<TemporaryGroup> getTemporaryGroups() {
        return executorDAO.getTemporaryGroups();
    }

    private void addNewExecutorsToGroup(Group temporaryGroup, Collection<? extends Executor> newGroupExecutors) {
        executorDAO.addExecutorsToGroup(newGroupExecutors, temporaryGroup);
        if (SystemProperties.setPermissionsToTemporaryGroups()) {
            Set<Executor> grantedExecutors = Sets.newHashSet();
            grantedExecutors.addAll(newGroupExecutors);
            for (Executor executor : newGroupExecutors) {
                grantedExecutors.addAll(permissionDAO.getExecutorsWithPermission(executor));
            }
            for (Executor executor : grantedExecutors) {
                permissionDAO.setPermissions(executor, Lists.newArrayList(GroupPermission.LIST_GROUP, GroupPermission.READ), temporaryGroup);
            }
        }
    }

}
