package ru.runa.wfe.user.dao;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.ExecutorLoader;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.QActor;
import ru.runa.wfe.user.QExecutor;
import ru.runa.wfe.user.QExecutorGroupMembership;
import ru.runa.wfe.user.QTemporaryGroup;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.cache.ExecutorCacheCtrl;

/**
 * DAO for managing executors.
 * 
 * @since 2.0
 */
@Component
@SuppressWarnings("unchecked")
public class ExecutorDao extends CommonDao implements ExecutorLoader {
    private static final String ID_PROPERTY_NAME = "id";
    private static final String CODE_PROPERTY_NAME = "code";

    @Autowired
    private ExecutorCacheCtrl executorCacheCtrl;

    /**
     * Check if executor with given name exists.
     * 
     * @param executorName
     *            Executor name to check.
     * @return Returns true, if executor with given name exists; false otherwise.
     */
    public boolean isExecutorExist(String executorName) {
        return getExecutorByName(Executor.class, executorName) != null;
    }

    /**
     * Load {@linkplain Executor} by name. Throws exception if load is impossible.
     * 
     * @param name
     *            Loaded executor name.
     * @return Executor with specified name.
     */
    @Override
    public Executor getExecutor(String name) {
        return getExecutor(Executor.class, name);
    }

    /**
     * Load {@linkplain Executor} by identity. Throws exception if load is impossible.
     * 
     * @param id
     *            Loaded executor identity.
     * @return {@linkplain Executor} with specified identity.
     */
    @Override
    public Executor getExecutor(Long id) {
        return getExecutor(Executor.class, id);
    }

    public Actor getActor(String name) {
        return getExecutor(Actor.class, name);
    }

    /**
     * Load {@linkplain Actor} by name without case check. This method is a big shame for us. It should never have its way out of DAO! It only purpose
     * is to use with stupid Microsoft Active Directory authentication, which is case insensitive. <b>Never use it! </b>
     * 
     * @param name
     *            Loaded actor name.
     * @return {@linkplain Actor} with specified name (case insensitive).
     */
    public Actor getActorCaseInsensitive(final String name) {
        val a = QActor.actor;
        Actor actor = queryFactory.selectFrom(a).where(a.name.likeIgnoreCase(name)).fetchFirst();
        return checkExecutorNotNull(actor, name, Actor.class);
    }

    public Actor getActor(Long id) {
        return getExecutor(Actor.class, id);
    }

    /**
     * Load {@linkplain Actor} by code. Throws exception if load is impossible.
     * 
     * @param code
     *            Loaded actor code.
     * @return {@linkplain Actor} with specified code.
     */
    @Override
    public Actor getActorByCode(Long code) {
        Actor actor = getActorByCodeInternal(code);
        return checkExecutorNotNull(actor, "with code " + code, Actor.class);
    }

    /**
     * Load {@linkplain Group} by name. Throws exception if load is impossible, or exist actor with same name.
     * 
     * @param name
     *            Loaded group name.
     * @return {@linkplain Group} with specified name.
     */
    public Group getGroup(String name) {
        return getExecutor(Group.class, name);
    }

    /**
     * Load {@linkplain Group} by identity. Throws exception if load is impossible, or exist actor with same identity.
     * 
     * @param id
     *            Loaded group identity.
     * @return {@linkplain Group} with specified identity.
     */
    public Group getGroup(Long id) {
        return getExecutor(Group.class, id);
    }

    /**
     * Load {@linkplain Executor}'s with given identities.
     * 
     * @param ids
     *            Loading {@linkplain Executor}'s identities.
     * @return Loaded executors in same order, as identities.
     */
    public List<Executor> getExecutors(List<Long> ids) {
        return getExecutors(Executor.class, ids, false);
    }

    /**
     * Load {@linkplain Actor}'s with given identities.
     * 
     * @param ids
     *            Loading {@linkplain Actor}'s identities.
     * @return Loaded actors in same order, as identities.
     */
    public List<Actor> getActors(List<Long> ids) {
        return getExecutors(Actor.class, ids, false);
    }

    /**
     * Load {@linkplain Actor}'s with given codes.
     * 
     * @param codes
     *            Loading {@linkplain Actor}'s codes.
     * @return Loaded actors in same order, as codes.
     */
    public List<Actor> getActorsByCodes(List<Long> codes) {
        return getExecutors(Actor.class, codes, true);
    }

    /**
     * Returns identities of {@linkplain Actor} and all his groups recursively. Actor identity is always result[0], but groups identities order is not
     * specified. </br> For example G1 contains A1 and G2 contains G1. In this case:</br>
     * <code>getActorAndGroupsIds(A1) == {A1.id, G1.id, G2.id}.</code>
     * 
     * @param actor
     *            {@linkplain Actor}, which identity and groups must be loaded.
     * @return Returns identities of {@linkplain Actor} and all his groups recursively.
     */
    public List<Long> getActorAndNotTemporaryGroupsIds(Actor actor) {
        Set<Group> groupSet = getExecutorParentsAll(actor);
        List<Long> ids = Lists.newArrayListWithExpectedSize(groupSet.size() + 1);
        ids.add(actor.getId());
        for (Group group : groupSet) {
            ids.add(group.getId());
        }
        return ids;
    }

    /**
     * Load {@linkplain Group}'s with given identities.
     * 
     * @param ids
     *            Loading {@linkplain Group}'s identities.
     * @return Loaded groups in same order, as identities.
     */
    public List<Group> getGroups(List<Long> ids) {
        return getExecutors(Group.class, ids, false);
    }

    public List<TemporaryGroup> getTemporaryGroups(final Long processId) {
        val tg = QTemporaryGroup.temporaryGroup;
        return queryFactory.selectFrom(tg).where(tg.description.eq(processId.toString())).fetch();
    }

    public List<Group> getExecutorsTemporaryGroups(Collection<? extends Executor> executors) {
        if (executors.size() == 0) {
            return new ArrayList<>();
        }
        QExecutorGroupMembership egm = QExecutorGroupMembership.executorGroupMembership;
        QTemporaryGroup tg = QTemporaryGroup.temporaryGroup;
        return queryFactory.select(egm.group).from(egm, tg).where(egm.executor.in(executors).and(egm.group.id.eq(tg.id))).fetch();
    }

    /**
     * Create executor (save it to database). Generate code property for {@linkplain Actor} with code == 0.
     * 
     * @param <T>
     *            Creating executor class.
     * @param executor
     *            Creating executor.
     * @return Returns created executor.
     */
    public <T extends Executor> T create(T executor) {
        if (isExecutorExist(executor.getName())) {
            throw new ExecutorAlreadyExistsException(executor.getName());
        }
        if (executor instanceof Actor) {
            Actor actor = (Actor) executor;
            if (actor.getCode() == null) {
                QActor a = QActor.actor;
                Long minCode = queryFactory.select(a.code.min()).from(a).fetchFirst();
                actor.setCode(minCode == null ? -1 : minCode - 1);
            } else if (getActorByCodeInternal(actor.getCode()) != null) {
                throw new ExecutorAlreadyExistsException(actor.getCode());
            }
        }
        sessionFactory.getCurrentSession().save(executor);
        return executor;
    }

    /**
     * Updates password for {@linkplain Actor}.
     * 
     * @param actor
     *            {@linkplain Actor} to update password.
     * @param password
     *            New actor password.
     */
    public void setPassword(Actor actor, String password) {
        ActorPassword actorPassword = getActorPassword(actor);
        if (actorPassword == null) {
            if (!Strings.isNullOrEmpty(password)) {
                actorPassword = new ActorPassword(actor, password);
                sessionFactory.getCurrentSession().save(actorPassword);
            }
        } else {
            if (!Strings.isNullOrEmpty(password)) {
                actorPassword.setPassword(password);
                sessionFactory.getCurrentSession().merge(actorPassword);
            } else {
                sessionFactory.getCurrentSession().delete(actorPassword);
            }
        }
    }

    /**
     * Check if password is valid for user.
     * 
     * @param actor
     *            {@linkplain Actor}, which password is checking.
     * @param password
     *            Checking password.
     * @return Returns true, if password is correct for actor and false otherwise.
     */
    public boolean isPasswordValid(Actor actor, String password) {
        Preconditions.checkNotNull(password, "Password must be specified.");
        ActorPassword actorPassword = new ActorPassword(actor, password);
        ActorPassword result = getActorPassword(actor);
        return actorPassword.equals(result);
    }

    /**
     * Set {@linkplain Actor} active state.
     * 
     * @param actor
     *            {@linkplain Actor}, which active state is set.
     * @param isActive
     *            Flag, equals true to set actor active and false, to set actor inactive.
     */
    public Actor setStatus(Actor actor, boolean isActive) {
        actor.setActive(isActive);
        return (Actor) sessionFactory.getCurrentSession().merge(actor);
    }

    /**
     * Update executor.
     * 
     * @param <T>
     *            Updated executor class.
     * @param newExecutor
     *            Updated executor new state.
     * @return Returns updated executor state after update.
     */
    public <T extends Executor> T update(T newExecutor) {
        T oldExecutor = (T) getExecutor(newExecutor.getId());
        if (!Objects.equal(oldExecutor.getName(), newExecutor.getName())) {
            Executor testExecutor = getExecutorByName(Executor.class, newExecutor.getName());
            if (testExecutor != null && !Objects.equal(testExecutor.getId(), newExecutor.getId())) {
                throw new ExecutorAlreadyExistsException(newExecutor.getName());
            }
        }
        if (newExecutor instanceof Actor) {
            Actor newActor = (Actor) newExecutor;
            if (!Objects.equal(((Actor) oldExecutor).getCode(), newActor.getCode())) {
                Actor testActor = getActorByCodeInternal(newActor.getCode());
                if (testActor != null && !Objects.equal(testActor.getId(), newActor.getId())) {
                    throw new ExecutorAlreadyExistsException(newActor.getCode());
                }
            }
        }
        return (T) sessionFactory.getCurrentSession().merge(newExecutor);
    }

    /**
     * Load all {@linkplain Actor}s according to {@linkplain BatchPresentation} .</br> <b>Paging is not enabled. Really ALL actors is loading.</b>
     *
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load actors.
     * @return {@linkplain Actor}s, loaded according to {@linkplain BatchPresentation}.
     */
    public List<Actor> getAllActors(BatchPresentation batchPresentation) {
        return getAll(Actor.class, batchPresentation);
    }

    /**
     * @param pageIndex
     *            page number (first page = 0)
     * @param pageSize
     *            number of items for a page
     */
    public List<Actor> getActorsForChatNotificationsWithPagination(int pageIndex, int pageSize) {
        QActor a = QActor.actor;
        return queryFactory.selectFrom(a)
                .where(a.active.isTrue().and(a.email.isNotNull().and(a.email.isNotEmpty().and(a.chatEmailNotificationsEnabled.isTrue()))))
                .orderBy(a.id.asc())
                .offset(pageIndex * pageSize).limit(pageSize).fetch();
    }

    /**
     * Load all {@linkplain Group}s.</br> <b>Paging is not enabled. Really ALL groups is loading.</b>
     * 
     * @return {@linkplain Group}s.
     */
    public List<Group> getAllGroups() {
        BatchPresentation batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        return getAll(Group.class, batchPresentation);
    }

    /**
     * Add {@linkplain Executor}'s to {@linkplain Group}.
     * 
     * @param executors
     *            {@linkplain Executor}'s, added to {@linkplain Group}.
     * @param group
     *            {@linkplain Group}, to add executors in.
     */
    public void addExecutorsToGroup(Collection<? extends Executor> executors, Group group) {
        for (Executor executor : executors) {
            addExecutorToGroup(executor, group);
        }
    }

    /**
     * Add {@linkplain Executor} to {@linkplain Group}'s.
     * 
     * @param executor
     *            {@linkplain Executor}, added to {@linkplain Group}s.
     * @param groups
     *            {@linkplain Group}s, to add executors in.
     */
    public void addExecutorToGroups(Executor executor, List<Group> groups) {
        for (Group group : groups) {
            addExecutorToGroup(executor, group);
        }
    }

    /**
     * Add {@linkplain Executor} to {@linkplain Group}
     */
    public boolean addExecutorToGroup(Executor executor, Group group) {
        if (getMembership(group, executor) == null) {
            if (executor instanceof TemporaryGroup) {
                throw new InternalApplicationException("TemporaryGroup cannot be nested");
            }
            sessionFactory.getCurrentSession().save(new ExecutorGroupMembership(group, executor));
            return true;
        }
        return false;
    }

    /**
     * Remove {@linkplain Executor}'s from {@linkplain Group}.
     * 
     * @param executors
     *            {@linkplain Executor}'s, removed from {@linkplain Group}.
     * @param group
     *            {@linkplain Group}, to remove executors from.
     */
    public void removeExecutorsFromGroup(Collection<? extends Executor> executors, Group group) {
        for (Executor executor : executors) {
            removeExecutorFromGroup(executor, group);
        }
    }

    /**
     * Remove {@linkplain Executor} from {@linkplain Group}'s.
     * 
     * @param executor
     *            {@linkplain Executor}, removed from {@linkplain Group}s.
     * @param groups
     *            {@linkplain Group}s, to remove executors from.
     */
    public void removeExecutorFromGroups(Executor executor, List<Group> groups) {
        for (Group group : groups) {
            removeExecutorFromGroup(executor, group);
        }
    }

    /**
     * Remove {@linkplain Executor} from {@linkplain Group}.
     */
    public boolean removeExecutorFromGroup(Executor executor, Group group) {
        ExecutorGroupMembership membership = getMembership(group, executor);
        if (membership != null) {
            sessionFactory.getCurrentSession().delete(membership);
            return true;
        }
        return false;
    }

    /**
     * Returns true if executor belongs to group recursively or false in any other case.</br> For example G1 contains G2, G2 contains A1. In this
     * case:</br> <code>isExecutorInGroup(A1,G2) == true;</code>
     * 
     * @param executor
     *            An executor to check if it in group.
     * @param group
     *            A group to check if it contains executor.
     * @return true if executor belongs to group recursively; false in any other case.
     */
    public boolean isExecutorInGroup(Executor executor, Group group) {
        if (group instanceof TemporaryGroup) {
            return getMembership(group, executor) != null;
        }
        return getExecutorParentsAll(executor).contains(group);
    }

    /**
     * Returns group children (first level children, not recursively).</br> For example G1 contains G2, G2 contains A1 and A2. In this case:</br>
     * <code> getGroupChildren(G2) == {A1, A2}</code><br/>
     * <code> getGroupChildren(G1) == {G2} </code>
     * 
     * @param group
     *            A group to load children's from.
     * @return Array of group children.
     */
    public Set<Executor> getGroupChildren(Group group) {
        Set<Executor> result = executorCacheCtrl.getGroupMembers(group);
        if (result != null) {
            return result;
        }
        result = new HashSet<>();
        for (ExecutorGroupMembership relation : getGroupMemberships(group)) {
            result.add(relation.getExecutor());
        }
        return result;
    }

    private List<ExecutorGroupMembership> getGroupMemberships(Group group) {
        val egm = QExecutorGroupMembership.executorGroupMembership;
        return queryFactory.selectFrom(egm).where(egm.group.eq(group)).fetch();
    }

    private ExecutorGroupMembership getMembership(Group group, Executor executor) {
        val egm = QExecutorGroupMembership.executorGroupMembership;
        return queryFactory.selectFrom(egm).where(egm.group.eq(group).and(egm.executor.eq(executor))).fetchFirst();
    }

    public Set<Actor> getGroupActors(Group group) {
        return getGroupActors(group, new HashSet<>());
    }

    public Set<Group> getExecutorParentsAll(Executor executor) {
        return getExecutorGroupsAll(executor, new HashSet<>());
    }

    /**
     * Returns an array of actors from group (first level children, not recursively).</br> For example G1 contains G2 and A0, G2 contains A1 and A2.
     * In this case: Only actor (non-group) executors are returned.</br> <code> getAllNonGroupExecutorsFromGroup(G2) returns {A1, A2}</code>;
     * <code> getAllNonGroupExecutorsFromGroup(G1) returns {A0} </code>
     * 
     * @param group
     *            {@linkplain Group}, to load actor children's.
     * @return Array of executors from group.
     */
    public List<Executor> getAllNonGroupExecutorsFromGroup(Group group) {
        Set<Executor> childrenSet = getGroupChildren(group);
        List<Executor> retVal = new ArrayList<>();
        for (Executor executor : childrenSet) {
            if (!(executor instanceof Group)) {
                retVal.add(executor);
            }
        }
        return retVal;
    }

    public void remove(Executor executor) {
        Assert.notNull(executor.getId());
        val egm = QExecutorGroupMembership.executorGroupMembership;
        val ap = QActorPassword.actorPassword;
        queryFactory.delete(egm).where(egm.executor.eq(executor)).execute();
        if (executor instanceof Group) {
            queryFactory.delete(egm).where(egm.group.eq((Group)executor)).execute();
        } else {
            queryFactory.delete(ap).where(ap.actorId.eq(executor.getId())).execute();
        }
        // #266#note-18
        executor = sessionFactory.getCurrentSession().get(Executor.class, executor.getId());
        sessionFactory.getCurrentSession().delete(executor);
    }

    public boolean hasPassword(Actor actor) {
        ActorPassword actorPassword = getActorPassword(actor);
        return actorPassword != null && actorPassword.getPassword().length > 0;
    }

    public long getAllActorsCount() {
        QActor a = QActor.actor;
        return queryFactory.selectFrom(a)
                .fetchCount();
    }

    private <T extends Executor> List<T> getAll(Class<T> clazz, BatchPresentation batchPresentation) {
        VersionedCacheData<List<T>> cached = executorCacheCtrl.getAllExecutor(clazz, batchPresentation);
        if (cached != null && cached.getData() != null) {
            return cached.getData();
        }
        CompilerParameters parameters = CompilerParameters.createNonPaged().addRequestedClass(clazz);
        List<T> result = new PresentationCompiler<T>(batchPresentation).getBatch(parameters);
        executorCacheCtrl.addAllExecutor(cached, clazz, batchPresentation, result);
        return result;
    }

    private Set<Actor> getGroupActors(Group group, Set<Group> visited) {
        Set<Actor> result = executorCacheCtrl.getGroupActorsAll(group);
        if (result != null) {
            return result;
        }
        result = new HashSet<>();
        if (visited.contains(group)) {
            return result;
        }
        visited.add(group);
        for (Executor executor : getGroupChildren(group)) {
            if (executor instanceof Group) {
                result.addAll(getGroupActors((Group) executor, visited));
            } else {
                result.add((Actor) executor);
            }
        }
        return result;
    }

    private Set<Group> getExecutorGroups(Executor executor) {
        Set<Group> result = executorCacheCtrl.getExecutorParents(executor);
        if (result != null) {
            return result;
        }
        result = new HashSet<>();
        SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(
                "SELECT * FROM " + ApplicationContextFactory.getSchemaPrefix() + "EXECUTOR_GROUP_MEMBER WHERE GROUP_ID IN (SELECT ID FROM "
                        + ApplicationContextFactory.getSchemaPrefix() + "EXECUTOR WHERE DISCRIMINATOR IN ('Y', 'N')) AND EXECUTOR_ID = :executorId")
                .addEntity(ExecutorGroupMembership.class);
        sqlQuery.setParameter("executorId", executor.getId());
        List<ExecutorGroupMembership> memberships = sqlQuery.list();
        for (ExecutorGroupMembership membership : memberships) {
            result.add(membership.getGroup());
        }
        return result;
    }

    private Set<Group> getExecutorGroupsAll(Executor executor, Set<Executor> visited) {
        Set<Group> cached = executorCacheCtrl.getExecutorParentsAll(executor);
        if (cached != null) {
            return cached;
        }
        if (visited.contains(executor)) {
            return new HashSet<>();
        }
        visited.add(executor);
        Set<Group> result = new HashSet<>();
        for (Group group : getExecutorGroups(executor)) {
            result.add(group);
            result.addAll(getExecutorGroupsAll(group, visited));
        }
        return result;
    }

    /**
     * Loads executors by id or code (for {@link Actor}).
     * 
     * @param clazz
     *            Loaded executors class.
     * @param identifiers
     *            Loaded executors identities or codes.
     * @param loadByCodes
     *            Flag, equals true, to loading actors by codes; false to load executors by identity.
     * @return Loaded executors.
     */
    private <T extends Executor> List<T> getExecutors(final Class<T> clazz, final List<Long> identifiers, boolean loadByCodes) {
        final String propertyName = loadByCodes ? CODE_PROPERTY_NAME : ID_PROPERTY_NAME;
        List<T> executors = getExecutorsFromCache(clazz, identifiers, loadByCodes);
        if (executors != null) {
            return executors;
        }
        List<T> list = sessionFactory.getCurrentSession().createQuery("from " + clazz.getName() + " where " + propertyName + " in (:ids)")
                .setParameterList("ids", identifiers)
                .list();
        HashMap<Long, Executor> idExecutorMap = Maps.newHashMapWithExpectedSize(list.size());
        for (Executor executor : list) {
            idExecutorMap.put(loadByCodes ? ((Actor) executor).getCode() : executor.getId(), executor);
        }
        executors = Lists.newArrayListWithExpectedSize(identifiers.size());
        for (Long id : identifiers) {
            Executor executor = idExecutorMap.get(id);
            if (executor == null) {
                throw new ExecutorDoesNotExistException("with identifier " + id + " for property " + propertyName, clazz);
            }
            executors.add((T) executor);
        }
        return executors;
    }

    /**
     * Loads executors by id or code (for {@link Actor}) from caches.
     * 
     * @param clazz
     *            Loaded executors class.
     * @param identifiers
     *            Loaded executors identities or codes.
     * @param loadByCodes
     *            Flag, equals true, to loading actors by codes; false to load executors by identity.
     * @return Loaded executors or null, if executors couldn't load from cache.
     */
    private <T extends Executor> List<T> getExecutorsFromCache(Class<T> clazz, List<Long> identifiers, boolean loadByCodes) {
        List<T> executors = Lists.newArrayListWithExpectedSize(identifiers.size());
        for (Long id : identifiers) {
            Preconditions.checkArgument(id != null, "id == null");
            Executor ex = !loadByCodes ? executorCacheCtrl.getExecutor(id) : executorCacheCtrl.getActor(id);
            if (ex == null) {
                return null;
            }
            if (!clazz.isAssignableFrom(ex.getClass())) {
                String propertyName = loadByCodes ? CODE_PROPERTY_NAME : ID_PROPERTY_NAME;
                throw new ExecutorDoesNotExistException("with identifier " + id + " for property " + propertyName, clazz);
            }
            executors.add((T) ex);
        }
        return executors;
    }

    private <T extends Executor> T getExecutorById(Class<T> clazz, Long id) {
        Executor executor = executorCacheCtrl.getExecutor(id);
        if (executor != null) {
            return clazz.isAssignableFrom(executor.getClass()) ? (T) executor : null;
        } else {
            return sessionFactory.getCurrentSession().get(clazz, id);
        }
    }

    private <T extends Executor> T getExecutorByName(Class<T> clazz, String name) {
        Executor executor = executorCacheCtrl.getExecutor(name);
        if (executor != null) {
            return (T) (clazz.isAssignableFrom(executor.getClass()) ? executor : null);
        } else {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("name", name);
            return findFirstOrNull("from " + clazz.getName() + " where name=:name", parameters);
        }
    }

    private <T extends Executor> T getExecutor(Class<T> clazz, Long id) {
        return checkExecutorNotNull(getExecutorById(clazz, id), id, clazz);
    }

    private <T extends Executor> T getExecutor(Class<T> clazz, String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new NullPointerException("Executor name must be specified");
        }
        return checkExecutorNotNull(getExecutorByName(clazz, name), name, clazz);
    }

    private ActorPassword getActorPassword(Actor actor) {
        val ap = QActorPassword.actorPassword;
        return queryFactory.selectFrom(ap).where(ap.actorId.eq(actor.getId())).fetchFirst();
    }

    private Actor getActorByCodeInternal(Long code) {
        Actor actor = executorCacheCtrl.getActor(code);
        if (actor != null) {
            return actor;
        }
        val a = QActor.actor;
        return queryFactory.selectFrom(a).where(a.code.eq(code)).fetchFirst();
    }

    private <T extends Executor> T checkExecutorNotNull(T executor, Long id, Class<T> clazz) {
        if (executor == null) {
            throw new ExecutorDoesNotExistException(id, clazz);
        }
        return executor;
    }

    private <T extends Executor> T checkExecutorNotNull(T executor, String name, Class<T> clazz) {
        if (executor == null) {
            throw new ExecutorDoesNotExistException(name, clazz);
        }
        return executor;
    }

    public List<Executor> getExecutorsLikeName(String nameTemplate) {
        val e = QExecutor.executor;
        return queryFactory.selectFrom(e).where(e.name.like(nameTemplate)).fetch();
    }

    public boolean isAdministrator(Actor actor) {
        try {
            Group administratorsGroup = (Group) getExecutor(SystemProperties.getAdministratorsGroupName());
            return isExecutorInGroup(actor, administratorsGroup);
        } catch (ExecutorDoesNotExistException e) {
            logger.debug(e);
            return false;
        }
    }

}
