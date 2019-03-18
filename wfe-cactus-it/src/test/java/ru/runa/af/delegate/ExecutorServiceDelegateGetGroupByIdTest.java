package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

import java.util.List;
import java.util.Map;

public class ExecutorServiceDelegateGetGroupByIdTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetGroupByIdTest.class.getName();

    private Group group;

    private Map<String, Executor> executorsMap;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getSubGroup());

        th.setPermissionsToAuthorizedPerformer(readPermissions, th.getBaseGroupActor());
        super.setUp();
    }

    public void testGetExecutorByAuthorizedPerformer() throws Exception {
        Group returnedBaseGroup = executorService.getExecutor(th.getAuthorizedPerformerUser(), group.getId());
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
        Group subGroup = (Group) executorsMap.get(ServiceTestHelper.SUB_GROUP_NAME);
        Group returnedSubGroup = executorService.getExecutor(th.getAuthorizedPerformerUser(), subGroup.getId());
        assertEquals("actor retuned by businessDelegate differes with expected", subGroup, returnedSubGroup);
    }

    public void testGetExecutorByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getExecutor(th.getUnauthorizedPerformerUser(), group.getId());
            fail("businessDelegate allow to getExecutor()");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
        try {
            executorService.getExecutor(th.getUnauthorizedPerformerUser(), th.getSubGroup().getId());
            fail("businessDelegate allow to getSubGroup()");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetUnexistedGroupByAuthorizedPerformer() throws Exception {
        try {
            executorService.getExecutor(th.getAuthorizedPerformerUser(), -1l);
            fail("businessDelegate does not throw Exception to getExecutor() in testGetUnexistedGroupByAuthorizedPerformer");
        } catch (ExecutorDoesNotExistException e) {
            // That's what we expect
        }
    }

    public void testGetActorInsteadOfGroup() throws Exception {
        try {
            Group actor = executorService.<Group>getExecutor(th.getAuthorizedPerformerUser(), th.getBaseGroupActor().getId());
            fail("businessDelegate allow to getExecutor() where the actor really is returned.");
        } catch (ClassCastException e) {
            // That's what we expect
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        group = null;
        executorsMap = null;
        super.tearDown();
    }
}
