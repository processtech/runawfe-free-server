package ru.runa.af.delegate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateGetGroupChildrenTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupChildrenTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> readUpdateListPermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE, GroupPermission.LIST_GROUP);
        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readUpdateListPermissions, group);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, subGroup);

        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        group = executorService.getExecutor(th.getAdminUser(), group.getId());
        subGroup = executorService.getExecutor(th.getAdminUser(), subGroup.getId());

        super.setUp();
    }

    final public void testGetGroupChildrenByAuthorizedPerformer() throws Exception {
        List<Executor> calculatedGroupChildren = executorService.getGroupChildren(th.getAuthorizedPerformerUser(), group,
                th.getExecutorBatchPresentation(), false);
        List<Executor> realGroupChildren = Lists.newArrayList(th.getBaseGroupActor(), th.getSubGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroupChildren, calculatedGroupChildren);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroupChildren(th.getUnauthorizedPerformerUser(), group, th.getExecutorBatchPresentation(), false);
            fail("businessDelegate.getGroupChildrenByUnauthorizedPerformer() no AuthorizationFailedException");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithNullSubject() throws Exception {
        try {
            executorService.getGroupChildren(null, group, th.getExecutorBatchPresentation(), false);
            fail("GetGroupChildrenwithNullSubject no Exception");
        } catch (IllegalArgumentException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithoutPermission() throws Exception {
        try {
            Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
            th.setPermissionsToAuthorizedPerformer(readPermissions, group);
            executorService.getGroupChildren(th.getAuthorizedPerformerUser(), group, th.getExecutorBatchPresentation(), false);
            fail("testGetGroupChildrenwithoutPermission no Exception");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithFakeSubject() throws Exception {
        try {
            User fakeUser = th.getFakeUser();
            executorService.getGroupChildren(fakeUser, group, th.getExecutorBatchPresentation(), false);
            fail("testGetExecutorGroupsWithFakeSubject no Exception");
        } catch (AuthenticationException e) {
            // That's what we expect
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        executorsMap = null;
        executorService = null;
        actor = null;
        group = null;
        subGroup = null;
        super.tearDown();
    }
}
