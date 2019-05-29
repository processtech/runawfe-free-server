package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.List;
import java.util.Map;

public class ExecutorServiceDelegateGetActorTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetActorTest.class.getName();

    private Group group;

    private Actor actor;

    private Map<String, Executor> executorsMap;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
        executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(readPermissions, group);
        th.setPermissionsToAuthorizedPerformer(readPermissions, (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME));
        super.setUp();
    }

    public void testGetExecutorByNameByAuthorizedPerformer() throws Exception {
        Actor returnedBaseGroupActor = executorService.getExecutorByName(th.getAuthorizedPerformerUser(),
                testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", actor, returnedBaseGroupActor);
        Actor returnedSubGroupActor = executorService.getExecutorByName(th.getAuthorizedPerformerUser(),
                testPrefix + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        Actor subGroupActor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", subGroupActor, returnedSubGroupActor);
    }

    public void testGetExecutorByNameByUnauthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getUnauthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutorByName() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
        try {
            executorService.getExecutorByName(th.getUnauthorizedPerformerUser(), testPrefix + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutorByName() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // That's what we expect
        }
    }

    public void testGetUnexistedActorByAuthorizedPerformer() throws Exception {
        try {
            executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + "unexistent actor name");
            fail("businessDelegate does not throw Exception to getExecutorByName() to performer without Permission.READ");
        } catch (ExecutorDoesNotExistException e) {
            // That's what we expect
        }
    }

    public void testGetExecutorByNameInsteadOfGroup() throws Exception {
        try {
            Actor actor = executorService.<Actor>getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName() where the group really is returned.");
        } catch (ClassCastException e) {
            // That's what we expect
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        actor = null;
        group = null;
        super.tearDown();
    }
}
