package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.Collection;
import java.util.List;

public class ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveManyExecutorsFromGroupTest.class.getName();

    private Group additionalGroup;

    private List<Actor> additionalActors;

    private final Collection<Permission> addToGroupReadListPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> removeFromGroupReadPermissions = Lists.newArrayList(Permission.READ, GroupPermission.REMOVE_FROM_GROUP);

    private List<Executor> getAdditionalActors() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        List<Long> ids = Lists.newArrayList();
        for (Executor executor : additionalActors) {
            ids.add(executor.getId());
        }
        return th.getExecutors(th.getAdminUser(), ids);
    }

    private Group getAdditionalGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAuthorizedPerformerUser(), additionalGroup.getId());
    }

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActors = th.createActorArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformer(addToGroupReadListPermissions, additionalGroup);
        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActors);

        executorService.addExecutorsToGroup(th.getAuthorizedPerformerUser(), th.toIds(additionalActors), additionalGroup.getId());

        super.setUp();
    }

    public void testRemoveExecutorsFromGroupByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in group before removing", th.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));

        List<Executor> executors = getAdditionalActors();
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), th.toIds(executors), getAdditionalGroup().getId());
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, getAdditionalGroup());

        executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), th.toIds(getAdditionalActors()), getAdditionalGroup().getId());

        assertFalse("Executor not removed from group ", th.isExecutorsInGroup(getAdditionalActors(), getAdditionalGroup()));
    }

    public void testRemoveExecutorsFromGroupByUnAuthorizedPerformer() throws Exception {
        List<Executor> executors = getAdditionalActors();
        try {
            executorService.removeExecutorsFromGroup(th.getUnauthorizedPerformerUser(), th.toIds(executors), getAdditionalGroup().getId());
            fail("Executors is removed from group ByUnAuthorizedPerformer");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, getAdditionalGroup());
        List<Executor> executors = th.getFakeExecutors();
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), th.toIds(executors), getAdditionalGroup().getId());
            fail("FakeExecutors removed from group ");
        } catch(IllegalArgumentException e) {
            // TDOO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
            fail ("TODO trap");
        }
    }

    public void testRemoveNullExecutor() throws Exception {
        th.setPermissionsToAuthorizedPerformer(removeFromGroupReadPermissions, getAdditionalGroup());
        List<Long> executors = Lists.newArrayList(null, null, null);
        try {
            executorService.removeExecutorsFromGroup(th.getAuthorizedPerformerUser(), executors, getAdditionalGroup().getId());
            fail("NullExecutors removed from group ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveExecutorsWithNullSubject() throws Exception {
        try {
            executorService.removeExecutorsFromGroup(null, th.toIds(getAdditionalActors()), getAdditionalGroup().getId());
            fail("Executors removed from group with null subject");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalGroup = null;
        additionalActors = null;
        super.tearDown();
    }
}
