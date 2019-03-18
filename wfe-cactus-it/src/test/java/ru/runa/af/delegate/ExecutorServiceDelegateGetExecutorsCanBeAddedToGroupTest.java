package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
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
import ru.runa.wfe.user.User;

public class ExecutorServiceDelegateGetExecutorsCanBeAddedToGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetExecutorsCanBeAddedToGroupTest.class.getName();

    private Group group;

    private Group subGroup;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> updatePermission = Lists.newArrayList(Permission.UPDATE);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(updatePermission, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(updatePermission, group);
        th.setPermissionsToAuthorizedPerformer(updatePermission, subGroup);
        super.setUp();
    }

    private Group getSubGroup() throws InternalApplicationException, AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return executorService.getExecutor(th.getAdminUser(), subGroup.getId());
    }

    final public void testGetExecutorsByAuthorizedPerformer1() throws Exception {
        List<Executor> calculatedExecutors = executorService.getGroupChildren(th.getAuthorizedPerformerUser(), getSubGroup(),
                th.getExecutorBatchPresentation(), true);
        List<Executor> realExecutors = Lists.newArrayList((Executor) group);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realExecutors, calculatedExecutors);
    }

    final public void testGetExecutorsByAuthorizedPerformer2() throws Exception {
        Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getBaseGroupActor());
        List<Executor> calculatedExecutors = executorService.getGroupChildren(th.getAuthorizedPerformerUser(), getSubGroup(),
                th.getExecutorBatchPresentation(), true);
        List<Executor> realExecutors = Lists.newArrayList(group, th.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors ...() returns wrong group set", realExecutors, calculatedExecutors);
    }

    public void testGetExecutorGroupsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getGroupChildren(th.getUnauthorizedPerformerUser(), getSubGroup(), th.getExecutorBatchPresentation(), true);
            fail("businessDelegate.getExecutorsByUnauthorizedPerformer() no AuthorizationFailedException");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupswithoutPermission() throws Exception {
        try {
            Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
            th.setPermissionsToAuthorizedPerformer(readPermissions, getSubGroup());
            executorService.getGroupChildren(th.getAuthorizedPerformerUser(), getSubGroup(), th.getExecutorBatchPresentation(), true);
            fail("testGetExecutorswithoutPermission no Exception");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupswithFakeSubject() throws Exception {
        try {
            User fakeUser = th.getFakeUser();
            executorService.getGroupChildren(fakeUser, getSubGroup(), th.getExecutorBatchPresentation(), true);
            fail("testGetExecutorswithoutPermission no Exception");
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
