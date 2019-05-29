package ru.runa.af.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 * 
 */
public class AuthorizationServiceDelegateGetExecutorsWithPermissionTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetExecutorsWithPermissionTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = Delegates.getAuthorizationService();
        batchPresentation = helper.getExecutorBatchPresentation();

        authorizationService.setPermissions(helper.getAdminUser(), helper.getBaseGroupActor().getId(), executorP, helper.getBaseGroupActor());
        authorizationService.setPermissions(helper.getAdminUser(), helper.getAuthorizedPerformerActor().getId(), executorP,
                helper.getAuthorizedPerformerActor());

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testGetExecutorsWithPermissionFakeSubject() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getFakeUser(), SecuredSingleton.EXECUTORS, batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetExecutorsWithPermissionFakeSecuredObject() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerUser(), helper.getFakeActor(), batchPresentation, true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
        }
    }

    public void testGetExecutorsWithPermission() throws Exception {
        List<Actor> expected = Lists.newArrayList(helper.getAuthorizedPerformerActor(), helper.getBaseGroupActor());
        List<Executor> actual = authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                batchPresentation, true);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getExecutorsWithPermission() returns wrong executors", expected, actual);
    }

    public void testGetExecutorsWithPermissionUnauthorized() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor(), batchPresentation,
                    true);
            fail("AuthorizationDelegate.getExecutorsWithPermission() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }

}
