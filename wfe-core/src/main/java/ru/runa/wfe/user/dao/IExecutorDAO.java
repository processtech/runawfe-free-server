package ru.runa.wfe.user.dao;

import java.util.List;
import java.util.Set;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.IExecutorLoader;

public interface IExecutorDAO extends IExecutorLoader {

    /**
     * Load {@linkplain Actor} by name. Throws exception if load is impossible, or exist group with same name.
     * 
     * @param name
     *            Loaded actor name.
     * @return {@linkplain Actor} with specified name.
     */
    public Actor getActor(String name);

    /**
     * Returns all executor parent {@linkplain Groups}s recursively. For example G1 contains G2 and A3, G2 contains A1 and A2. In this case:</br>
     * <code> getExecutorParentsAll(A1) == {G1, G2}</code><br/>
     * <code> getExecutorParentsAll(A3) == {G1} </code>
     * 
     * @param executor
     *            {@linkplain Executor} to load parent groups.
     * @param includeTemporaryGroups
     *            Flag equals true, if temporary groups must be returned and false to remove temporary groups from result.
     * @return Set of executor parents.
     */
    public Set<Group> getExecutorParentsAll(Executor executor, boolean includeTemporaryGroups);

    /**
     * Load {@linkplain Actor} by identity. Throws exception if load is impossible, or exist group with same identity.
     * 
     * @param name
     *            Loaded actor identity.
     * @return {@linkplain Actor} with specified identity.
     */
    public Actor getActor(Long id);

    public Group getGroup(String name);

    /**
     * Returns all {@linkplain Actor}s from {@linkplain Group} recursively. All actors from subgroups is also added to result. For example G1 contains
     * G2 and A3, G2 contains A1 and A2. In this case:</br> <code> getGroupActors(G2) == {A1, A2}</code><br/>
     * <code> getGroupActors(G1) == {A1, A2, A3} </code>
     * 
     * @param group
     *            {@linkplain Group} to load {@linkplain Actor} children's
     * @return Set of actor children's.
     */
    public Set<Actor> getGroupActors(Group group);

    /**
     * Returns group children (first level children, not recursively).</br> For example G1 contains G2, G2 contains A1 and A2. In this case:</br>
     * <code> getGroupChildren(G2) == {A1, A2}</code><br/>
     * <code> getGroupChildren(G1) == {G2} </code>
     * 
     * @param group
     *            A group to load children's from.
     * @param batchPresentation
     *            As {@linkplain BatchPresentation} of array returned.
     * @return Array of group children.
     */
    public Set<Executor> getGroupChildren(Group group);

    public List<Executor> getExecutorsLikeName(String nameTemplate);

    public boolean isAdministrator(Actor actor);
}
