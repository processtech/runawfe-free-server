package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

/**
 * Created on 27.08.2004
 */
public class ExecutorServiceDelegateSetPasswordTest extends ServletTestCase {
    private static final String NEW_PASSWD = "new passwd";

    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Actor actor;
    private Group group;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(getClass().getName());
        executorService = Delegates.getExecutorService();

        h.createDefaultExecutorsMap();
        actor = h.getBaseGroupActor();
        group = h.getBaseGroup();

        val pp = Lists.newArrayList(Permission.UPDATE);
        h.setPermissionsToAuthorizedActor(pp, actor);
        h.setPermissionsToAuthorizedActor(pp, group);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        executorService = null;
        actor = null;
        group = null;
    }

    public void testSetPasswordByAuthorizedUser() {
        executorService.setPassword(h.getAuthorizedUser(), actor, NEW_PASSWD);
        assertTrue("Password is not correct.", h.isPasswordCorrect(actor.getName(), NEW_PASSWD));
    }

    public void testSetPasswordByUnauthorizedUser() {
        try {
            executorService.setPassword(h.getUnauthorizedUser(), actor, NEW_PASSWD);
            fail("Password was changed without permission.");
        } catch (AuthorizationException e) {
            // Expected.
        }
        assertFalse("Password was changed without permission.", h.isPasswordCorrect(actor.getName(), NEW_PASSWD));
    }
}
