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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.WeakPasswordException;
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
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ServiceTestHelper {

    private final static String ADMINISTRATOR_NAME = "Administrator";

    private final static String ADMINISTRATOR_PASSWORD = "wf";

    private final static String ADMINISTRATORS_NAME = "Administrators";

    private final Set<Executor> createdExecutorsSet = Sets.newHashSet();

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

    private ExecutorService executorService;

    private RelationService relationService;

    private SubstitutionService substitutionService;

    protected AuthorizationService authorizationService;

    private AuthenticationService authenticationService;

    private SystemService systemService;

    private ProfileService profileService;

    private Actor fakeActor;

    private List<Executor> fakeExecutors;

    private Actor baseGroupActor;

    private Actor subGroupActor;

    private Group fakeGroup;

    private Group baseGroup;

    private Group subGroup;

    private User fakeUser;

    private User authorizedPerformerUser, unauthorizedPerformerUser;

    private final String testClassName;

    protected User adminUser;

    private Set<Subject> subjectOfActorsWithProfileSet = Sets.newHashSet();

    public ServiceTestHelper(String testClassName) throws InternalApplicationException {
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

    public List<Long> toIds(Collection<? extends SecuredObject> list) {
        List<Long> ids = Lists.newArrayList();
        for (SecuredObject securedObject : list) {
            ids.add(securedObject.getIdentifiableId());
        }
        return ids;
    }

    private void createProfileServiceDelegate() {
        profileService = Delegates.getProfileService();
    }

    public ProfileService getProfileService() {
        return profileService;
    }

    public User getAdminUser() {
        return adminUser;
    }

    /**
     * Creates groups and actors group contains subGroup and subActor subGroup contains subGroupActor
     */
    public void createDefaultExecutorsMap() throws InternalApplicationException {
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

    public void releaseResources() throws InternalApplicationException {
        removeCreatedProfiles();
        removeCreatedExecutors();
        removeDefaultExecutors();
        executorService = null;
        executorService = null;
        authorizationService = null;
        authorizationService = null;
        authenticationService = null;
        authenticationService = null;
        systemService = null;
        systemService = null;

        fakeActor = null;
        fakeGroup = null;

        fakeExecutors.clear();
        fakeExecutors = null;

        baseGroup = null;
        baseGroupActor = null;
        subGroup = null;
        subGroupActor = null;
        fakeUser = null;

    }

    private void removeCreatedProfiles() throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
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

    public void removeExecutorIfExists(Executor executor) throws InternalApplicationException {
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

    public void addExecutorToGroup(Executor executor, Group group) throws InternalApplicationException {
        executorService.addExecutorsToGroup(adminUser, Lists.newArrayList(executor.getId()), group.getId());
    }

    public void removeExecutorFromGroup(Executor executor, Group group) throws InternalApplicationException {
        executorService.removeExecutorsFromGroup(adminUser, Lists.newArrayList(executor.getId()), group.getId());
    }

    public boolean isExecutorExist(Executor executor) throws InternalApplicationException {
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

    public void setPermissionsToAuthorizedPerformer(Collection<Permission> permissions, Executor executor) throws InternalApplicationException {
        authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, executor);
    }

    public void setPermissionsToAuthorizedPerformerOnExecutorsList(Collection<Permission> permissions, List<? extends Executor> executors)
            throws InternalApplicationException {
        for (Executor executor : executors) {
            authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, executor);
        }
    }

    public void setPermissionsToAuthorizedPerformerOnExecutors(Collection<Permission> permissions) {
        authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, SecuredSingleton.EXECUTORS);
    }

    public Actor createActorIfNotExist(String name, String description)
            throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        Actor actor;
        try {
            actor = executorService.getExecutorByName(adminUser, name);
        } catch (ExecutorDoesNotExistException e) {
            actor = executorService.create(adminUser, new Actor(name, description));
        }
        createdExecutorsSet.add(actor);
        return actor;
    }

    public List<Actor> createActorArray(String name, String description)
            throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
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

    public Group createGroupIfNotExist(String name, String description)
            throws ExecutorAlreadyExistsException, AuthorizationException, AuthenticationException {
        Group group;
        try {
            group = executorService.getExecutorByName(adminUser, name);
        } catch (ExecutorDoesNotExistException e) {
            group = executorService.create(adminUser, new Group(name, description));
        }
        createdExecutorsSet.add(group);
        return group;
    }

    public List<Group> createGroupArray(String name, String description) throws InternalApplicationException {
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

    public List<Executor> createMixedActorsGroupsArray(String name, String description) throws InternalApplicationException {
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

    public User getAuthorizedPerformerUser() {
        return authorizedPerformerUser;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public User getUnauthorizedPerformerUser() {
        return unauthorizedPerformerUser;
    }

    public Map<String, Executor> getDefaultExecutorsMap() {
        return defaultExecutorsMap;
    }

    public Actor getFakeActor() {
        return fakeActor;
    }

    public Group getFakeGroup() {
        return fakeGroup;
    }

    public List<Executor> getFakeExecutors() {
        return fakeExecutors;
    }

    public boolean isExecutorInGroup(Executor executor, Group group) throws InternalApplicationException {
        return executorService.isExecutorInGroup(adminUser, executor, group);
    }

    public boolean isExecutorInGroups(Executor executor, List<Group> groups) throws InternalApplicationException {
        for (Group group : groups) {
            if (!executorService.isExecutorInGroup(adminUser, executor, group)) {
                return false;
            }
        }
        return true;
    }

    public boolean isExecutorsInGroup(List<? extends Executor> executors, Group group) throws InternalApplicationException {
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

    private void createPerformersAndPerformesSubjects() throws ExecutorDoesNotExistException, ExecutorAlreadyExistsException, AuthorizationException,
            AuthenticationException, WeakPasswordException {
        String authorizedActorName = testClassName + AUTHORIZED_PERFORMER_NAME;
        Actor authorizedPerformerActor;
        try {
            authorizedPerformerActor = executorService.getExecutorByName(adminUser, authorizedActorName);
        } catch (ExecutorDoesNotExistException e) {
            authorizedPerformerActor = executorService.create(adminUser, new Actor(authorizedActorName, AUTHORIZED_PERFORMER_DESCRIPTION));
            executorService.setPassword(adminUser, authorizedPerformerActor, AUTHORIZED_PERFORMER_PASSWORD);
        }
        String unauthorizedActorName = testClassName + UNAUTHORIZED_PERFORMER_NAME;
        Actor unauthorizedPerformerActor;
        try {
            unauthorizedPerformerActor = executorService.getExecutorByName(adminUser, unauthorizedActorName);
        } catch (ExecutorDoesNotExistException e) {
            unauthorizedPerformerActor = executorService.create(adminUser, new Actor(unauthorizedActorName, UNAUTHORIZED_PERFORMER_DESCRIPTION));
            executorService.setPassword(adminUser, unauthorizedPerformerActor, UNAUTHORIZED_PERFORMER_PASSWORD);
        }
        authorizedPerformerUser = authenticationService.authenticateByLoginPassword(authorizedPerformerActor.getName(),
                AUTHORIZED_PERFORMER_PASSWORD);
        unauthorizedPerformerUser = authenticationService.authenticateByLoginPassword(unauthorizedPerformerActor.getName(),
                UNAUTHORIZED_PERFORMER_PASSWORD);
    }

    private void createAdminSubject() throws InternalApplicationException {
        adminUser = authenticationService.authenticateByLoginPassword(ADMINISTRATOR_NAME, ADMINISTRATOR_PASSWORD);
    }

    /** Removes all created executors from DB. */
    private void removeCreatedExecutors() throws InternalApplicationException {
        try {
            for (Executor executor : createdExecutorsSet) {
                executor = executorService.getExecutor(adminUser, executor.getId());
                executorService.remove(adminUser, Lists.newArrayList(executor.getId()));
            }
        } catch (ExecutorDoesNotExistException ignored) {
        }
        executorService.remove(adminUser, Lists.newArrayList(getAuthorizedPerformerActor().getId(), getUnauthorizedPerformerActor().getId()));
    }

    public Collection<Permission> getOwnPermissions(Executor performer, Executor executor) throws InternalApplicationException {
        return authorizationService.getIssuedPermissions(adminUser, performer, executor);
    }

    /** check if default executors still exists in db, and id so removes them */
    private void removeDefaultExecutors() throws InternalApplicationException {
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

    public AuthorizationService getAuthorizationService() {
        return authorizationService;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public SystemService getSystemService() {
        return systemService;
    }

    public User getFakeUser() {
        return fakeUser;
    }

    public Actor getAuthorizedPerformerActor() throws InternalApplicationException {
        return authorizedPerformerUser.getActor();
    }

    public Actor getUnauthorizedPerformerActor() throws InternalApplicationException {
        return unauthorizedPerformerUser.getActor();
    }

    public Group getAdministratorsGroup() throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, ADMINISTRATORS_NAME);
    }

    public Actor getAdministrator() throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, ADMINISTRATOR_NAME);
    }

    public String getAdministratorPassword() {
        return ADMINISTRATOR_PASSWORD;
    }

    public Group getBaseGroup() throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, baseGroup.getName());
        // return baseGroup; we can't cache executor that changes it's state in
        // db
    }

    public Actor getBaseGroupActor() throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, baseGroupActor.getName());
        // return baseGroupActor;
    }

    public Group getSubGroup() throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, subGroup.getName());
        // return subGroup;
    }

    public Actor getSubGroupActor() throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, subGroupActor.getName());
        // return subGroupActor;
    }

    public Executor getExecutor(String name) throws InternalApplicationException {
        return executorService.getExecutorByName(adminUser, name);
    }

    public void setActorStatus(Actor actor, boolean isActive) throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        executorService.setStatus(getAdminUser(), actor, isActive);
    }

    /*
     * TODO: remove public Profile getDefaultProfile(Subject subject) throws InternalApplicationException, AuthenticationException { Profile profile =
     * profileService.getProfile(subject); subjectOfActorsWithProfileSet.add(subject); return profile; }
     * 
     * public Identifiable getFakeIdentifiable() { return new Identifiable() {
     * 
     * @Override public Long getId() { return 0L; }
     * 
     * @Override public SecuredObjectType getSecuredObjectType() { return SecuredObjectType.DEFINITION; } }; }
     */

    public Substitution createTerminator(User user, SubstitutionCriteria substitutionCriteria, boolean isEnabled)
            throws AuthorizationException, ExecutorDoesNotExistException, AuthenticationException {
        TerminatorSubstitution terminatorSubstitution = new TerminatorSubstitution();
        terminatorSubstitution.setOrgFunction("");
        terminatorSubstitution.setActorId(user.getActor().getId());
        terminatorSubstitution.setCriteria(substitutionCriteria);
        terminatorSubstitution.setEnabled(isEnabled);
        return substitutionService.createSubstitution(getAdminUser(), terminatorSubstitution);
    }

    public Substitution createActorSubstitutor(User user, String orgFunction, SubstitutionCriteria substitutionCriteria, boolean isEnabled)
            throws AuthorizationException, ExecutorDoesNotExistException, AuthenticationException {
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

    public Relation createRelation(String name, String description)
            throws RelationAlreadyExistException, AuthorizationException, AuthenticationException {
        return relationService.createRelation(adminUser, new Relation(name, description));
    }

    public RelationPair addRelationPair(Long relationId, Executor left, Executor right)
            throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        return relationService.addRelationPair(adminUser, relationId, left, right);
    }

    public void removeRelation(Long relationId) throws RelationDoesNotExistException, AuthorizationException, AuthenticationException {
        relationService.removeRelation(adminUser, relationId);
    }

    public <T extends SubstitutionCriteria> T createSubstitutionCriteria(T substitutionCriteria)
            throws AuthorizationException, ExecutorDoesNotExistException, AuthenticationException {
        if (substitutionCriteria == null) {
            return null;
        }
        substitutionService.createCriteria(getAdminUser(), substitutionCriteria);
        return (T) substitutionService.getCriteriaByName(getAdminUser(), substitutionCriteria.getName());
    }

    public void removeSubstitutionCriteria(SubstitutionCriteria substitutionCriteria)
            throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        if (substitutionCriteria == null) {
            return;
        }
        substitutionService.deleteCriteria(getAdminUser(), substitutionCriteria);
    }

    public void removeCriteriaFromSubstitution(Substitution substitution)
            throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException, SubstitutionDoesNotExistException {
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
