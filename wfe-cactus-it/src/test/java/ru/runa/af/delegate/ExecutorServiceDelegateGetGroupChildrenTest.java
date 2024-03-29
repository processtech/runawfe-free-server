package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.List;
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

public class ExecutorServiceDelegateGetGroupChildrenTest extends ServletTestCase {
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

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, actor);
        h.setPermissionsToAuthorizedActor(pp, group);
        h.setPermissionsToAuthorizedActor(pp, subGroup);

        actor = executorService.getExecutor(h.getAdminUser(), actor.getId());
        group = executorService.getExecutor(h.getAdminUser(), group.getId());
        subGroup = executorService.getExecutor(h.getAdminUser(), subGroup.getId());
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

    final public void testGetGroupChildrenByAuthorizedUser() {
        List<Executor> calculatedGroupChildren = executorService.getGroupChildren(h.getAuthorizedUser(), group,
                h.getExecutorBatchPresentation(), false);
        List<Executor> realGroupChildren = Lists.newArrayList(h.getBaseGroupActor(), h.getSubGroup());
        ArrayAssert.assertWeakEqualArrays("businessDelegate.getExecutorGroups() returns wrong group set", realGroupChildren, calculatedGroupChildren);
    }

    public void testGetExecutorGroupsByUnauthorizedUser() {
        try {
            executorService.getGroupChildren(h.getUnauthorizedUser(), group, h.getExecutorBatchPresentation(), false);
            fail();
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithoutPermission() {
        try {
            h.setPermissionsToAuthorizedActor(Lists.newArrayList(), group);
            executorService.getGroupChildren(h.getAuthorizedUser(), group, h.getExecutorBatchPresentation(), false);
            fail("testGetGroupChildrenwithoutPermission no Exception");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorGroupsWithFakeUser() {
        try {
            executorService.getGroupChildren(h.getFakeUser(), group, h.getExecutorBatchPresentation(), false);
            fail();
        } catch (AuthenticationException e) {
            // That's what we expect
        }
    }
}
