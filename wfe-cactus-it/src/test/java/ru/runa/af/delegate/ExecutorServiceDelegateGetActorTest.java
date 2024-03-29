package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.Map;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

public class ExecutorServiceDelegateGetActorTest extends ServletTestCase {
    private final String PREFIX = getClass().getName();
    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Group group;
    private Actor actor;
    private Map<String, Executor> executorsMap;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(PREFIX);
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        executorsMap = h.getDefaultExecutorsMap();
        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);

        val readPermissions = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(readPermissions, actor);
        h.setPermissionsToAuthorizedActor(readPermissions, group);
        h.setPermissionsToAuthorizedActor(readPermissions, executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME));
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        actor = null;
        group = null;
    }

    public void testGetExecutorByNameByAuthorizedUser() {
        Actor returnedBaseGroupActor = executorService.getExecutorByName(h.getAuthorizedUser(),
                PREFIX + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", actor, returnedBaseGroupActor);
        Actor returnedSubGroupActor = executorService.getExecutorByName(h.getAuthorizedUser(),
                PREFIX + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        Actor subGroupActor = (Actor) executorsMap.get(ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
        assertEquals("actor retuned by businessDelegate differes with expected", subGroupActor, returnedSubGroupActor);
    }

    public void testGetExecutorByNameByUnauthorizedUser() {
        try {
            executorService.getExecutorByName(h.getUnauthorizedUser(), PREFIX + ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutorByName() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // Expected.
        }
        try {
            executorService.getExecutorByName(h.getUnauthorizedUser(), PREFIX + ServiceTestHelper.SUB_GROUP_ACTOR_NAME);
            fail("businessDelegate allow to getExecutorByName() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetUnexistedActorByAuthorizedUser() {
        try {
            executorService.getExecutorByName(h.getAuthorizedUser(), PREFIX + "unexistent actor name");
            fail("businessDelegate does not throw Exception to getExecutorByName() to performer without Permission.READ");
        } catch (ExecutorDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetExecutorByNameInsteadOfGroup() {
        try {
            Actor actor = executorService.<Actor>getExecutorByName(h.getAuthorizedUser(), PREFIX + ServiceTestHelper.BASE_GROUP_NAME);
            fail("businessDelegate allow to getExecutorByName() where the group really is returned.");
        } catch (ClassCastException e) {
            // Expected.
        }
    }
}
