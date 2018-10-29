package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.List;
import java.util.Map;

public class ExecutorServiceDelegateGetExecutorTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateGetExecutorTest.class.getName();

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
        super.setUp();
    }

    public void testGetActorByAuthorizedPerformer() throws Exception {
        Actor returnedBaseGroupActor = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix
                + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", actor, returnedBaseGroupActor);
        Group returnedBaseGroup = executorService.getExecutorByName(th.getAuthorizedPerformerUser(), testPrefix
                + ServiceTestHelper.BASE_GROUP_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", group, returnedBaseGroup);
    }

    public void testGetExecutorByUnauthorizedPerformer() throws Exception {
        User unauthorizedPerformerUser = th.getUnauthorizedPerformerUser();
        try {
            executorService.getExecutorByName(unauthorizedPerformerUser, testPrefix + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
        try {
            executorService.getExecutorByName(unauthorizedPerformerUser, testPrefix + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistentExecutorByAuthorizedPerformer() throws Exception {
        User authorizedPerformerUser = th.getAuthorizedPerformerUser();
        try {
            executorService.getExecutorByName(authorizedPerformerUser, testPrefix + "unexistent actor name");
            fail("businessDelegate does not throw Exception to getExecutor() to performer without Permission.READ");
        } catch (ExecutorDoesNotExistException e) {
            //That's what we expect
        }
    }

    public void testGetNullExecutorByAuthorizedPerformer() throws Exception {
        User authorizedPerformerUser = th.getAuthorizedPerformerUser();
        try {
            executorService.getExecutorByName(authorizedPerformerUser, null);
            fail("businessDelegate allow to getExecutor()with null actor.");
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorActorByNullPerformer() throws Exception {
        try {
            executorService.getExecutorByName(null, actor.getName());
            fail("businessDelegate allow to getExecutor() (Actor really) to performer with null subject");
        } catch (IllegalArgumentException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorGroupByNullPerformer() throws Exception {
        try {
            executorService.getExecutorByName(null, group.getName());
            fail("businessDelegate allow to getExecutor() (Group really) to performer with null subject.");
        } catch (IllegalArgumentException e) {
            //That's what we expect
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
