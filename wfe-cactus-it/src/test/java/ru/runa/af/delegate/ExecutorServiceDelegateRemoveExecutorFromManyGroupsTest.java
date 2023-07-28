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
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

/*
 */
public class ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private long additionalGroupId;
    private long additionalActorId;

    private List<Long> additionalGroupsIds;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);
    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        Actor additionalActor = h.createActorIfNotExist("additionalA", "Additional Actor");
        Group additionalGroup = h.createGroupIfNotExist("additionalG", "Additional Group");
        List<Group> additionalGroups = h.createGroupArray("additionalGroups", "Additional Groups");

        additionalActorId = additionalActor.getId();
        additionalGroupId = additionalGroup.getId();
        additionalGroupsIds = Lists.newArrayList();
        for (Group group : additionalGroups) {
            additionalGroupsIds.add(group.getId());
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalActor());
        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalGroup());
        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(h.getAuthorizedUser(), getAdditionalActor().getId(), h.toIds(getAdditionalGroups()));
        executorService.addExecutorToGroups(h.getAuthorizedUser(), getAdditionalGroup().getId(), h.toIds(getAdditionalGroups()));
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
    }

    private List<Group> getAdditionalGroups() {
        return h.getExecutors(h.getAdminUser(), additionalGroupsIds);
    }

    private Actor getAdditionalActor() {
        return executorService.getExecutor(h.getAdminUser(), additionalActorId);
    }

    private Group getAdditionalGroup() {
        return executorService.getExecutor(h.getAdminUser(), additionalGroupId);
    }

    public void testRemoveActorFromGroupsByAuthorizedUser() {

        assertTrue("Executor is not in groups before removing", h.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        h.setPermissionsToAuthorizedActor(readPermissions, getAdditionalGroups());
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalGroups());
        executorService.removeExecutorFromGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));

        assertFalse("Executor not removed from group ", h.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testRemoveGrouprFromGroupsByAuthorizedUser() {

        assertTrue("Executor is not in groups before removing", h.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        h.setPermissionsToAuthorizedActor(readPermissions, getAdditionalGroups());
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalGroups());
        executorService.removeExecutorFromGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));

        assertFalse("Executor not removed from group ", h.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testRemoveActorFromGroupsByUnAuthorizedUser() {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(h.getUnauthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testRemoveGroupFromGroupsByUnAuthorizedUser() {
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(h.getUnauthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testRemoveFakeActorFromGroups() {
        Executor executor = h.getFakeActor();
        try {
            executorService.removeExecutorFromGroups(h.getAuthorizedUser(), executor.getId(), h.toIds(getAdditionalGroups()));
            fail("FakeExecutor removed from groups ");
        } catch (AuthorizationException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
            fail("TODO trap");
        }
    }
}
