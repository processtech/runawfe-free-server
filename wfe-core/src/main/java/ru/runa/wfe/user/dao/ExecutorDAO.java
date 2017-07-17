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
package ru.runa.wfe.user.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.dao.CommonDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.cache.ExecutorCache;

/**
 * DAO for managing executors.
 * 
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class ExecutorDAO extends CommonDAO implements IExecutorDAO {
    private static final String NAME_PROPERTY_NAME = "name";
    private static final String ID_PROPERTY_NAME = "id";
    private static final String CODE_PROPERTY_NAME = "code";

    @Autowired
    private ExecutorCache executorCacheCtrl;

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
     * Check if {@linkplain Actor} with given code exists.
     * 
     * @param code
     *            {@linkplain Actor} code to check.
     * @return Returns true, if {@linkplain Actor} with given name exists; false otherwise.
     */
    public boolean isActorExist(Long code) {
        return getActorByCodeInternal(code) != null;
    }

    public boolean isActorExist(Long code, boolean cacheVerify) {
        if (cacheVerify) {
            return getActorByCodeInternal(code) != null;
        }
        return getActorByCodeInternalWithoutCacheVerify(code) != null;
    }

    private Actor getActorByCodeInternalWithoutCacheVerify(Long code) {
        return findFirstOrNull("from Actor where code=?", code);
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
     * @param name
     *            Loaded executor identity.
     * @return {@linkplain Executor} with specified identity.
     */
    @Override
    public Executor getExecutor(Long id) {
        return getExecutor(Executor.class, id);
    }

    @Override
    public Actor getActor(String name) {
        return getExecutor(Actor.class, name);
    }

    public List<Actor> getActors() {
        return getHibernateTemplate().find("from Actor a");
    }

    public List<Group> getGroups() {
        return getHibernateTemplate().find("from Group g");
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
        return getHibernateTemplate().execute(new HibernateCallback<Actor>() {

            @Override
            public Actor doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(Actor.class);
                criteria.add(Restrictions.ilike(NAME_PROPERTY_NAME, name, MatchMode.EXACT));
                Actor actor = (Actor) getFirstOrNull(criteria.list());
                return checkExecutorNotNull(actor, name, Actor.class);
            }
        });
    }

    @Override
    public Actor getActor(Long id) {
        return getExecutor(Actor.class, id);
    }

    /**
     * Load {@linkplain Actor} by code. Throws exception if load is impossible.
     * 
     * @param name
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
    @Override
    public Group getGroup(String name) {
        return getExecutor(Group.class, name);
    }

    /**
     * Load {@linkplain Group} by identity. Throws exception if load is impossible, or exist actor with same identity.
     * 
     * @param name
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
     * @param executorIds
     *            Loading {@linkplain Actor}'s identities.
     * @return Loaded actors in same order, as identities.
     */
    public List<Actor> getActors(List<Long> ids) {
        return getExecutors(Actor.class, ids, false);
    }

    /**
     * Returns Actors by array of executor identities. If id element belongs to group it is replaced by all actors in group recursively.
     * 
     * @param ids
     *            Executors identities, to load actors.
     * @return Loaded actors, belongs to executor identities.
     */
    public List<Actor> getActorsByExecutorIds(List<Long> executorIds) {
        Set<Actor> actorSet = new HashSet<Actor>();
        for (Executor executor : getExecutors(executorIds)) {
            if (executor instanceof Actor) {
                actorSet.add((Actor) executor);
            } else {
                actorSet.addAll(getGroupActors((Group) executor));
            }
        }
        return Lists.newArrayList(actorSet);
    }

    /**
     * Load {@linkplain Actor}'s with given codes.
     * 
     * @param executorIds
     *            Loading {@linkplain Actor}'s codes.
     * @return Loaded actors in same order, as codes.
     */
    public List<Actor> getActorsByCodes(List<Long> codes) {
        return getExecutors(Actor.class, codes, true);
    }

    /**
     * Returns identities of {@linkplain Actor} and all his groups recursively. Actor identity is always result[0], but groups identities order is not
     * specified. </br>
     * For example G1 contains A1 and G2 contains G1. In this case:</br>
     * <code>getActorAndGroupsIds(A1) == {A1.id, G1.id, G2.id}.</code>
     * 
     * @param actor
     *            {@linkplain Actor}, which identity and groups must be loaded.
     * @return Returns identities of {@linkplain Actor} and all his groups recursively.
     */
    public List<Long> getActorAndNotTemporaryGroupsIds(Actor actor) {
        Set<Group> groupSet = getExecutorParentsAll(actor, false);
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
     * @param executorIds
     *            Loading {@linkplain Group}'s identities.
     * @return Loaded groups in same order, as identities.
     */
    public List<Group> getGroups(List<Long> ids) {
        return getExecutors(Group.class, ids, false);
    }

    public List<TemporaryGroup> getTemporaryGroups(final Long processId) {
        return getHibernateTemplate().executeFind(new HibernateCallback<List<TemporaryGroup>>() {

            @Override
            public List<TemporaryGroup> doInHibernate(Session session) {
                Query query = session.createQuery("from TemporaryGroup where description=:processIdString");
                query.setParameter("processIdString", processId.toString());
                return query.list();
            }
        });
    }

    public List<TemporaryGroup> getUnusedTemporaryGroups() {
        String query = "select tg from TemporaryGroup tg where tg.processId not in (select process.id from Swimlane where executor=tg) and tg.processId not in (select process.id from Task where executor=tg)";
        return getHibernateTemplate().find(query);
    }

    public List<Group> getTemporaryGroupsByExecutor(Executor executor) {
        String query = "select egm.group from ExecutorGroupMembership egm, TemporaryGroup tg where egm.executor=? and egm.group=tg";
        return getHibernateTemplate().find(query, executor);
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
            checkActorCode((Actor) executor);
        }
        getHibernateTemplate().save(executor);
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
        Preconditions.checkNotNull(password, "Password must be specified.");
        ActorPassword actorPassword = getActorPassword(actor);
        if (actorPassword == null) {
            actorPassword = new ActorPassword(actor, password);
            getHibernateTemplate().save(actorPassword);
        } else {
            actorPassword.setPassword(password);
            getHibernateTemplate().merge(actorPassword);
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
        return getHibernateTemplate().merge(actor);
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
        return getHibernateTemplate().merge(newExecutor);
    }

    /**
     * Remove children's from group.
     * 
     * @param group
     *            Group from which removal will be executed.
     * @param executors
     *            List of executors which will be deleted from group
     */
    public void deleteExecutorsFromGroup(Group group, Collection<? extends Executor> executors) {
        List<ExecutorGroupMembership> list = Lists.newArrayList();
        for (Executor executor : executors) {
            list.add(getMembership(group, executor));
        }
        getHibernateTemplate().deleteAll(list);
    }

    /**
     * Load all {@linkplain Executor}s according to {@linkplain BatchPresentation}.</br>
     * <b>Paging is not enabled. Really ALL executors is loading.</b>
     * 
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load executors.
     * @return {@linkplain Executor}s, loaded according to {@linkplain BatchPresentation}.
     */
    public List<Executor> getAllExecutors(BatchPresentation batchPresentation) {
        return getAll(Executor.class, batchPresentation);
    }

    /**
     * Load all {@linkplain Actor}s according to {@linkplain BatchPresentation} .</br>
     * <b>Paging is not enabled. Really ALL actors is loading.</b>
     * 
     * @param batchPresentation
     *            {@linkplain BatchPresentation} to load actors.
     * @return {@linkplain Actor}s, loaded according to {@linkplain BatchPresentation}.
     */
    public List<Actor> getAllActors(BatchPresentation batchPresentation) {
        return getAll(Actor.class, batchPresentation);
    }

    /**
     * Load all {@linkplain Group}s.</br>
     * <b>Paging is not enabled. Really ALL groups is loading.</b>
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
     * @param executors
     *            {@linkplain Executor}, added to {@linkplain Group}'s.
     * @param group
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
            getHibernateTemplate().save(new ExecutorGroupMembership(group, executor));
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
     * @param executors
     *            {@linkplain Executor}, removed from {@linkplain Group}'s.
     * @param group
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
            getHibernateTemplate().delete(membership);
            return true;
        }
        return false;
    }

    /**
     * Returns true if executor belongs to group recursively or false in any other case.</br>
     * For example G1 contains G2, G2 contains A1. In this case:</br>
     * <code>isExecutorInGroup(A1,G2) == true;</code>
     * 
     * @param executor
     *            An executor to check if it in group.
     * @param group
     *            A group to check if it contains executor.
     * @return true if executor belongs to group recursively; false in any other case.
     */
    public boolean isExecutorInGroup(Executor executor, Group group) {
        return getExecutorParentsAll(executor, true).contains(group);
    }

    /**
     * Returns group children (first level children, not recursively).</br>
     * For example G1 contains G2, G2 contains A1 and A2. In this case:</br>
     * <code> getGroupChildren(G2) == {A1, A2}</code><br/>
     * <code> getGroupChildren(G1) == {G2} </code>
     * 
     * @param group
     *            A group to load children's from.
     * @param batchPresentation
     *            As {@linkplain BatchPresentation} of array returned.
     * @return Array of group children.
     */
    @Override
    public Set<Executor> getGroupChildren(Group group) {
        Set<Executor> result = executorCacheCtrl.getGroupMembers(group);
        if (result != null) {
            return result;
        }
        result = new HashSet<Executor>();
        for (ExecutorGroupMembership relation : getGroupMemberships(group)) {
            result.add(relation.getExecutor());
        }
        return result;
    }

    private List<ExecutorGroupMembership> getGroupMemberships(Group group) {
        return getHibernateTemplate().find("from ExecutorGroupMembership where group=?", group);
    }

    private List<ExecutorGroupMembership> getExecutorMemberships(Executor executor) {
        return getHibernateTemplate().find("from ExecutorGroupMembership where executor=?", executor);
    }

    private ExecutorGroupMembership getMembership(Group group, Executor executor) {
        return findFirstOrNull("from ExecutorGroupMembership where group=? and executor=?", group, executor);
    }

    @Override
    public Set<Actor> getGroupActors(Group group) {
        Set<Actor> result = executorCacheCtrl.getGroupActorsAll(group);
        if (result == null) {
            result = getGroupActors(group, new HashSet<Group>());
        }
        return result;
    }

    @Override
    public Set<Group> getExecutorParentsAll(Executor executor, boolean includeTemporaryGroups) {
        Set<Group> executorGroupsAll = getExecutorGroupsAll(executor, new HashSet<Executor>(), includeTemporaryGroups);
        if (!includeTemporaryGroups) {
            Set<Group> withoutTemporary = Sets.newHashSet();
            for (Group group : executorGroupsAll) {
                if (!group.isTemporary()) {
                    withoutTemporary.add(group);
                }
            }
            return withoutTemporary;
        } else {
            return executorGroupsAll;
        }
    }

    /**
     * @return direct executor parent {@linkplain Groups}s skipping cache.
     */
    public Set<Group> getExecutorParents(Executor executor) {
        HashSet<Group> result = new HashSet<Group>();
        for (ExecutorGroupMembership membership : getExecutorMemberships(executor)) {
            result.add(membership.getGroup());
        }
        return result;
    }

    /**
     * Returns an array of actors from group (first level children, not recursively).</br>
     * For example G1 contains G2 and A0, G2 contains A1 and A2. In this case: Only actor (non-group) executors are returned.</br>
     * <code> getAllNonGroupExecutorsFromGroup(G2) returns {A1, A2}</code>; <code> getAllNonGroupExecutorsFromGroup(G1) returns {A0} </code>
     * 
     * @param group
     *            {@linkplain Group}, to load actor children's.
     * @return Array of executors from group.
     */
    public List<Executor> getAllNonGroupExecutorsFromGroup(Group group) {
        Set<Executor> childrenSet = getGroupChildren(group);
        List<Executor> retVal = new ArrayList<Executor>();
        for (Executor executor : childrenSet) {
            if (!(executor instanceof Group)) {
                retVal.add(executor);
            }
        }
        return retVal;
    }

    public void remove(Executor executor) {
        getHibernateTemplate().deleteAll(getExecutorMemberships(executor));
        if (executor instanceof Group) {
            getHibernateTemplate().deleteAll(getGroupMemberships((Group) executor));
        } else {
            ActorPassword actorPassword = getActorPassword((Actor) executor);
            if (actorPassword != null) {
                getHibernateTemplate().delete(actorPassword);
            }
        }
        // TODO avoid DuplicateKeyException
        executor = getHibernateTemplate().get(executor.getClass(), executor.getId());
        getHibernateTemplate().delete(executor);
    }

    /**
     * Generates code for actor, if code not set (equals 0). If code is already set, when throws {@linkplain ExecutorAlreadyExistsException} if
     * executor with what code exists in database.
     * 
     * @param actor
     *            Actor to generate code if not set.
     */
    private void checkActorCode(Actor actor) {
        checkActorCode(actor, true);
    }

    private void checkActorCode(Actor actor, boolean cacheVerify) {
        if (actor.getCode() == null) {
            Long nextCode = getHibernateTemplate().execute(new HibernateCallback<Long>() {
                @Override
                public Long doInHibernate(Session session) {
                    Criteria criteria = session.createCriteria(Actor.class);
                    criteria.setMaxResults(1);
                    criteria.addOrder(Order.asc(CODE_PROPERTY_NAME));
                    List<Actor> actors = criteria.list();
                    if (actors.size() > 0) {
                        return Long.valueOf(actors.get(0).getCode().longValue() - 1);
                    }
                    return -1L;
                }
            });
            actor.setCode(nextCode);
        }
        if (isActorExist(actor.getCode(), cacheVerify)) {
            throw new ExecutorAlreadyExistsException(actor.getCode());
        }
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
        result = new HashSet<Actor>();
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
        result = new HashSet<Group>();
        for (ExecutorGroupMembership membership : getExecutorMemberships(executor)) {
            result.add(membership.getGroup());
        }
        return result;
    }

    private Set<Group> getExecutorGroupsAll(Executor executor, Set<Executor> visited, boolean includeTemporaryGroups) {
        Set<Group> cached = executorCacheCtrl.getExecutorParentsAll(executor);
        if (cached != null) {
            return cached;
        }
        if (visited.contains(executor)) {
            return new HashSet<Group>();
        }
        visited.add(executor);
        Set<Group> result = new HashSet<Group>();
        for (Group group : getExecutorGroups(executor)) {
            if (!group.isTemporary() || group.isTemporary() && includeTemporaryGroups) {
                result.add(group);
                result.addAll(getExecutorGroupsAll(group, visited, includeTemporaryGroups));
            }
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
        List<T> list = getHibernateTemplate().executeFind(new HibernateCallback<List<T>>() {

            @Override
            public List<T> doInHibernate(Session session) {
                Query query = session.createQuery("from " + clazz.getName() + " where " + propertyName + " in (:ids)");
                query.setParameterList("ids", identifiers);
                return query.list();
            }
        });
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
            return getHibernateTemplate().get(clazz, id);
        }
    }

    private <T extends Executor> T getExecutorByName(Class<T> clazz, String name) {
        Executor executor = executorCacheCtrl.getExecutor(name);
        if (executor != null) {
            return (T) (clazz.isAssignableFrom(executor.getClass()) ? executor : null);
        } else {
            return (T) findFirstOrNull("from " + clazz.getName() + " where name=?", name);
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
        return findFirstOrNull("from ActorPassword where actorId=?", actor.getId());
    }

    private Actor getActorByCodeInternal(Long code) {
        Actor actor = executorCacheCtrl.getActor(code);
        if (actor != null) {
            return actor;
        }
        return findFirstOrNull("from Actor where code=?", code);
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

    public <T extends Executor> T createWithoutCacheVerify(T executor) {
        T exist = (T) findFirstOrNull("from Executor where name = ?", executor.getName());
        if (exist != null) {
            throw new ExecutorAlreadyExistsException(executor.getName());
        }
        if (executor instanceof Actor) {
            checkActorCode((Actor) executor, false);
        }
        getHibernateTemplate().save(executor);
        return executor;
    }

    @Override
    public List<Executor> getExecutorsLikeName(String nameTemplate) {
        return getHibernateTemplate().find("from Executor where name like ?", nameTemplate);
    }

    @Override
    public boolean isAdministrator(Actor actor) {
        try {
            Group administratorsGroup = (Group) getExecutor(SystemProperties.getAdministratorsGroupName());
            return isExecutorInGroup(actor, administratorsGroup);
        } catch (ExecutorDoesNotExistException e) {
            log.debug(e);
            return false;
        }
    }

}
