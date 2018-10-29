package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
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

/*
 */
public class ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveExecutorFromManyGroupsTest.class.getName();

    private long additionalGroupId;
    private long additionalActorId;

    private List<Long> additionalGroupsIds;

    private final Collection<Permission> addToGroupReadListPermissions = Lists.newArrayList(Permission.READ, GroupPermission.LIST_GROUP, GroupPermission.ADD_TO_GROUP);

    private final Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);

    private final Collection<Permission> removeFromGroupReadPermissions = Lists.newArrayList(Permission.READ, GroupPermission.REMOVE_FROM_GROUP);

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        Actor additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        Group additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        List<Group> additionalGroups = th.createGroupArray("additionalGroups", "Additional Groups");

        additionalActorId = additionalActor.getId();
        additionalGroupId = additionalGroup.getId();
        additionalGroupsIds = Lists.newArrayList();
        for (Group group : additionalGroups) {
            additionalGroupsIds.add(group.getId());
        }

        th.setPermissionsToAuthorizedPerformer(readPermissions, getAdditionalActor());
        th.setPermissionsToAuthorizedPerformer(readPermissions, getAdditionalGroup());
        th.setPermissionsToAuthorizedPerformerOnExecutors(addToGroupReadListPermissions, getAdditionalGroups());

        executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), getAdditionalActor().getId(), th.toIds(getAdditionalGroups()));
        executorService.addExecutorToGroups(th.getAuthorizedPerformerUser(), getAdditionalGroup().getId(), th.toIds(getAdditionalGroups()));
        super.setUp();
    }

    private List<Group> getAdditionalGroups() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return th.getExecutors(th.getAdminUser(), additionalGroupsIds);
    }

    private Actor getAdditionalActor() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), additionalActorId);
    }

    private Group getAdditionalGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException,
            ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), additionalGroupId);
    }

    public void testRemoveActorFromGroupsByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in groups before removing", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(removeFromGroupReadPermissions, getAdditionalGroups());

        executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));

        assertFalse("Executor not removed from group ", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));
    }

    public void testRemoveGrouprFromGroupsByAuthorizedPerformer() throws Exception {

        assertTrue("Executor is not in groups before removing", th.isExecutorInGroups(getAdditionalActor(), getAdditionalGroups()));

        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executors removed from group without corresponding permissions");
        } catch (AuthorizationException e) {
            // this is supposed result
        }

        th.setPermissionsToAuthorizedPerformerOnExecutors(removeFromGroupReadPermissions, getAdditionalGroups());

        executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));

        assertFalse("Executor not removed from group ", th.isExecutorInGroups(getAdditionalGroup(), getAdditionalGroups()));
    }

    public void testRemoveActorFromGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getUnauthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor is removed from groups ByUnAuthorizedPerformer");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveGroupFromGroupsByUnAuthorizedPerformer() throws Exception {
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(th.getUnauthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor is removed from groups ByUnAuthorizedPerformer");
        } catch (AuthorizationException e) {
            // this is supposed result
        }
    }

    public void testRemoveFakeActorFromGroups() throws Exception {
        Executor executor = th.getFakeActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("FakeExecutor removed from groups ");
        } catch (AuthorizationException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
            fail ("TODO trap");
        }
    }

    public void testRemoveFakeGroupFromGroups() throws Exception {
        Executor executor = th.getFakeGroup();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), th.toIds(getAdditionalGroups()));
            fail("FakeExecutor removed from groups");
        } catch (IllegalArgumentException e) {
            // TODO
        } catch (ExecutorDoesNotExistException e) {
            // this is supposed result
            fail ("TODO trap");
        }
    }

    public void testRemoveNullExecutorFromGroups() throws Exception {
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), null, th.toIds(getAdditionalGroups()));
            fail("NullExecutor removed from groups ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveExecutorFromNullGroups() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(th.getAuthorizedPerformerUser(), executor.getId(), null);
            fail("Executor removed from Null groups ");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveActorWithNullSubjectFromGroups() throws Exception {
        Executor executor = getAdditionalActor();
        try {
            executorService.removeExecutorFromGroups(null, executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor removed from groups with null subject");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    public void testRemoveGroupWithNullSubjectFromGroups() throws Exception {
        Executor executor = getAdditionalGroup();
        try {
            executorService.removeExecutorFromGroups(null, executor.getId(), th.toIds(getAdditionalGroups()));
            fail("Executor removed from groups with null subject");
        } catch (IllegalArgumentException e) {
            // this is supposed result
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        super.tearDown();
    }
}
