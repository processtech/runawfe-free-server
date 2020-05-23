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

public class ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group additionalGroup;
    private List<Actor> additionalActors;

    private final Collection<Permission> updatePermissions = Lists.newArrayList(Permission.UPDATE);
    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    @Override
    protected void setUp() {
        executorService = Delegates.getExecutorService();
        h = new ServiceTestHelper(getClass().getName());

        additionalGroup = h.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActors = h.createActorArray("additionalMixed", "Additional Mixed");

        h.setPermissionsToAuthorizedActor(updatePermissions, additionalGroup);
        h.setPermissionsToAuthorizedActor(updatePermissions, additionalActors);

        executorService.addExecutorsToGroup(h.getAuthorizedUser(), h.toIds(additionalActors), additionalGroup.getId());
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        additionalGroup = null;
        additionalActors = null;
    }


    private List<Executor> getAdditionalActors() {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActors) {
            ids.add(executor.getId());
        }
        return h.getExecutors(h.getAdminUser(), ids);
    }

    private Group getAdditionalGroup() {
        return executorService.getExecutor(h.getAuthorizedUser(), additionalGroup.getId());
    }

    public void testRemoveExecutorsFromGroupByAuthorizedUser() {
        assertTrue("Executor is not in group before removing", h.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));

        h.setPermissionsToAuthorizedActor(readPermissions, getAdditionalGroup());
        List<Executor> executors = getAdditionalActors();
        try {
            executorService.removeExecutorsFromGroup(h.getAuthorizedUser(), h.toIds(executors), getAdditionalGroup().getId());
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // Expected.
        }

        h.setPermissionsToAuthorizedActor(updatePermissions, getAdditionalGroup());
        executorService.removeExecutorsFromGroup(h.getAuthorizedUser(), h.toIds(getAdditionalActors()), getAdditionalGroup().getId());
        assertFalse("Executor not removed from group ", h.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));
    }

    public void testRemoveExecutorsFromGroupByUnauthorizedUser() {
        List<Executor> executors = getAdditionalActors();
        try {
            executorService.removeExecutorsFromGroup(h.getUnauthorizedUser(), h.toIds(executors), getAdditionalGroup().getId());
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
