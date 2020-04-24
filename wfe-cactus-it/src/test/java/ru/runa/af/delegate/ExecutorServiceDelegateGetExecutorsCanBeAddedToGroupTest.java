package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Map;
import lombok.val;
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
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class ExecutorServiceDelegateGetExecutorsCanBeAddedToGroupTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Group subGroup;
    private Actor actor;
    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        executorsMap = h.getDefaultExecutorsMap();
        actor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);

        val pp = Lists.newArrayList(Permission.UPDATE);
        h.setPermissionsToAuthorizedActor(pp, actor);
        h.setPermissionsToAuthorizedActor(pp, group);
        h.setPermissionsToAuthorizedActor(pp, subGroup);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        h = null;
        executorsMap = null;
        executorService = null;
        actor = null;
        group = null;
        subGroup = null;
    }

    private Group getSubGroup() {
        return executorService.getExecutor(h.getAdminUser(), subGroup.getId());
    }

    final public void testGetExecutorsByAuthorizedUser1() {
        val actual = executorService.getGroupChildren(h.getAuthorizedUser(), getSubGroup(), h.getExecutorBatchPresentation(), true);
        val expected = Lists.newArrayList((Executor) group);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", expected, actual);
    }

    final public void testGetExecutorsByAuthorizedUser2() {
        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.UPDATE), h.getBaseGroupActor());
        val actual = executorService.getGroupChildren(h.getAuthorizedUser(), getSubGroup(), h.getExecutorBatchPresentation(), true);
        val expected = Lists.newArrayList(group, h.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutors ...() returns wrong group set", expected, actual);
    }

    public void testGetExecutorGroupsByUnauthorizedUser() {
        try {
            executorService.getGroupChildren(h.getUnauthorizedUser(), getSubGroup(), h.getExecutorBatchPresentation(), true);
            fail();
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupswithoutPermission() {
        try {
            Collection<Permission> readPermissions = Lists.newArrayList(Permission.READ);
            h.setPermissionsToAuthorizedActor(readPermissions, getSubGroup());
            executorService.getGroupChildren(h.getAuthorizedUser(), getSubGroup(), h.getExecutorBatchPresentation(), true);
            fail("testGetExecutorswithoutPermission no Exception");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetExecutorGroupswithFakeUser() {
        try {
            User fakeUser = h.getFakeUser();
            executorService.getGroupChildren(fakeUser, getSubGroup(), h.getExecutorBatchPresentation(), true);
            fail("testGetExecutorswithoutPermission no Exception");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
