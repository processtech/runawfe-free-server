package ru.runa.af.delegate;

import java.util.Collection;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import com.google.common.collect.Lists;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;

public class AuthorizationServiceDelegateIsAllowedReturnsArrayTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateIsAllowedTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> executorsP = Lists.newArrayList(Permission.CREATE);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(executorsP);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_STATUS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getSubGroupActor());

        authorizationService = Delegates.getAuthorizationService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        helper = null;
        authorizationService = null;
        super.tearDown();
    }

    public void testIsAllowedFakeSubject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getFakeUser(), Permission.READ, Lists.newArrayList(SecuredSingleton.EXECUTORS));
            fail("AuthorizationDelegate.isAllowed() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testIsAllowedFakeSecuredObject() throws Exception {
        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ, Lists.newArrayList(helper.getFakeActor()));
            // TODO
            // fail("AuthorizationDelegate.isAllowed() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            fail("TODO trap");
        }

        try {
            authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ,
                    Lists.newArrayList(helper.getBaseGroupActor(), helper.getFakeActor()));
            // TODO
            // fail("AuthorizationDelegate.isAllowed() allows fake SecuredObject");
        } catch (InternalApplicationException e) {
            fail("TODO trap");
        }
    }

    public void testIsAllowedAASystem() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.CREATE,
                Lists.newArrayList(SecuredSingleton.EXECUTORS));
        boolean[] expected = { true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ,
                Lists.newArrayList(SecuredSingleton.EXECUTORS));
        expected = new boolean[] { false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutor() throws Exception {
        boolean[] isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.READ,
                Lists.newArrayList(helper.getBaseGroupActor(), helper.getSubGroupActor()));
        boolean[] expected = { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE,
                Lists.newArrayList(helper.getBaseGroupActor(), helper.getSubGroupActor()));
        expected = new boolean[] { false, false };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);

        isAllowed = authorizationService.isAllowed(helper.getAuthorizedPerformerUser(), Permission.UPDATE_STATUS,
                Lists.newArrayList(helper.getBaseGroupActor(), helper.getSubGroupActor()));
        expected = new boolean[] { true, true };
        ArrayAssert.assertEqualArrays("AuthorizationDelegate.isAllowed() returns wrong info", expected, isAllowed);
    }

    public void testIsAllowedExecutorDifferentObjects() throws Exception {
        try {
            authorizationService.isAllowed(helper.getUnauthorizedPerformerUser(), Permission.READ,
                    Lists.newArrayList(SecuredSingleton.EXECUTORS, helper.getBaseGroupActor(), helper.getBaseGroup()));
            fail("No Exception: Secured objects should be of the same secured object type (EXECUTORS)");
        } catch (InternalApplicationException e) {
            assertEquals("Secured objects should be of the same secured object type (EXECUTORS)", e.getMessage());
        }
    }

}
