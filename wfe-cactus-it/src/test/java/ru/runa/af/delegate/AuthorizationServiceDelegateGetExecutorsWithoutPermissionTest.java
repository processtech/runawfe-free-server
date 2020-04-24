package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.08.2004.
 */
public class AuthorizationServiceDelegateGetExecutorsWithoutPermissionTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;
    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(AuthorizationServiceDelegateGetExecutorsWithoutPermissionTest.class.getName());
        authorizationService = Delegates.getAuthorizationService();

        batchPresentation = h.getExecutorBatchPresentation();

        h.createDefaultExecutorsMap();

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroup());
        authorizationService.setPermissions(h.getAdminUser(), h.getBaseGroupActor().getId(), pp, h.getBaseGroupActor());
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
    }

    public void testGetExecutorsWithoutPermissionFakeUser() {
        try {
            authorizationService.getExecutorsWithPermission(h.getFakeUser(), h.getBaseGroupActor(), batchPresentation, false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected. Although we also can get here because user is not fake but is not allowed to read object's permissions.
        }
    }

    public void testGetExecutorsWithoutPermissionFakeObject() {
        try {
            authorizationService.getExecutorsWithPermission(h.getAuthorizedUser(), h.getFakeActor(), batchPresentation, false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            // Expected.
        }
    }

    public void testGetExecutorsWithoutPermission() {
        val expected = Lists.newArrayList(h.getBaseGroup());
        val actual = authorizationService.getExecutorsWithPermission(h.getAuthorizedUser(), h.getBaseGroupActor(), batchPresentation, false);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getExecutorsWithoutPermission() returns wrong executors", expected, actual);
    }

    public void testGetExecutorsWithoutPermissionUnauthorized() {
        try {
            authorizationService.getExecutorsWithPermission(h.getUnauthorizedUser(), h.getBaseGroupActor(), batchPresentation, false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
