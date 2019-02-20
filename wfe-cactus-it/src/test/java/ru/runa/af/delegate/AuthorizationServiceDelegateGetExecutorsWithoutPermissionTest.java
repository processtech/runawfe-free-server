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
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

/**
 * Created on 20.08.2004
 * 
 */
public class AuthorizationServiceDelegateGetExecutorsWithoutPermissionTest extends ServletTestCase {
    private ServiceTestHelper helper;

    private AuthorizationService authorizationService;

    @Override
    protected void setUp() throws Exception {
        helper = new ServiceTestHelper(AuthorizationServiceDelegateGetExecutorsWithoutPermissionTest.class.getName());
        helper.createDefaultExecutorsMap();

        Collection<Permission> systemP = Lists.newArrayList(Permission.READ, SystemPermission.CREATE_EXECUTOR);
        helper.setPermissionsToAuthorizedPerformerOnSystem(systemP);

        Collection<Permission> executorP = Lists.newArrayList(Permission.READ, Permission.UPDATE_PERMISSIONS);
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroupActor());
        helper.setPermissionsToAuthorizedPerformer(executorP, helper.getBaseGroup());

        authorizationService = Delegates.getAuthorizationService();
        authorizationService.setPermissions(helper.getAdminUser(), helper.getBaseGroupActor().getId(), executorP, helper.getBaseGroupActor());
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        authorizationService = null;
        super.tearDown();
    }

    public void testGetExecutorsWithoutPermissionNullUser() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(null, helper.getAASystem(), helper.getExecutorBatchPresentation(), false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetExecutorsWithoutPermissionFakeSubject() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getFakeUser(), helper.getAASystem(), helper.getExecutorBatchPresentation(), false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetExecutorsWithoutPermissionNullIdentifiable() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerUser(), null, helper.getExecutorBatchPresentation(), false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows null identifiable");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGetExecutorsWithoutPermissionFakeIdentifiable() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerUser(), helper.getFakeActor(),
                    helper.getExecutorBatchPresentation(), false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows fake identifiable");
        } catch (InternalApplicationException e) {
        }
    }

    public void testGetExecutorsWithoutPermission() throws Exception {
        List<Group> expected = Lists.newArrayList(helper.getBaseGroup());
        List<Executor> actual = authorizationService.getExecutorsWithPermission(helper.getAuthorizedPerformerUser(), helper.getBaseGroupActor(),
                helper.getExecutorBatchPresentation(), false);
        ArrayAssert.assertWeakEqualArrays("AuthorizationDelegate.getExecutorsWithoutPermission() returns wrong executors", expected, actual);
    }

    public void testGetExecutorsWithoutPermissionUnauthorized() throws Exception {
        try {
            authorizationService.getExecutorsWithPermission(helper.getUnauthorizedPerformerUser(), helper.getBaseGroupActor(),
                    helper.getExecutorBatchPresentation(), false);
            fail("AuthorizationDelegate.getExecutorsWithoutPermission() allows unauthorized operation");
        } catch (AuthorizationException e) {
        }
    }

}
