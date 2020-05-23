package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.List;
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
import ru.runa.wfe.user.Group;

public class ExecutorServiceDelegateGetExecutorGroupsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Group subGroup;
    private Actor actor;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        actor = h.getSubGroupActor();
        group = h.getBaseGroup();
        subGroup = h.getSubGroup();

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, actor);
        h.setPermissionsToAuthorizedActor(pp, group);
        h.setPermissionsToAuthorizedActor(pp, subGroup);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        h = null;
        executorService = null;
        actor = null;
        group = null;
        subGroup = null;
    }

    final public void testGetExecutorGroupsByAuthorizedUser1() {
        List<Group> calculatedGroups = executorService.getExecutorGroups(h.getAuthorizedUser(), actor, h.getExecutorBatchPresentation(),
                false);
        List<Group> realGroups = Lists.newArrayList(subGroup);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups( actor ) returns wrong group set", realGroups, calculatedGroups);
    }

    final public void testGetExecutorGroupsByAuthorizedUser2() {
        List<Group> calculatedGroups = executorService.getExecutorGroups(h.getAuthorizedUser(), subGroup, h.getExecutorBatchPresentation(),
                false);
        List<Group> realGroups = Lists.newArrayList(group);
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups( group ) returns wrong group set", realGroups, calculatedGroups);
    }

    final public void testGetExecutorGroupsByAuthorizedUser3() {
        List<Permission> updatePermission = Lists.newArrayList(Permission.UPDATE);
        h.setPermissionsToAuthorizedActor(updatePermission, group);
        h.addExecutorToGroup(getActor(), getGroup());
        List<Group> calculatedGroups = executorService.getExecutorGroups(h.getAuthorizedUser(), getActor(),
                h.getExecutorBatchPresentation(), false);
        List<Group> realGroups = Lists.newArrayList(subGroup, getGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroups, calculatedGroups);
    }

    private Group getGroup() {
        return executorService.getExecutor(h.getAdminUser(), group.getId());
    }

    private Actor getActor() {
        return executorService.getExecutor(h.getAdminUser(), actor.getId());
    }

    final public void testGetExecutorGroupsByAuthorizedUser4() {
        List<Permission> updatePermission = Lists.newArrayList(Permission.UPDATE);
        List<Permission> noPermissionArray = Lists.newArrayList();
        h.setPermissionsToAuthorizedActor(updatePermission, group);
        h.addExecutorToGroup(getActor(), getGroup());
        h.setPermissionsToAuthorizedActor(noPermissionArray, subGroup);
        List<Group> calculatedGroups = executorService.getExecutorGroups(h.getAuthorizedUser(), getActor(),
                h.getExecutorBatchPresentation(), false);
        List<Group> realGroups = Lists.newArrayList(getGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroups, calculatedGroups);
    }

    public void testGetExecutorGroupsByUnauthorizedUser1() {
        try {
            executorService.getExecutorGroups(h.getUnauthorizedUser(), actor, h.getExecutorBatchPresentation(), false);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetExecutorGroupsByUnauthorizedUser2() {
        try {
            executorService.getExecutorGroups(h.getUnauthorizedUser(), subGroup, h.getExecutorBatchPresentation(), false);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetExecutorGroupsWithFakeUser() {
        try {
            executorService.getExecutorGroups(h.getFakeUser(), actor, h.getExecutorBatchPresentation(), false);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
