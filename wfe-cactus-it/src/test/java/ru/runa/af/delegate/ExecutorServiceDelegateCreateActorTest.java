package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;

public class ExecutorServiceDelegateCreateActorTest extends ServletTestCase {
    private final String PREFIX = getClass().getName();

    private ServiceTestHelper h;
    private ExecutorService executorService;

    private Actor actor;

    @Override
    protected void setUp() {
        h = new ServiceTestHelper(PREFIX);
        executorService = Delegates.getExecutorService();

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_EXECUTOR), SecuredSingleton.SYSTEM);
        actor = new Actor(PREFIX + "_Name", PREFIX + "_Desc", PREFIX + "_FullName", System.currentTimeMillis());
    }

    @Override
    protected void tearDown() {
        h.removeExecutorIfExists(actor);
        actor = null;
        h.releaseResources();
        executorService = null;
    }

    public void testCreateActorByAuthorizedUser() {
        actor = executorService.create(h.getAuthorizedUser(), actor);
        assertTrue("Executor does not exists ", h.isExecutorExist(actor));
        Actor returnedActor = executorService.getExecutorByName(h.getAdminUser(), actor.getName());
        assertEquals("Returned actor differes with created one", actor, returnedActor);
    }

    public void testCreateExecutorByUnAuthorizedUser() {
        try {
            executorService.create(h.getUnauthorizedUser(), actor);
            fail("ExecutorServiceDelegate allow unauthorized create");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testCreateAlreadyExistedExecutor() {
        Actor actor2 = executorService.create(h.getAuthorizedUser(), actor);
        assertTrue("Executor does not exists ", h.isExecutorExist(actor));
        try {
            executorService.create(h.getAuthorizedUser(), actor);
            fail("ExecutorServiceDelegate allow create actor with same name");
        } catch (ExecutorAlreadyExistsException e) {
            // This is supposed result of operation
        }
        Actor returnedActor = executorService.getExecutor(h.getAuthorizedUser(), actor2.getId());
        assertEquals("Returned actor differes with created one", actor, returnedActor);
    }

    public void testCreateExecutorWithFakeUser() {
        try {
            executorService.create(h.getFakeUser(), actor);
            fail("executor with fake subject created");
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
