package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import lombok.val;
import lombok.var;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.08.2004.
 */
public class AuthorizationServiceDelegateGetPermissionsTest extends ServletTestCase {
    private ServiceTestHelper h;
    private AuthorizationService authorizationService;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(AuthorizationServiceDelegateGetPermissionsTest.class.getName());
        authorizationService = Delegates.getAuthorizationService();

        h.createDefaultExecutorsMap();

        // authorizationService.getIssuedPermissions() requires READ on subject (executors)
        // and READ_PERMISSIONS (which is hidden and derived from READ) on object (other executors, system).
        val pp = Lists.newArrayList(Permission.READ);

        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroup());
        h.setPermissionsToAuthorizedActor(pp, SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        authorizationService = null;
    }

    public void testGetPermissionsFakeUser() {
        try {
            authorizationService.getIssuedPermissions(h.getFakeUser(), h.getBaseGroupActor(), h.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows fake subject");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetPermissions() {
        val empty = new ArrayList<Permission>();
        val expected = Lists.newArrayList(Permission.READ);

        var actual = authorizationService.getIssuedPermissions(h.getAuthorizedUser(), h.getBaseGroupActor(), SecuredSingleton.SYSTEM);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", empty, actual);

        authorizationService.setPermissions(h.getAdminUser(), h.getBaseGroupActor().getId(), expected, SecuredSingleton.SYSTEM);
        actual = authorizationService.getIssuedPermissions(h.getAuthorizedUser(), h.getBaseGroupActor(), SecuredSingleton.SYSTEM);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", expected, actual);

        actual = authorizationService.getIssuedPermissions(h.getAuthorizedUser(), h.getBaseGroup(), h.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", empty, actual);

        authorizationService.setPermissions(h.getAdminUser(), h.getBaseGroup().getId(), expected, h.getBaseGroupActor());
        actual = authorizationService.getIssuedPermissions(h.getAuthorizedUser(), h.getBaseGroup(), h.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", expected, actual);
    }

    public void testGetOwnPermissionsGroupPermissions() {
        val empty = new ArrayList<Permission>();
        val expected = Lists.newArrayList(Permission.READ);

        authorizationService.setPermissions(h.getAdminUser(), h.getBaseGroup().getId(), expected, SecuredSingleton.SYSTEM);
        val actual = authorizationService.getIssuedPermissions(h.getAdminUser(), h.getBaseGroupActor(), SecuredSingleton.SYSTEM);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", empty, actual);
    }

    public void testGetPermissionsUnauthorized() {
        try {
            authorizationService.getIssuedPermissions(h.getUnauthorizedUser(), h.getBaseGroupActor(), SecuredSingleton.SYSTEM);
            fail("AuthorizationDelegate.getIssuedPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }

        try {
            authorizationService.getIssuedPermissions(h.getUnauthorizedUser(), h.getBaseGroupActor(), h.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }
}
