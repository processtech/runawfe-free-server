package ru.runa.af.delegate;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 16.08.2004
 */
public class AASystemServiceDelegateLoginTest extends ServletTestCase {
    private ServiceTestHelper th;
    private SystemService systemService;
    private static String testPrefix = AASystemServiceDelegateLoginTest.class.getName();

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        systemService = null;
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        systemService = Delegates.getSystemService();
        th = new ServiceTestHelper(testPrefix);
        th.createDefaultExecutorsMap();
        th.setPermissionsToAuthorizedPerformerOnSystem(Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM));
        super.setUp();
    }

    public void testLoginWithNullUser() throws Exception {
        try {
            systemService.login(null);
            fail("SystemServiceDelegate does not throw IllegalArgumentException on login(null) call.");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testLoginWithUnauthorizedPerformer() throws Exception {
        try {
            systemService.login(th.getUnauthorizedPerformerUser());
            fail("SystemServiceDelegate does not throw AuthorizationFailedException on login() with unauthorized performer user call.");
        } catch (AuthorizationException e) {
            // that's what we expected
        }
    }

    public void testLoginWithAuthorizedPerformer() throws Exception {
        systemService.login(th.getAuthorizedPerformerUser());
        assertTrue("SystemServiceDelegate.login() works.", true);
    }

    public void testLoginWithFakeUser() throws Exception {
        try {
            systemService.login(th.getFakeUser());
            fail("SystemServiceDelegate does not throw AuthorizationFailedException on login() with fakeUser call.");
        } catch (AuthenticationException e) {
            // that's what we expected
        }
    }

}
