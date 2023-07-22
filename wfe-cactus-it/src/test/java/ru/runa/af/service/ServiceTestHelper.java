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
package ru.runa.af.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import lombok.Getter;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class ServiceTestHelper {

    private final static String ADMINISTRATOR_NAME = "Administrator";

    private final static String ADMINISTRATOR_PASSWORD = "wf";

    private final static String ADMINISTRATORS_NAME = "Administrators";

    private final Set<Executor> createdExecutorsSet = Sets.newHashSet();

    @Getter
    private final Map<String, Executor> defaultExecutorsMap = Maps.newHashMap();

    public final static String AUTHORIZED_PERFORMER_NAME = "AUTHORIZED_PERFORMER_NAME";
    public final static String AUTHORIZED_PERFORMER_DESCRIPTION = "AUTHORIZED_PERFORMER_DESCRIPTION";
    public final static String AUTHORIZED_PERFORMER_PASSWORD = "AUTHORIZED_PERFORMER_PASSWORD";

    public final static String UNAUTHORIZED_PERFORMER_NAME = "UNAUTHORIZED_PERFORMER_NAME";
    public final static String UNAUTHORIZED_PERFORMER_DESCRIPTION = "UNAUTHORIZED_PERFORMER_DESCRIPTION";
    public final static String UNAUTHORIZED_PERFORMER_PASSWORD = "UNAUTHORIZED_PERFORMER_PASSWORD";

    public final static String BASE_GROUP_NAME = "BASE_GROUP_NAME";
    public final static String BASE_GROUP_DESC = "BASE_GROUP_DESC";
    public final static String BASE_GROUP_ACTOR_NAME = "BASE_GROUP_ACTOR_NAME";
    public final static String BASE_GROUP_ACTOR_DESC = "BASE_GROUP_ACTOR_DESC";
    public final static String SUB_GROUP_NAME = "SUB_GROUP_NAME";
    public final static String SUB_GROUP_DESC = "SUB_GROUP_DESC";
    public final static String SUB_GROUP_ACTOR_NAME = "SUB_GROUP_ACTOR_NAME";
    public final static String SUB_GROUP_ACTOR_DESC = "SUB_GROUP_ACTOR_DESC";
    public final static String FAKE_ACTOR_NAME = "FAKE_ACTOR_NAME";
    public final static String FAKE_ACTOR_DESC = "FAKE_ACTOR_DESC";
    public final static String FAKE_GROUP_NAME = "FAKE_GROUP_NAME";
    public final static String FAKE_GROUP_DESC = "FAKE_GROUP_DESC";

    @Getter
    private ExecutorService executorService;

    private RelationService relationService;

    private SubstitutionService substitutionService;

    @Getter
    protected AuthorizationService authorizationService;

    @Getter
    private AuthenticationService authenticationService;

    @Getter
    private SystemService systemService;

    @Getter
    private ProfileService profileService;

    @Getter
    private Actor fakeActor;

    @Getter
    private List<Executor> fakeExecutors;

    private Actor baseGroupActor;

    private Actor subGroupActor;

    @Getter
    private Group fakeGroup;

    private Group baseGroup;

    private Group subGroup;

    @Getter
    private User fakeUser;

    @Getter
    private User authorizedUser;

    @Getter
    private User unauthorizedUser;

    private final String testClassName;

    @Getter
    protected User adminUser;

    private Set<Subject> subjectOfActorsWithProfileSet = Sets.newHashSet();

    public ServiceTestHelper(String testClassName) {
        this.testClassName = testClassName;
        createExecutorServiceDelegate();
        createRelationServiceDelegate();
        createSubstitutionServiceDelegate();
        createAuthorizationServiceDelegate();
        createAuthenticationServiceDelegate();
        createSystemServiceDelegate();
        createAdminSubject();
        createPerformersAndPerformesSubjects();
        createFakeExecutors();
        createFakeUser();
        createProfileServiceDelegate();
    }

//    @SafeVarargs
//    public final <T> ArrayList<T> list(T... oo) {
//        return Lists.newArrayList(oo);
//    }

    public ArrayList<Long> toIds(Collection<? extends SecuredObject> list) {
        // TODO Use java8 map.
        val ids = new ArrayList<Long>(list.size());
        for (SecuredObject securedObject : list) {
            ids.add(securedObject.getIdentifiableId());
        }
        return ids;
    }

    private void createProfileServiceDelegate() {
        profileService = Delegates.getProfileService();
    }

    /**
     * Creates groups and actors group contains subGroup and subActor subGroup contains subGroupActor
     */
    public void createDefaultExecutorsMap() {
        baseGroup = executorService.create(adminUser, new Group(testClassName + BASE_GROUP_NAME, testClassName + BASE_GROUP_DESC));
        defaultExecutorsMap.put(BASE_GROUP_NAME, baseGroup);

        baseGroupActor = executorService.create(adminUser, new Actor(testClassName + BASE_GROUP_ACTOR_NAME, testClassName + BASE_GROUP_ACTOR_NAME));
        defaultExecutorsMap.put(BASE_GROUP_ACTOR_NAME, baseGroupActor);

        subGroup = executorService.create(adminUser, new Group(testClassName + SUB_GROUP_NAME, testClassName + SUB_GROUP_DESC));
        defaultExecutorsMap.put(SUB_GROUP_NAME, subGroup);

        subGroupActor = executorService.create(adminUser, new Actor(testClassName + SUB_GROUP_ACTOR_NAME, testClassName + SUB_GROUP_ACTOR_DESC));
        defaultExecutorsMap.put(SUB_GROUP_ACTOR_NAME, subGroupActor);

        executorService.addExecutorsToGroup(adminUser, Lists.newArrayList(baseGroupActor.getId(), subGroup.getId()), baseGroup.getId());
        // subGroup = executorService.getExecutorByNameChildren(adminUser,
        // subGroup, BatchPresentationFactory.ACTORS.createDefault(), false);
        executorService.addExecutorsToGroup(adminUser, Lists.newArrayList(subGroupActor.getId()), subGroup.getId());
    }

    public void releaseResources() {
        removeCreatedProfiles();
        removeCreatedExecutors();
        removeDefaultExecutors();
        executorService = null;
        authorizationService = null;
        authenticationService = null;
        systemService = null;

        fakeActor = null;
        fakeGroup = null;
        fakeExecutors.clear();
        fakeExecutors = null;
        fakeUser = null;

        baseGroup = null;
        baseGroupActor = null;
        subGroup = null;
        subGroupActor = null;
    }

    private void removeCreatedProfiles() {
        subjectOfActorsWithProfileSet.clear();
        subjectOfActorsWithProfileSet = null;
        profileService = null;
    }

    /**
     * This method removes executor from createdExecutors set bun not from db, call this method when you want to remove executor manually.
     */
    public void removeCreatedExecutor(Executor executor) {
        createdExecutorsSet.remove(executor);
    }

    public void removeExecutorIfExists(Executor executor) {
        if (executor != null) {
            try {
                if (executor instanceof Actor) {
                    executor = executorService.getExecutorByName(adminUser, executor.getName());
                }
                if (executor instanceof Group) {
                    executor = executorService.getExecutorByName(adminUser, executor.getName());
                }
                executorService.remove(adminUser, Lists.newArrayList(executor.getId()));
            } catch (ExecutorDoesNotExistException ignored) {
            }
        }
    }

    public void addExecutorToGroup(Executor executor, Group group) {
        executorService.addExecutorsToGroup(adminUser, Lists.newArrayList(executor.getId()), group.getId());
    }

    public void removeExecutorFromGroup(Executor executor, Group group) {
        executorService.removeExecutorsFromGroup(adminUser, Lists.newArrayList(executor.getId()), group.getId());
    }

    public boolean isExecutorExist(Executor executor) {
        boolean isExecutorExist = true;
        try {
            if (executor instanceof Actor) {
                executorService.getExecutorByName(adminUser, executor.getName());
            }
            if (executor instanceof Group) {
                executorService.getExecutorByName(adminUser, executor.getName());
            }
        } catch (ExecutorDoesNotExistException e) {
            isExecutorExist = false;
        }
        return isExecutorExist;
    }

    public boolean isPasswordCorrect(String name, String password) {
        boolean isPasswordCorrect = false;
        try {
            authenticationService.authenticateByLoginPassword(name, password);
            isPasswordCorrect = true;
        } catch (InternalApplicationException ignored) {
        }
        return isPasswordCorrect;
    }

    public void setPermissionsToAuthorizedActor(Collection<Permission> permissions, SecuredObject object) {
        authorizationService.setPermissions(adminUser, getAuthorizedActor().getId(), permissions, object);
    }

    public void setPermissionsToAuthorizedActor(Collection<Permission> permissions, List<? extends SecuredObject> objects) {
        for (SecuredObject o : objects) {
            setPermissionsToAuthorizedActor(permissions, o);
        }
    }

    public Actor createActorIfNotExist(String name, String description) {
        Actor actor;
        try {
            actor = executorService.getExecutorByName(adminUser, name);
        } catch (ExecutorDoesNotExistException e) {
            actor = executorService.create(adminUser, new Actor(name, description));
        }
        createdExecutorsSet.add(actor);
        return actor;
    }

    public List<Actor> createActorArray(String name, String description) {
        Actor[] actorArray = new Actor[5];
        actorArray[0] = executorService.create(adminUser, new Actor(name + "0", description + "0"));
        actorArray[1] = executorService.create(adminUser, new Actor(name + "1", description + "1"));
        actorArray[2] = executorService.create(adminUser, new Actor(name + "2", description + "2"));
        actorArray[3] = executorService.create(adminUser, new Actor(name + "3", description + "3"));
        actorArray[4] = executorService.create(adminUser, new Actor(name + "4", description + "4"));
        createdExecutorsSet.add(actorArray[0]);
        createdExecutorsSet.add(actorArray[1]);
        createdExecutorsSet.add(actorArray[2]);
        createdExecutorsSet.add(actorArray[3]);
        createdExecutorsSet.add(actorArray[4]);
        return Lists.newArrayList(actorArray);
    }

    public Group createGroupIfNotExist(String name, String description) {
        Group group;
        try {
            group = executorService.getExecutorByName(adminUser, name);
        } catch (ExecutorDoesNotExistException e) {
            group = executorService.create(adminUser, new Group(name, description));
        }
        createdExecutorsSet.add(group);
        return group;
    }

    public ArrayList<Group> createGroupArray(String name, String description) {
        Group[] groupArray = new Group[5];
        groupArray[0] = executorService.create(adminUser, new Group(name + "0", description + "0"));
        groupArray[1] = executorService.create(adminUser, new Group(name + "1", description + "1"));
        groupArray[2] = executorService.create(adminUser, new Group(name + "2", description + "2"));
        groupArray[3] = executorService.create(adminUser, new Group(name + "3", description + "3"));
        groupArray[4] = executorService.create(adminUser, new Group(name + "4", description + "4"));
        createdExecutorsSet.add(groupArray[0]);
        createdExecutorsSet.add(groupArray[1]);
        createdExecutorsSet.add(groupArray[2]);
        createdExecutorsSet.add(groupArray[3]);
        createdExecutorsSet.add(groupArray[4]);
        return Lists.newArrayList(groupArray);
    }

    public ArrayList<Executor> createMixedActorsGroupsArray(String name, String description) {
        Executor[] mixedArray = new Executor[5];
        mixedArray[0] = executorService.create(adminUser, new Actor(name + "0", description + "0"));
        mixedArray[1] = executorService.create(adminUser, new Group(name + "1", description + "1"));
        mixedArray[2] = executorService.create(adminUser, new Actor(name + "2", description + "2"));
        mixedArray[3] = executorService.create(adminUser, new Group(name + "3", description + "3"));
        mixedArray[4] = executorService.create(adminUser, new Actor(name + "4", description + "4"));
        createdExecutorsSet.add(mixedArray[0]);
        createdExecutorsSet.add(mixedArray[1]);
        createdExecutorsSet.add(mixedArray[2]);
        createdExecutorsSet.add(mixedArray[3]);
        createdExecutorsSet.add(mixedArray[4]);
        return Lists.newArrayList(mixedArray);
    }

    public boolean isExecutorInGroup(Executor executor, Group group) {
        return executorService.isExecutorInGroup(adminUser, executor, group);
    }

    public boolean isExecutorInGroups(Executor executor, List<Group> groups) {
        for (Group group : groups) {
            if (!executorService.isExecutorInGroup(adminUser, executor, group)) {
                return false;
            }
        }
        return true;
    }

    public boolean isExecutorsInGroup(List<? extends Executor> executors, Group group) {
        for (Executor executor : executors) {
            if (!executorService.isExecutorInGroup(adminUser, executor, group)) {
                return false;
            }
        }
        return true;
    }

    private void createExecutorServiceDelegate() {
        executorService = Delegates.getExecutorService();
    }

    private void createRelationServiceDelegate() {
        relationService = Delegates.getRelationService();
    }

    private void createSubstitutionServiceDelegate() {
        substitutionService = Delegates.getSubstitutionService();
    }

    private void createAuthorizationServiceDelegate() {
        authorizationService = Delegates.getAuthorizationService();
    }

    private void createAuthenticationServiceDelegate() {
        authenticationService = Delegates.getAuthenticationService();
    }

    private void createSystemServiceDelegate() {
        systemService = Delegates.getSystemService();
    }

    private void createPerformersAndPerformesSubjects() {
        String authorizedActorName = testClassName + AUTHORIZED_PERFORMER_NAME;
        Actor authorizedActor;
        try {
            authorizedActor = executorService.getExecutorByName(adminUser, authorizedActorName);
        } catch (ExecutorDoesNotExistException e) {
            authorizedActor = executorService.create(adminUser, new Actor(authorizedActorName, AUTHORIZED_PERFORMER_DESCRIPTION));
            executorService.setPassword(adminUser, authorizedActor, AUTHORIZED_PERFORMER_PASSWORD);
        }
        String unauthorizedActorName = testClassName + UNAUTHORIZED_PERFORMER_NAME;
        Actor unauthorizedActor;
        try {
            unauthorizedActor = executorService.getExecutorByName(adminUser, unauthorizedActorName);
        } catch (ExecutorDoesNotExistException e) {
            unauthorizedActor = executorService.create(adminUser, new Actor(unauthorizedActorName, UNAUTHORIZED_PERFORMER_DESCRIPTION));
            executorService.setPassword(adminUser, unauthorizedActor, UNAUTHORIZED_PERFORMER_PASSWORD);
        }
        authorizedUser = authenticationService.authenticateByLoginPassword(authorizedActor.getName(),
                AUTHORIZED_PERFORMER_PASSWORD);
        unauthorizedUser = authenticationService.authenticateByLoginPassword(unauthorizedActor.getName(),
                UNAUTHORIZED_PERFORMER_PASSWORD);
    }

    private void createAdminSubject() {
        adminUser = authenticationService.authenticateByLoginPassword(ADMINISTRATOR_NAME, ADMINISTRATOR_PASSWORD);
    }

    /** Removes all created executors from DB. */
    private void removeCreatedExecutors() {
        try {
            for (Executor executor : createdExecutorsSet) {
                executor = executorService.getExecutor(adminUser, executor.getId());
                executorService.remove(adminUser, Lists.newArrayList(executor.getId()));
            }
        } catch (ExecutorDoesNotExistException ignored) {
        }
        executorService.remove(adminUser, Lists.newArrayList(getAuthorizedActor().getId(), getUnauthorizedActor().getId()));
    }

    public Collection<Permission> getOwnPermissions(Executor performer, Executor executor) {
        return authorizationService.getIssuedPermissions(adminUser, performer, executor);
    }

    /** check if default executors still exists in db, and id so removes them */
    private void removeDefaultExecutors() {
        List<Executor> undeletedExecutorsList = Lists.newArrayList();
        for (Executor executor : defaultExecutorsMap.values()) {
            boolean canRemove = false;
            try {
                executor = executorService.getExecutor(adminUser, executor.getId());
                canRemove = true;
            } catch (ExecutorDoesNotExistException e) {
                // do nothing, this executor was deleted
            }
            if (canRemove) {
                undeletedExecutorsList.add(executor);
            }
        }
        executorService.remove(adminUser, toIds(undeletedExecutorsList));
    }

    private void createFakeExecutors() {
        fakeActor = new Actor(testClassName + FAKE_ACTOR_NAME, testClassName + FAKE_ACTOR_DESC);
        fakeActor.setId(adminUser.getActor().getId());
        fakeGroup = new Group(testClassName + FAKE_GROUP_NAME, testClassName + FAKE_GROUP_DESC);
        fakeExecutors = Lists.newArrayList();
        for (int i = 0; i < 5; i++) {
            fakeExecutors.add(new Actor(testClassName + FAKE_ACTOR_NAME + i, testClassName + FAKE_ACTOR_DESC + i));
        }
    }

    private void createFakeUser() {
        fakeUser = new User(fakeActor, null);
    }

    public Actor getAuthorizedActor() {
        return authorizedUser.getActor();
    }

    public Actor getUnauthorizedActor() {
        return unauthorizedUser.getActor();
    }

    public Group getAdministratorsGroup() {
        return executorService.getExecutorByName(adminUser, ADMINISTRATORS_NAME);
    }

    public Actor getAdministrator() {
        return executorService.getExecutorByName(adminUser, ADMINISTRATOR_NAME);
    }

    public String getAdministratorPassword() {
        return ADMINISTRATOR_PASSWORD;
    }

    public Group getBaseGroup() {
        return executorService.getExecutorByName(adminUser, baseGroup.getName());
        // return baseGroup; we can't cache executor that changes it's state in
        // db
    }

    public Actor getBaseGroupActor() {
        return executorService.getExecutorByName(adminUser, baseGroupActor.getName());
        // return baseGroupActor;
    }

    public Group getSubGroup() {
        return executorService.getExecutorByName(adminUser, subGroup.getName());
        // return subGroup;
    }

    public Actor getSubGroupActor() {
        return executorService.getExecutorByName(adminUser, subGroupActor.getName());
        // return subGroupActor;
    }

    public Executor getExecutor(String name) {
        return executorService.getExecutorByName(adminUser, name);
    }

    public void setActorStatus(Actor actor, boolean isActive) {
        executorService.setStatus(getAdminUser(), actor, isActive);
    }

    /*
     * TODO: remove public Profile getDefaultProfile(Subject subject) { Profile profile =
     * profileService.getProfile(subject); subjectOfActorsWithProfileSet.add(subject); return profile; }
     * 
     * public Identifiable getFakeIdentifiable() { return new Identifiable() {
     * 
     * @Override public Long getId() { return 0L; }
     * 
     * @Override public SecuredObjectType getSecuredObjectType() { return SecuredObjectType.DEFINITION; } }; }
     */

    public Substitution createTerminator(User user, SubstitutionCriteria substitutionCriteria, boolean isEnabled) {
        TerminatorSubstitution terminatorSubstitution = new TerminatorSubstitution();
        terminatorSubstitution.setOrgFunction("");
        terminatorSubstitution.setActorId(user.getActor().getId());
        terminatorSubstitution.setCriteria(substitutionCriteria);
        terminatorSubstitution.setEnabled(isEnabled);
        return substitutionService.createSubstitution(getAdminUser(), terminatorSubstitution);
    }

    public Substitution createActorSubstitutor(User user, String orgFunction, SubstitutionCriteria substitutionCriteria, boolean isEnabled) {
        Substitution substitution = new Substitution();
        substitution.setActorId(user.getActor().getId());
        substitution.setOrgFunction(orgFunction);
        substitution.setCriteria(substitutionCriteria);
        substitution.setEnabled(isEnabled);
        return substitutionService.createSubstitution(getAdminUser(), substitution);
    }

    public BatchPresentation getExecutorBatchPresentation() {
        return BatchPresentationFactory.EXECUTORS.createDefault();
    }

    public BatchPresentation getExecutorBatchPresentation(String presentationId) {
        return BatchPresentationFactory.EXECUTORS.createDefault(presentationId);
    }

    public Relation createRelation(String name, String description) {
        return relationService.createRelation(adminUser, new Relation(name, description));
    }

    public RelationPair addRelationPair(Long relationId, Executor left, Executor right) {
        return relationService.addRelationPair(adminUser, relationId, left, right);
    }

    public void removeRelation(Long relationId) {
        relationService.removeRelation(adminUser, relationId);
    }

    public <T extends SubstitutionCriteria> T createSubstitutionCriteria(T substitutionCriteria) {
        if (substitutionCriteria == null) {
            return null;
        }
        substitutionService.createCriteria(getAdminUser(), substitutionCriteria);
        return (T) substitutionService.getCriteriaByName(getAdminUser(), substitutionCriteria.getName());
    }

    public void removeSubstitutionCriteria(SubstitutionCriteria substitutionCriteria) {
        if (substitutionCriteria == null) {
            return;
        }
        substitutionService.deleteCriteria(getAdminUser(), substitutionCriteria);
    }

    public void removeCriteriaFromSubstitution(Substitution substitution) {
        substitution.setCriteria(null);
        substitutionService.updateSubstitution(getAdminUser(), substitution);
    }

    @Deprecated
    // TODO: ExecutionService.getExecutorsByIds
    public <T extends Executor> List<T> getExecutors(User performer, List<Long> ids) {
        List<T> res = Lists.newArrayList();
        for (Long id : ids) {
            res.add(executorService.<T>getExecutor(performer, id));
        }
        return res;
    }
}
