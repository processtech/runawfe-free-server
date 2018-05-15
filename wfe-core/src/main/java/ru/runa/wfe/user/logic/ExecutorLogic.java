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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ProfileDAO;

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
        permissionDAO.checkAllowed(user, Permission.LIST, executor);
        return true;
    }

    public Executor update(User user, Executor executor) {
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        return executorDAO.update(executor);
    }

    public List<? extends Executor> getExecutors(User user, BatchPresentation batchPresentation) {
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        return compiler.getBatch();
    }

    public int getExecutorsCount(User user, BatchPresentation batchPresentation) {
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        return compiler.getCount();
    }

    public Actor getActor(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDAO.getActor(name), Permission.LIST);
    }

    public Actor getActorCaseInsensitive(String login) {
        return executorDAO.getActorCaseInsensitive(login);
    }

    public Group getGroup(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDAO.getGroup(name), Permission.LIST);
    }

    public Executor getExecutor(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDAO.getExecutor(name), Permission.LIST);
    }

    public boolean isAdministrator(User user) {
        return executorDAO.isAdministrator(user.getActor());
    }

    public void remove(User user, List<Long> ids) {
        List<Executor> executors = getExecutors(user, ids);
        checkPermissionsOnExecutors(user, executors, Permission.DELETE);
        for (Executor executor : executors) {
            remove(executor);
        }
    }

    public void remove(Executor executor) {
        log.info("Removing " + executor);
        if (permissionDAO.isPrivilegedExecutor(executor) || SystemExecutors.PROCESS_STARTER_NAME.equals(executor.getName())) {
            throw new AuthorizationException(executor.getName() + " can not be removed");
        }
        Set<Long> processIds = processDAO.getDependentProcessIds(executor);
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
        permissionDAO.checkAllowed(user, Permission.CREATE, SecuredSingleton.EXECUTORS);
        Collection<Permission> selfPermissions = SystemProperties.getDefaultPermissions(executor.getSecuredObjectType());
        executorDAO.create(executor);
        postCreateExecutor(user, executor, selfPermissions);
        return executor;
    }

    private void postCreateExecutor(User user, Executor executor, Collection<Permission> selfPermissions) {
        permissionDAO.setPermissions(user.getActor(), ApplicablePermissions.listVisible(executor), executor);
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
        checkPermissionsOnExecutors(user, executors, Permission.UPDATE);
        checkPermissionsOnExecutor(user, group, Permission.UPDATE);
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
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        checkPermissionsOnExecutors(user, groups, Permission.UPDATE);
        executorDAO.addExecutorToGroups(executor, groups);
    }

    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        // TODO Was (isExclude ? Permission.ADD_TO_GROUP : Permission.LIST_GROUP). That ADD_TO_GROUP is bad in get...() method.
        checkPermissionsOnExecutor(user, group, isExclude ? Permission.UPDATE : Permission.READ);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation,
                !isExclude);
        return compiler.getBatch();
    }

    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        // TODO Was (isExclude ? Permission.ADD_TO_GROUP : Permission.LIST_GROUP). That ADD_TO_GROUP is bad in get...() method.
        checkPermissionsOnExecutor(user, group, isExclude ? Permission.UPDATE : Permission.READ);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public List<Actor> getGroupActors(User user, Group group) {
        checkPermissionsOnExecutor(user, group, Permission.READ);
        Set<Actor> groupActors = executorDAO.getGroupActors(group);
        return filterSecuredObject(user, Lists.newArrayList(groupActors), Permission.LIST);
    }

    public List<Executor> getAllExecutorsFromGroup(User user, Group group) {
        checkPermissionsOnExecutor(user, group, Permission.READ);
        return filterSecuredObject(user, executorDAO.getAllNonGroupExecutorsFromGroup(group), Permission.LIST);
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
        checkPermissionsOnExecutor(user, group, Permission.UPDATE);
        checkPermissionsOnExecutors(user, executors, Permission.LIST);
        executorDAO.removeExecutorsFromGroup(executors, group);
    }

    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) {
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        checkPermissionsOnExecutors(user, groups, Permission.UPDATE);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) {
        Executor executor = executorDAO.getExecutor(executorId);
        List<Group> groups = executorDAO.getGroups(groupIds);
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        checkPermissionsOnExecutors(user, groups, Permission.UPDATE);
        executorDAO.removeExecutorFromGroups(executor, groups);
    }

    public void setPassword(User user, Actor actor, String password) {
        String passwordsRegexp = SystemProperties.getStrongPasswordsRegexp();
        if (!Strings.isNullOrEmpty(passwordsRegexp) && !Pattern.compile(passwordsRegexp).matcher(password).matches()) {
            throw new WeakPasswordException();
        }
        if (!permissionDAO.isAllowed(user, Permission.UPDATE, actor)) {
            if (user.getActor().equals(actor)) {
                permissionDAO.checkAllowed(user, Permission.UPDATE_SELF, SecuredSingleton.EXECUTORS);
            } else {
                throw new AuthorizationException(user + " hasn't permission to change password for actor " + actor);
            }
        }
        executorDAO.setPassword(actor, password);
    }

    public Actor setStatus(User user, Actor actor, boolean isActive, boolean callHandlers) {
        checkPermissionsOnExecutor(user, actor, Permission.UPDATE_STATUS);
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
        checkPermissionsOnExecutor(user, executor, Permission.LIST);
        PresentationConfiguredCompiler<Group> compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(user, executor, batchPresentation,
                !isExclude);
        return compiler.getBatch();
    }

    public int getExecutorGroupsCount(User user, Executor executor, BatchPresentation batchPresentation, boolean isExclude) {
        checkPermissionsOnExecutor(user, executor, Permission.LIST);
        PresentationConfiguredCompiler<Group> compiler = PresentationCompilerHelper.createExecutorGroupsCompiler(user, executor, batchPresentation,
                !isExclude);
        return compiler.getCount();
    }

    public boolean isExecutorInGroup(User user, Executor executor, Group group) {
        checkPermissionsOnExecutor(user, executor, Permission.LIST);
        checkPermissionsOnExecutor(user, group, Permission.LIST);
        return executorDAO.isExecutorInGroup(executor, group);
    }

    public Executor getExecutor(User user, Long id) {
        return checkPermissionsOnExecutor(user, executorDAO.getExecutor(id), Permission.LIST);
    }

    public List<Executor> getExecutors(User user, List<Long> ids) {
        return checkPermissionsOnExecutors(user, executorDAO.getExecutors(ids), Permission.LIST);
    }

    public Actor getActorByCode(User user, Long code) {
        return checkPermissionsOnExecutor(user, executorDAO.getActorByCode(code), Permission.LIST);
    }

    public Group saveTemporaryGroup(Group temporaryGroup, Collection<? extends Executor> newGroupExecutors) {
        if (executorDAO.isExecutorExist(temporaryGroup.getName())) {
            temporaryGroup = (Group) executorDAO.getExecutor(temporaryGroup.getName());
            Set<Executor> oldGroupExecutors = executorDAO.getGroupChildren(temporaryGroup);
            Set<Executor> executorsToDelete = new HashSet<>();
            Set<Executor> executorsToAdd = new HashSet<>();
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

    private void addNewExecutorsToGroup(Group temporaryGroup, Collection<? extends Executor> newGroupExecutors) {
        executorDAO.addExecutorsToGroup(newGroupExecutors, temporaryGroup);
        if (SystemProperties.setPermissionsToTemporaryGroups()) {
            Set<Executor> grantedExecutors = Sets.newHashSet();
            grantedExecutors.addAll(newGroupExecutors);
            for (Executor executor : newGroupExecutors) {
                grantedExecutors.addAll(permissionDAO.getExecutorsWithPermission(executor));
            }
            for (Executor executor : grantedExecutors) {
                permissionDAO.setPermissions(executor, Lists.newArrayList(Permission.READ, Permission.LIST), temporaryGroup);
            }
        }
    }

}
