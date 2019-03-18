package ru.runa.af.delegate;

import java.util.Collection;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 */
public class AuthorizationServiceDelegateSetPermissionsTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    private Collection<Permission> p = Lists.newArrayList(Permission.READ, Permission.UPDATE);

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateSetPermissionsTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> executorsP = Lists.newArrayList(Permission.UPDATE);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(executorsP);

        authorizationService = Delegates.getAuthorizationService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testSetPermissionsFakeSubject() throws Exception {
        try {
            authorizationService.setPermissions(helper.getFakeUser(), helper.getBaseGroupActor().getId(), p, SecuredSingleton.EXECUTORS);
            fail("AuthorizationDelegate.setPermissions() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testSetPermissions() throws Exception {
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getBaseGroup());
        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup().getId(), p, helper.getBaseGroupActor());
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroup(), helper.getBaseGroupActor());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);

        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, SecuredSingleton.EXECUTORS);
        actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                SecuredSingleton.EXECUTORS);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);
    }

    public void testSetNoPermission() throws Exception {
        p = Lists.newArrayList();
        authorizationService.setPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getBaseGroup());
        Collection<Permission> actual = authorizationService.getIssuedPermissions(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getBaseGroup());
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.setPermissions() does not set right permissions", p, actual);
    }

    public void testSetPermissionsUnauthorized() throws Exception {
        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p, helper.getBaseGroup());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroup().getId(), p, helper.getBaseGroupActor());
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }

        try {
            authorizationService.setPermissions(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor().getId(), p,
                    SecuredSingleton.EXECUTORS);
            fail("AuthorizationDelegate.setPermissions() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }

}
