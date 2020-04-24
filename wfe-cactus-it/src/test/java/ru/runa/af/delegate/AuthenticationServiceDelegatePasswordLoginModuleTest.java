package ru.runa.af.delegate;

import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public class AuthenticationServiceDelegatePasswordLoginModuleTest extends ServletTestCase {
    private static final String ACTOR1_NAME = "actor1";
    private static final String ACTOR2_NAME = "actor2";
    private static final String ACTOR_VALID_PWD = "validPWD";

    private final String PREFIX = getClass().getName();

    private ServiceTestHelper h;
    private AuthenticationService authenticationService;
    private ExecutorService executorService;

    private Actor validActor;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(PREFIX);
        authenticationService = h.getAuthenticationService();
        executorService = h.getExecutorService();

        validActor = h.createActorIfNotExist(PREFIX + ACTOR1_NAME, "");
        executorService.setPassword(h.getAdminUser(), validActor, ACTOR_VALID_PWD);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        h = null;
        authenticationService = null;
        executorService = null;
        validActor = null;
    }

    public void testValidPassword() {
        User validSubject = authenticationService.authenticateByLoginPassword(validActor.getName(), ACTOR_VALID_PWD);
        assertEquals("authenticated subject doesn't contains actor principal", validSubject.getActor(), validActor);
    }

    public void testValidPasswordWithAnotherActorWithSamePassword() {
        Actor actor2 = h.createActorIfNotExist(PREFIX + ACTOR2_NAME, "");
        executorService.setPassword(h.getAdminUser(), actor2, ACTOR_VALID_PWD);

        User validSubject = authenticationService.authenticateByLoginPassword(validActor.getName(), ACTOR_VALID_PWD);
        assertEquals("authenticated subject doesn't contains actor principal", validSubject.getActor(), validActor);
    }

    public void testLoginFakeActor() {
        try {
            authenticationService.authenticateByLoginPassword(h.getFakeActor().getName(), ACTOR_VALID_PWD);
            fail("allowing fake actor");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testInValidPassword() {
        try {
            authenticationService.authenticateByLoginPassword(validActor.getName(), ACTOR_VALID_PWD + "Invalid");
            fail("allowing invalid password");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testInValidLogin() {
        try {
            authenticationService.authenticateByLoginPassword(validActor.getName() + "Invalid", ACTOR_VALID_PWD);
            fail("allowing invalid login");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
