package ru.runa.wfe.user.logic;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.persistence.DiscriminatorValue;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.digitalsignature.dao.DigitalSignatureDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.relation.dao.RelationPairDao;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.ss.dao.SubstitutionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.DelegationGroup;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ProfileDao;

/**
 * Created on 14.03.2005
 */
public class ExecutorLogic extends CommonLogic {
    private List<SetStatusHandler> setStatusHandlers;

    @Autowired
    private ProfileDao profileDao;
    @Autowired
    private RelationPairDao relationPairDao;
    @Autowired
    private SubstitutionDao substitutionDao;
    @Autowired
    private DigitalSignatureDao digitalSignatureDao;
    @Autowired
    private AuthorizationLogic authorizationLogic;

    @Required
    public void setSetStatusHandlers(List<SetStatusHandler> setStatusHandlers) {
        this.setStatusHandlers = setStatusHandlers;
    }

    public boolean isExecutorExist(User user, String executorName) {
        if (!executorDao.isExecutorExist(executorName)) {
            return false;
        }
        Executor executor = executorDao.getExecutor(executorName);
        permissionDao.checkAllowed(user, Permission.READ, executor);
        return true;
    }

    public Executor update(User user, Executor executor) {
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        return executorDao.update(executor);
    }

    public List<? extends Executor> getExecutors(User user, List<Long> ids) {
        return executorDao.getExecutors(ids);
    }

    public List<? extends Executor> getExecutors(User user, BatchPresentation batchPresentation) {
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        return compiler.getBatch();
    }

    public List<? extends Executor> getNotTemporaryExecutors(User user, BatchPresentation batchPresentation) {
        List<String> restrictions = batchPresentation.getType().getRestrictions();
        String temporaryExecutorsExclusionQuery = buildTemporaryExecutorsExclusionQuery();
        restrictions.add(temporaryExecutorsExclusionQuery);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        List<? extends Executor> executors = compiler.getBatch();
        restrictions.remove(temporaryExecutorsExclusionQuery);
        return executors;
    }

    public int getExecutorsCount(User user, BatchPresentation batchPresentation) {
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        return compiler.getCount();
    }

    public int getNotTemporaryExecutorsCount(User user, BatchPresentation batchPresentation) {
        List<String> restrictions = batchPresentation.getType().getRestrictions();
        String temporaryExecutorsExclusionQuery = buildTemporaryExecutorsExclusionQuery();
        restrictions.add(temporaryExecutorsExclusionQuery);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createAllExecutorsCompiler(user, batchPresentation);
        int executorsCount = compiler.getCount();
        restrictions.remove(temporaryExecutorsExclusionQuery);
        return executorsCount;
    }

    public Actor getActor(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDao.getActor(name), Permission.READ);
    }

    public Actor getActor(User user, Long id) {
        return checkPermissionsOnExecutor(user, executorDao.getActor(id), Permission.READ);
    }

    public Actor getActorCaseInsensitive(String login) {
        return executorDao.getActorCaseInsensitive(login);
    }

    public Group getGroup(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDao.getGroup(name), Permission.READ);
    }

    public Group getGroup(User user, Long id) {
        return checkPermissionsOnExecutor(user, executorDao.getGroup(id), Permission.READ);
    }

    public Executor getExecutor(User user, String name) {
        return checkPermissionsOnExecutor(user, executorDao.getExecutor(name), Permission.READ);
    }

    public boolean isAdministrator(User user) {
        return executorDao.isAdministrator(user.getActor());
    }

    public boolean isAdministrator(Actor actor) {
        return executorDao.isAdministrator(actor);
    }

    public void remove(User user, List<Long> ids) {
        List<Executor> executors = checkPermissionsOnExecutors(user, executorDao.getExecutors(ids), Permission.UPDATE);
        for (Executor executor : executors) {
            remove(executor);
        }
    }

    public void remove(Executor executor) {
        log.info("Removing " + executor);
        if (permissionDao.isPrivilegedExecutor(executor) || SystemExecutors.PROCESS_STARTER_NAME.equals(executor.getName())) {
            throw new AuthorizationException(executor.getName() + " can not be removed");
        }
        Set<Long> processIds = processDao.getDependentProcessIds(executor, ExecutorParticipatesInProcessesException.LIMIT + 1);
        if (!processIds.isEmpty()) {
            throw new ExecutorParticipatesInProcessesException(executor.getName(), processIds);
        }
        if (executor instanceof Actor) {
            profileDao.delete((Actor) executor);
        }
        permissionDao.deleteOwnPermissions(executor);
        permissionDao.deleteAllPermissions(executor);
        relationPairDao.removeAllRelationPairs(executor);
        substitutionDao.deleteAllActorSubstitutions(executor.getId());
        executorDao.remove(executor);
        digitalSignatureDao.remove(executor.getId());

    }

    public <T extends Executor> T create(User user, T executor) {
        permissionDao.checkAllowed(user, Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM);
        executorDao.create(executor);
        permissionDao.setPermissions(user.getActor(), ApplicablePermissions.listVisible(executor), executor);
        permissionDao.setPermissions(executor, Collections.singletonList(Permission.READ), executor);
        return executor;
    }

    public void addExecutorsToGroup(User user, List<? extends Executor> executors, Group group) {
        addExecutorsToGroupInternal(user, executors, group);
    }

    public void addExecutorsToGroup(User user, List<Long> executorIds, Long groupId) {
        List<Executor> executors = executorDao.getExecutors(executorIds);
        Group group = executorDao.getGroup(groupId);
        addExecutorsToGroupInternal(user, executors, group);
    }

    private void addExecutorsToGroupInternal(User user, List<? extends Executor> executors, Group group) {
        checkPermissionsOnExecutors(user, executors, Permission.UPDATE);
        checkPermissionsOnExecutor(user, group, Permission.UPDATE);
        executorDao.addExecutorsToGroup(executors, group);
    }

    public void addExecutorToGroups(User user, Executor executor, List<Group> groups) {
        addExecutorToGroupsInternal(user, executor, groups);
    }

    public void addExecutorToGroups(User user, Long executorId, List<Long> groupIds) {
        Executor executor = executorDao.getExecutor(executorId);
        List<Group> groups = executorDao.getGroups(groupIds);
        addExecutorToGroupsInternal(user, executor, groups);
    }

    private void addExecutorToGroupsInternal(User user, Executor executor, List<Group> groups) {
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        checkPermissionsOnExecutors(user, groups, Permission.UPDATE);
        executorDao.addExecutorToGroups(executor, groups);
    }

    public List<Executor> getGroupChildren(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        // TODO Was (isExclude ? Permission.ADD_TO_GROUP : Permission.LIST_GROUP). That ADD_TO_GROUP is bad in get...() method.
        checkPermissionsOnExecutor(user, group, isExclude ? Permission.UPDATE : Permission.READ);
        val compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation, isExclude);
        return compiler.getBatch();
    }

    public int getGroupChildrenCount(User user, Group group, BatchPresentation batchPresentation, boolean isExclude) {
        // TODO Was (isExclude ? Permission.ADD_TO_GROUP : Permission.LIST_GROUP). That ADD_TO_GROUP is bad in get...() method.
        checkPermissionsOnExecutor(user, group, isExclude ? Permission.UPDATE : Permission.READ);
        val compiler = PresentationCompilerHelper.createGroupChildrenCompiler(user, group, batchPresentation, isExclude);
        return compiler.getCount();
    }

    public List<Actor> getGroupActors(User user, Group group) {
        checkPermissionsOnExecutor(user, group, Permission.READ);
        Set<Actor> groupActors = executorDao.getGroupActors(group);
        return filterSecuredObject(user, Lists.newArrayList(groupActors), Permission.READ);
    }

    public List<Executor> getAllExecutorsFromGroup(User user, Group group) {
        checkPermissionsOnExecutor(user, group, Permission.READ);
        return filterSecuredObject(user, executorDao.getAllNonGroupExecutorsFromGroup(group), Permission.READ);
    }

    public void removeExecutorsFromGroup(User user, List<? extends Executor> executors, Group group) {
        removeExecutorsFromGroupInternal(user, executors, group);
    }

    public void removeExecutorsFromGroup(User user, List<Long> executorIds, Long groupId) {
        List<Executor> executors = executorDao.getExecutors(executorIds);
        Group group = executorDao.getGroup(groupId);
        removeExecutorsFromGroupInternal(user, executors, group);
    }

    private void removeExecutorsFromGroupInternal(User user, List<? extends Executor> executors, Group group) {
        checkPermissionsOnExecutor(user, group, Permission.UPDATE);
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        executorDao.removeExecutorsFromGroup(executors, group);
    }

    public void removeExecutorFromGroups(User user, Executor executor, List<Group> groups) {
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        checkPermissionsOnExecutors(user, groups, Permission.UPDATE);
        executorDao.removeExecutorFromGroups(executor, groups);
    }

    public void removeExecutorFromGroups(User user, Long executorId, List<Long> groupIds) {
        Executor executor = executorDao.getExecutor(executorId);
        List<Group> groups = executorDao.getGroups(groupIds);
        checkPermissionsOnExecutor(user, executor, Permission.UPDATE);
        checkPermissionsOnExecutors(user, groups, Permission.UPDATE);
        executorDao.removeExecutorFromGroups(executor, groups);
    }

    public void setPassword(User user, Actor actor, String password) {
        String passwordsRegexp = SystemProperties.getStrongPasswordsRegexp();
        if (!Strings.isNullOrEmpty(passwordsRegexp) && !Pattern.compile(passwordsRegexp).matcher(password).matches()) {
            throw new WeakPasswordException();
        }
        if (!permissionDao.isAllowed(user, Permission.UPDATE, actor)) {
            if (Objects.equals(actor.getId(), user.getActor().getId())) {
                permissionDao.checkAllowed(user, Permission.CHANGE_SELF_PASSWORD, SecuredSingleton.SYSTEM);
            } else {
                throw new AuthorizationException(user + " hasn't permission to change password for actor " + actor);
            }
        }
        executorDao.setPassword(actor, password);
    }

    public Actor setStatus(User user, Actor actor, boolean isActive, boolean callHandlers) {
        checkPermissionsOnExecutor(user, actor, Permission.UPDATE_ACTOR_STATUS);
        Actor updated = executorDao.setStatus(actor, isActive);
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
        return compiler.getBatch();
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
        return executorDao.isExecutorInGroup(executor, group);
    }

    public Executor getExecutor(User user, Long id) {
        return checkPermissionsOnExecutor(user, executorDao.getExecutor(id), Permission.READ);
    }

    public Actor getActorByCode(User user, Long code) {
        return checkPermissionsOnExecutor(user, executorDao.getActorByCode(code), Permission.READ);
    }

    public Group saveTemporaryGroup(Group temporaryGroup, Collection<? extends Executor> newGroupExecutors) {
        if (executorDao.isExecutorExist(temporaryGroup.getName())) {
            temporaryGroup = (Group) executorDao.getExecutor(temporaryGroup.getName());
            Set<Executor> oldGroupExecutors = executorDao.getGroupChildren(temporaryGroup);
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
            executorDao.removeExecutorsFromGroup(executorsToDelete, temporaryGroup);
            addNewExecutorsToGroup(temporaryGroup, executorsToAdd);
        } else {
            temporaryGroup = executorDao.create(temporaryGroup);
            addNewExecutorsToGroup(temporaryGroup, newGroupExecutors);
        }
        return temporaryGroup;
    }

    private void addNewExecutorsToGroup(Group temporaryGroup, Collection<? extends Executor> newGroupExecutors) {
        executorDao.addExecutorsToGroup(newGroupExecutors, temporaryGroup);
        if (SystemProperties.setPermissionsToTemporaryGroups()) {
            Set<Executor> grantedExecutors = Sets.newHashSet();
            grantedExecutors.addAll(newGroupExecutors);
            for (Executor executor : newGroupExecutors) {
                grantedExecutors.addAll(permissionDao.getExecutorsWithPermission(executor));
            }
            for (Executor executor : grantedExecutors) {
                permissionDao.setPermissions(executor, Lists.newArrayList(Permission.READ), temporaryGroup);
            }
        }
    }

    private String buildTemporaryExecutorsExclusionQuery() {
        String temporaryGroupDiscriminatorValue = TemporaryGroup.class.getAnnotation(DiscriminatorValue.class).value();
        String delegationGroupDiscriminatorValue = DelegationGroup.class.getAnnotation(DiscriminatorValue.class).value();
        String escalationGroupDiscriminatorValue = EscalationGroup.class.getAnnotation(DiscriminatorValue.class).value();
        String restrictionQuery = ClassPresentation.classNameSQL + ".class not in ('" + temporaryGroupDiscriminatorValue + "', '"
                + delegationGroupDiscriminatorValue + "', '" + escalationGroupDiscriminatorValue + "')";
        return restrictionQuery;
    }
}
