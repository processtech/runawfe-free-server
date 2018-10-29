package ru.runa.af.delegate;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.service.delegate.Delegates;

public class ProfileServiceDelegateGetProfileTest extends ServletTestCase {
    private static final String PREFIX = ProfileServiceDelegateGetProfileTest.class.getName();

    private ServiceTestHelper th;

    private ProfileService profileService;

    @Override
    protected void setUp() throws Exception {
        th = new ServiceTestHelper(PREFIX);
        profileService = Delegates.getProfileService();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        th = null;
        profileService = null;
        super.tearDown();
    }

    public void testNullUser() throws Exception {
        try {
            profileService.getProfile(null);
            fail("ProfileServiceDelegate.saveProfile() allows null subject");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testFakeUser() throws Exception {
        try {
            profileService.getProfile(th.getFakeUser());
            fail("ProfileServiceDelegate.saveProfile() allows fake subject");
        } catch (AuthenticationException e) {
        }
    }

}
