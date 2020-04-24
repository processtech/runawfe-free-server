package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/*
 */
public class ExecutorServiceDelegateAddManyExecutorsToGroupsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group additionalGroup;
    private List<Group> additionalGroups;
    private Actor additionalActor;
    private List<Actor> additionalActors;
    private List<Executor> additionalActorGroupsMixed;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);
    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        additionalGroup = h.createGroupIfNotExist("additionalG", "Additional Group");
        additionalGroups = h.createGroupArray("additionalG", "Additional Group");

        additionalActor = h.createActorIfNotExist("additionalA", "Additional Actor");
        additionalActors = h.createActorArray("additionalA", "Additional Actor");

        additionalActorGroupsMixed = h.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");

        h.setPermissionsToAuthorizedActor(readPermissions, additionalActor);
        h.setPermissionsToAuthorizedActor(readPermissions, additionalActors);

        h.setPermissionsToAuthorizedActor(readPermissions, additionalGroup);
        h.setPermissionsToAuthorizedActor(readPermissions, additionalGroups);

        h.setPermissionsToAuthorizedActor(readPermissions, additionalActorGroupsMixed);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        additionalActor = null;
        additionalGroup = null;
        additionalGroups = null;
        additionalActors = null;
        additionalActorGroupsMixed = null;
    }

    private List<Actor> getAdditionalActors() {
        List<Long> ids = Lists.newArrayList();
        for (Actor actor : additionalActors) {
            ids.add(actor.getId());
        }
        return h.getExecutors(h.getAdminUser(), ids);
    }

    private List<Group> getAdditionalGroups() {
        List<Long> ids = Lists.newArrayList();
        for (Group group : additionalGroups) {
            ids.add(group.getId());
        }
        return h.getExecutors(h.getAdminUser(), ids);
    }

    private List<Executor> getAdditionalGroupsMixed() {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            ids.add(executor.getId());
        }
        return h.getExecutors(h.getAdminUser(), ids);
    }

    private Group getAdditionalGroup() {
        return executorService.getExecutor(h.getAdminUser(), additionalGroup.getId());
    }

    private Actor getAdditionalActor() {
        return executorService.getExecutor(h.getAdminUser(), additionalActor.getId());
    }

    public void testAddActorsToGroupByAuthorizedUser() {
        assertFalse("Executors not added to group ", h.isExecutorsInGroup(additionalActors, additionalGroup));
        try {
            executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalActors), additionalGroup.getId());
            fail("Executors added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroup);
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalActors);

        executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalActors), additionalGroup.getId());

        assertTrue("Executors not added to group ", h.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));
    }

    public void testAddGroupsToGroupByAuthorizedUser() {
        assertFalse("Executors not added to group ", h.isExecutorsInGroup(additionalGroups, additionalGroup));
        try {
            executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalGroups), additionalGroup.getId());
            fail("Executors added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroup);
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroups);

        executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalGroups), getAdditionalGroup().getId());

        assertTrue("Executors not added to group ", h.isExecutorsInGroup(getAdditionalGroups(), getAdditionalGroup()));
    }

    public void testAddMixedActorsGroupsToGroupByAuthorizedUser() {
        assertFalse("Executors not added to group ", h.isExecutorsInGroup(additionalActorGroupsMixed, additionalGroup));
        try {
            executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalActorGroupsMixed), additionalGroup.getId());
            fail("Executors added to group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroup);
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalActorGroupsMixed);
        try {
            executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalActorGroupsMixed), getAdditionalGroup().getId());
        } catch (AuthorizationException e) {
            // Expected.
        }
        assertTrue("Executors not added to group ", h.isExecutorsInGroup(getAdditionalGroupsMixed(), getAdditionalGroup()));
    }

    public void testAddActorToGroupsByAuthorizedUser() {
        assertFalse("Executor not added to groups ", h.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
        Executor executor = getAdditionalActor();
        try {
            executorService.addExecutorToGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
            fail("Executor added to groups without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, executor);
        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
        assertTrue("Executor not added to groups ", h.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testAddGroupToGroupsByAuthorizedUser() {

        assertFalse("Executor not added to groups ", h.isExecutorInGroups(additionalGroup, additionalGroups));
        Executor executor = additionalGroup;
        try {
            executorService.addExecutorToGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(additionalGroups));
            fail("Executor added to groups without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroups);
        h.setPermissionsToAuthorizedActor(updatePermissions, executor);

        executorService.addExecutorToGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));

        assertTrue("Executor not added to groups ", h.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testAddExecutorsToGroupByUnAuthorizedUser() {
        try {
            executorService.addExecutorsToGroup(h.getUnauthorizedUser(), h.toIds(additionalActorGroupsMixed), additionalGroup.getId());
            assertTrue("Executors not added to group ", h.isExecutorsInGroup(additionalActorGroupsMixed, additionalGroup));
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testAddExecutorToGroupsByUnAuthorizedUser() {
        Executor executor = additionalActor;
        try {
            executorService.addExecutorToGroups(h.getUnauthorizedUser(), executor.getId(), h.toIds(additionalGroups));
            assertTrue("Executor not added to groups ", h.isExecutorInGroups(additionalActor, additionalGroups));
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
