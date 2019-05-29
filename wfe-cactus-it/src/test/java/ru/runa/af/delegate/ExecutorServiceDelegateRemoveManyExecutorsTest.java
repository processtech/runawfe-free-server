package ru.runa.af.delegate;

import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateRemoveManyExecutorsTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateRemoveManyExecutorsTest.class.getName();

    private List<Executor> additionalActorsGroupsMixed;

    private final List<Permission> deletePermissions = Lists.newArrayList(Permission.DELETE);

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        additionalActorsGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");
        th.setPermissionsToAuthorizedPerformerOnExecutorsList(deletePermissions, additionalActorsGroupsMixed);
    }

    public void testRemoveExecutorsByAuthorizedPerformer() throws Exception {
        executorService.remove(th.getAuthorizedPerformerUser(), th.toIds(additionalActorsGroupsMixed));
        for (Executor executor : additionalActorsGroupsMixed) {
            assertFalse("Executor was not deleted.", th.isExecutorExist(executor));
        }
    }

    public void testRemoveExecutorsByUnauthorizedPerformer() throws Exception {
        try {
            executorService.remove(th.getUnauthorizedPerformerUser(), th.toIds(additionalActorsGroupsMixed));
            fail("Executors were deleted by unauthorize performer.");
        } catch (AuthorizationException e) {
            // that's what we expect to see
        }
        for (Executor executor : additionalActorsGroupsMixed) {
            assertTrue("Executor was deleted.", th.isExecutorExist(executor));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        additionalActorsGroupsMixed = null;
        super.tearDown();
    }
}
