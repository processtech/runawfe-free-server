package ru.runa.af.delegate;

import java.util.Collection;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 */
public class AuthorizationServiceDelegateGetOwnPermissionsTest extends ServletTestCase {
    private ServiceTestHelper helper;
    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetOwnPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> p = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformerOnSystem(p);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = Delegates.getAuthorizationService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testGetOwnPermissionsNullSubject() throws Exception {
        try {
            authorizationService.getIssuedPermissions(null, helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetOwnPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getFakeUser(), helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetOwnPermissionsNullExecutor() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), null, helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows null executor");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetOwnPermissionsFakeExecutor() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getFakeActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows fake executor");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetOwnPermissionsNullIdentifiable() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(), null);
            fail("AuthorizationDelegate.getIssuedPermissions() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetOwnPermissionsFakeIdentifiable() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(), helper.getFakeActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testGetOwnPermissions() throws Exception {
        Collection<Permission> noPermission = Lists.newArrayList();
        Collection<Permission> expected = Lists.newArrayList(Permission.READ);

        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", noPermission, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), expected, helper.getAASystem());
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(), helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", expected, actual);

        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", noPermission, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup().getId(), expected, helper.getBaseGroupActor());
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", expected, actual);
    }

    public void testGetOwnPermissionsGroupPermissions() throws Exception {
        Collection<Permission> noPermission = Lists.newArrayList();
        Collection<Permission> expected = Lists.newArrayList(Permission.READ);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup().getId(), expected, helper.getAASystem());
        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getAASystem());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getIssuedPermissions() returns wrong permissions", noPermission, actual);

    }

    public void testGetOwnPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.getIssuedPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor(), helper.getAASystem());
            fail("AuthorizationDelegate.getIssuedPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.getIssuedPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor(), helper.getBaseGroupActor());
            fail("AuthorizationDelegate.getIssuedPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }
}
