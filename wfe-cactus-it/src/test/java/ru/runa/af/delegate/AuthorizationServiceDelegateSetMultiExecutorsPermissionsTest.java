package ru.runa.af.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

/**
 * Created on 16.02.2005
 */
public class AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest extends ServletTestCase {
    private ServiceTestHelper th;
    private AuthorizationService authorizationService;

    Collection<Permission> testPermission = Lists.newArrayList(Permission.READ, Permission.UPDATE);

    private static String testPrefix = AuthorizationServiceDelegateSetMultiExecutorsPermissionsTest.class.getName();

    private Group additionalGroup;
    private Actor additionalActor;

    private List<Executor> additionalActorGroupsMixed;
    private List<Long> executorIDs;

    @Override
    protected void setUp() throws Exception {
        th = new ServiceTestHelper(testPrefix);

        Collection<Permission> readUpdateExecutorPermission = Lists.newArrayList(Permission.READ, Permission.UPDATE);

        authorizationService = Delegates.getAuthorizationService();

        additionalActor = th.createActorIfNotExist("additionalA", "Additional Actor");
        additionalGroup = th.createGroupIfNotExist("additionalG", "Additional Group");
        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("mixed", "Additional Mixed");
        executorIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorIDs.add(executor.getId());
            th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, executor);
        }

        th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, additionalActor);
        th.setPermissionsToAuthorizedPerformer(readUpdateExecutorPermission, additionalGroup);

        super.setUp();
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, testPermission, additionalActor);
        for (int i = 0; i < executorIDs.size(); i++) {
            additionalActorGroupsMixed.set(i, Delegates.getExecutorService().getExecutor(th.getAuthorizedPerformerUser(), executorIDs.get(i)));
        }
        for (int i = 0; i < executorIDs.size(); i++) {
            Collection<Permission> expected = authorizationService.getIssuedPermissions(th.getAuthorizedPerformerUser(),
                    additionalActorGroupsMixed.get(i), additionalActor);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", testPermission, expected);
        }

        authorizationService.setPermissions(th.getAuthorizedPerformerUser(), executorIDs, testPermission, additionalGroup);

        for (int i = 0; i < executorIDs.size(); i++) {
            additionalActorGroupsMixed.set(i, Delegates.getExecutorService().getExecutor(th.getAuthorizedPerformerUser(), executorIDs.get(i)));
        }

        for (int i = 0; i < executorIDs.size(); i++) {
            Collection<Permission> expected = authorizationService.getIssuedPermissions(th.getAuthorizedPerformerUser(),
                    additionalActorGroupsMixed.get(i), additionalGroup);
            ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", testPermission, expected);
        }
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(th.getFakeUser(), executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
            // This is what we expect
        }
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(th.getUnauthorizedPerformerUser(), executorIDs, testPermission, additionalActor);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // This is what we expect
        }

        try {
            authorizationService.setPermissions(th.getUnauthorizedPerformerUser(), executorIDs, testPermission, additionalGroup);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // This is what we expect
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

}
