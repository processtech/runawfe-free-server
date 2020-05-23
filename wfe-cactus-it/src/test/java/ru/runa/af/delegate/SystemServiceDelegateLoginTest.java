package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 16.08.2004
 */
public class SystemServiceDelegateLoginTest extends ServletTestCase {
    private ServiceTestHelper h;
    private SystemService systemService;

    @Override
    protected void setUp() {
        systemService = Delegates.getSystemService();
        h = new ServiceTestHelper(getClass().getName());

        h.createDefaultExecutorsMap();
        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.LOGIN), SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        systemService = null;
    }

    public void testLoginWithUnauthorizedUser() {
        try {
            systemService.login(h.getUnauthorizedUser());
            fail("SystemServiceDelegate does not throw AuthorizationFailedException on login() with unauthorized performer user call.");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testLoginWithAuthorizedUser() {
        systemService.login(h.getAuthorizedUser());
        assertTrue("SystemServiceDelegate.login() works.", true);
    }

    public void testLoginWithFakeUser() {
        try {
            systemService.login(h.getFakeUser());
            fail("SystemServiceDelegate does not throw AuthorizationFailedException on login() with fakeUser call.");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
