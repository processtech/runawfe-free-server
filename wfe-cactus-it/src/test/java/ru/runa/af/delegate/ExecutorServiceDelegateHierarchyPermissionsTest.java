package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

import java.util.Collection;
import java.util.Map;

public class ExecutorServiceDelegateHierarchyPermissionsTest extends ServletTestCase {

    private static final String ACTOR_PWD = "ActorPWD";

    private ServiceTestHelper th;

    private ExecutorService executorService;

    private AuthorizationService authorizationService;

    private static String testPrefix = ExecutorServiceDelegateHierarchyPermissionsTest.class.getName();

    private Actor actor;

    private Group group;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        authorizationService = Delegates.getAuthorizationService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        Collection<Permission> updatePermissions = Lists.newArrayList(Permission.READ, ExecutorPermission.UPDATE);
        Collection<Permission> loginPermissions = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
        Collection<Permission> createExecutorPermissions = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR);
        Collection<Permission> addToGroupPermissions = Lists.newArrayList(Permission.READ, GroupPermission.ADD_TO_GROUP);
        Collection<Permission> grantAASystem = Lists.newArrayList(Permission.UPDATE_PERMISSIONS);

        Map<String, Executor> executorsMap = th.getDefaultExecutorsMap();

        actor = (Actor) executorsMap.get(ServiceTestHelper.BASE_GROUP_ACTOR_NAME);
        th.setPermissionsToAuthorizedPerformer(updatePermissions, actor);
        group = (Group) executorsMap.get(ServiceTestHelper.BASE_GROUP_NAME);
        th.setPermissionsToAuthorizedPerformer(addToGroupPermissions, group);

        th.setPermissionsToAuthorizedPerformerOnSystem(grantAASystem);

        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), actor.getId(), loginPermissions, ASystem.INSTANCE);
        group = executorService.getExecutor(th.getAdminUser(), group.getId());
        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), group.getId(), createExecutorPermissions, ASystem.INSTANCE);

        actor = executorService.getExecutor(th.getAdminUser(), actor.getId());
        executorService.setPassword(th.getAuthorizedPerformerUser(), actor, ACTOR_PWD);

        super.setUp();
    }

    public void testPermissionsInheritance() throws Exception {
        User additionalUser = Delegates.getAuthenticationService().authenticateByLoginPassword(actor.getName(), ACTOR_PWD);

        if (!authorizationService.isAllowed(additionalUser, SystemPermission.CREATE_EXECUTOR, ASystem.INSTANCE)) {
            assertTrue("unproper createExecutor permission ", false);
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        authorizationService = null;
        actor = null;
        group = null;

        super.tearDown();
    }

}
