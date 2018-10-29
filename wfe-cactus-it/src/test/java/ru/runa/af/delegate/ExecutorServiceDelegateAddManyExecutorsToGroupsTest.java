package ru.runa.af.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;

import com.google.common.collect.Lists;

/*
 */
public class ExecutorServiceDelegateAddManyExecutorsToGroupsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateAddManyExecutorsToGroupsTest.class.getName();

    private Group additionalGroup;
    private List<Group> additionalGroups;

    private Actor additionalActor;
    private List<Actor> additionalActors;

    private List<Executor> additionalActorGroupsMixed;

    private final Collection<Permission> addToGroupPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP,
            GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> readlistPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP);

    private List<Actor> getAdditionalActors() throws InternalApplicationException {
        List<Long> ids = Lists.newArrayList();
        for (Actor actor : additionalActors) {
            ids.add(actor.getId());
        }
        List<Actor> executors = th.getExecutors(th.getAdminUser(), ids);
        return executors;
    }

    private List<Group> getAdditionalGroups() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        List<Long> ids = Lists.newArrayList();
        for (Group group : additionalGroups) {
            ids.add(group.getId());
        }
        return th.getExecutors(th.getAdminUser(), ids);
    }

    private List<Executor> getAdditionalGroupsMixed() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            ids.add(executor.getId());
        }
        return th.getExecutors(th.getAdminUser(), ids);
    }

    private Group getAdditionalGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), additionalGroup.getId());
    }

    private Actor getAdditionalActor() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), additionalActor.getId());
    }

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalGroups = th.createGroupArray("additionalG", "Additional Group");

        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        additionalActors = th.createActorArray("additionalA", "Additional Actor");

        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformer(readPermissions, additionalActor);
        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActors);

        th.setPermissionsToAuthorizedPerformer(readlistPermissions, additionalGroup);
        th.setPermissionsToAuthorizedPerformerOnExecutors(readlistPermissions, additionalGroups);

        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActorGroupsMixed);

        super.setUp();
    }

    public void testAddActorsToGroupByAuthorizedPerformer() throws Exception {
        assertFalse("Executors not added to group ", th.isExecutorsInGroup(additionalActors, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalActors), additionalGroup.getId());
            fail("Executors added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalActors), additionalGroup.getId());

        assertTrue("Executors not added to group ", th.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));
    }

    public void testAddGroupsToGroupByAuthorizedPerformer() throws Exception {
        assertFalse("Executors not added to group ", th.isExecutorsInGroup(additionalGroups, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalGroups), additionalGroup.getId());
            fail("Executors added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalGroups), getAdditionalGroup().getId());

        assertTrue("Executors not added to group ", th.isExecutorsInGroup(getAdditionalGroups(), getAdditionalGroup()));
    }

    public void testAddMixedActorsGroupsToGroupByAuthorizedPerformer() throws Exception {
        assertFalse("Executors not added to group ", th.isExecutorsInGroup(additionalActorGroupsMixed, additionalGroup));
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalActorGroupsMixed), additionalGroup.getId());
            fail("Executors added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalActorGroupsMixed), getAdditionalGroup().getId());
        } catch (AuthorizationException e) {
        }
        assertTrue("Executors not added to group ", th.isExecutorsInGroup(getAdditionalGroupsMixed(), getAdditionalGroup()));
    }

    public void testAddActorToGroupsByAuthorizedPerformer() throws Exception {
        assertFalse("Executor not added to groups ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
        Executor executor = getAdditionalActor();
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor added to groups without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));

        assertTrue("Executor not added to groups ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testAddGroupToGroupsByAuthorizedPerformer() throws Exception {

        assertFalse("Executor not added to groups ", th.isExecutorInGroups(additionalGroup, additionalGroups));
        Executor executor = additionalGroup;
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(additionalGroups));
            fail("Executor added to groups without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);

        executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));

        assertTrue("Executor not added to groups ", th.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testAddExecutorsToGroupByUnAuthorizedPerformer() throws Exception {
        try {
            executorService.addExecutorsToGroup(th.getUnauthorizedPerformerUser(), th.toIds(additionalActorGroupsMixed), additionalGroup.getId());
            assertTrue("Executors not added to group ", th.isExecutorsInGroup(additionalActorGroupsMixed, additionalGroup));
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorToGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = additionalActor;
        try {
            executorService.addExecutorToGroups(th.getUnauthorizedPerformerUser(), executor.getId(), th.toIds(additionalGroups));
            assertTrue("Executor not added to groups ", th.isExecutorInGroups(additionalActor, additionalGroups));
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddFakeExecutorsToGroup() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);

        List<Executor> fakeExecutors = Lists.newArrayList(th.getFakeActor(), th.getFakeGroup());
        try {
            executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(fakeExecutors), additionalGroup.getId());
            fail("Executors added to group");
        } catch (IllegalArgumentException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
        }
    }

    public void testAddFakeExecutorToGroups() throws Exception {
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);
        Executor fakeExecutor = th.getFakeActor();
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), fakeExecutor.getId(), th.toIds(additionalGroups));
            fail("Executor added to groups ");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testAddNullExecutorToGroups() throws Exception {
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);
        try {
            executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), null, th.toIds(additionalGroups));
            fail("Null Executor added to groups ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorsToGroupWithNullSubject() throws Exception {
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, additionalGroup);
        try {
            executorService.addExecutorsToGroup(null, th.toIds(additionalActorGroupsMixed), additionalGroup.getId());
            fail("Executors added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testAddExecutorToGroupsWithNullSubject() throws Exception {
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupPermissions, additionalGroups);
        Executor executor = additionalActor;
        try {
            executorService.addExecutorToGroups(null, executor.getId(), th.toIds(additionalGroups));
            fail("Executor added to group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    @Override
    protected void tearDown() throws Exception {

        th.releaseResources();
        executorService = null;

        additionalActor = null;
        additionalGroup = null;
        additionalGroups = null;
        additionalActors = null;
        additionalActorGroupsMixed = null;
        super.tearDown();
    }

}
