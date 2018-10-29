package ru.runa.af.delegate;

import com.google.common.collect.Lists;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.junit.ArrayAssert;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;

import java.util.List;

/**
 * Created on 16.05.2005
 */
// TODO executionService.getExecutorsByIds
public class ExecutorServiceDelegateGetExecutorsTest extends ServletTestCase {
    private ServiceTestHelper th;
    private ExecutorService executorService;
    private static String testPrefix = ExecutorServiceDelegateGetExecutorsTest.class.getName();

    private List<Executor> additionalActorGroupsMixed;
    private final List<Permission> readPermissions = Lists.newArrayList(Permission.READ);
    private List<Long> executorsIDs;

    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);

        additionalActorGroupsMixed = th.createMixedActorsGroupsArray("additionalMixed", "Additional Mixed");

        th.setPermissionsToAuthorizedPerformerOnExecutors(readPermissions, additionalActorGroupsMixed);

        executorsIDs = Lists.newArrayList();
        for (Executor executor : additionalActorGroupsMixed) {
            executorsIDs.add(executor.getId());
        }

        super.setUp();
    }

    public void testGetExecutorsByAuthorizedPerformer() throws Exception {
        List<Executor> returnedExecutors = th.getExecutors(th.getAuthorizedPerformerUser(), executorsIDs);
        ArrayAssert
                .assertWeakEqualArrays("businessDelegate.getExecutors() returns wrong executor set", additionalActorGroupsMixed, returnedExecutors);
    }

    public void testGetExecutorsByUnauthorizedPerformer() throws Exception {
        User unauthorizedPerformerUser = th.getUnauthorizedPerformerUser();
        try {
            th.getExecutors(unauthorizedPerformerUser, executorsIDs);
            fail("businessDelegate allow to getExecutor() to performer without Permission.READ.");
        } catch (AuthorizationException e) {
            //That's what we expect
        }
    }

    public void testGetUnexistedExecutorByAuthorizedPerformer() throws Exception {
        executorsIDs = Lists.newArrayList(-1L, -2L, -3L);
        try {
            th.getExecutors(th.getAuthorizedPerformerUser(), executorsIDs);
            fail("businessDelegate does not throw Exception to getExecutor() for UnexistedExecutor");
        } catch (ExecutorDoesNotExistException e) {
            //That's what we expect
        }
    }

    public void testGetExecutorsByNullPerformer() throws Exception {
        try {
            th.getExecutors(null, executorsIDs);
            fail("businessDelegate allow to getExecutors() to performer with null subject.");
        } catch (IllegalArgumentException e) {
            //That's what we expect 
        }
    }

    protected void tearDown() throws Exception {
        th.releaseResources();
        executorService = null;
        executorsIDs = null;
        additionalActorGroupsMixed = null;
        super.tearDown();
    }
}
