package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * Sets some permission on Group and tests Group's member is allowed.
 */
public class ExecutorServiceDelegateHierarchyPermissionsTest extends ServletTestCase {
    private static final String ACTOR_PWD = "ActorPWD";

    private ServiceTestHelper h;
    private ExecutorService executorService;
    private AuthorizationService authorizationService;

    private Actor actor;
    private Group group;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(ExecutorServiceDelegateHierarchyPermissionsTest.class.getName());
        executorService = Delegates.getExecutorService();
        authorizationService = Delegates.getAuthorizationService();

        h.createDefaultExecutorsMap();
        actor = h.getBaseGroupActor();
        group = h.getBaseGroup();

        executorService.setPassword(h.getAdminUser(), actor, ACTOR_PWD);

        authorizationService.setPermissions(h.getAdminUser(), group.getId(), Lists.newArrayList(Permission.CREATE_EXECUTOR),
                SecuredSingleton.SYSTEM);
        authorizationService.setPermissions(h.getAdminUser(), actor.getId(), Lists.newArrayList(Permission.LOGIN),
                SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        authorizationService = null;
        actor = null;
        group = null;
    }

    public void testPermissionsInheritance() {
        User user = Delegates.getAuthenticationService().authenticateByLoginPassword(actor.getName(), ACTOR_PWD);
        assertTrue(authorizationService.isAllowed(user, Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM));
    }
}
