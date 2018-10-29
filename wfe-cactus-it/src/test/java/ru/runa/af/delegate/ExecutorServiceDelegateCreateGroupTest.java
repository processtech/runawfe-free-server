package ru.runa.af.delegate;

import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

public class ExecutorServiceDelegateCreateGroupTest extends ServletTestCase {
    private ServiceTestHelper th;

    private ExecutorService executorService;

    private static String testPrefix = ExecutorServiceDelegateCreateGroupTest.class.getName();

    private final List<Permission> createPermissions = Lists.newArrayList(SystemPermission.CREATE_EXECUTOR);

    private Group group;

    @Override
    protected void setUp() throws Exception {
        executorService = Delegates.getExecutorService();
        th = new ServiceTestHelper(testPrefix);
        th.setPermissionsToAuthorizedPerformerOnSystem(createPermissions);
        super.setUp();
    }

    public void testCreateGroupByAuthorizedPerformer() throws Exception {
        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        group = executorService.create(th.getAuthorizedPerformerUser(), group);
        assertTrue("Executor (group) does not exists ", th.isExecutorExist(group));
        Group returnedGroup = executorService.getExecutor(th.getAuthorizedPerformerUser(), group.getId());
        assertEquals("Returned group differes with created one", group, returnedGroup);
    }

    public void testCreateExecutorByUnAuthorizedPerformer() throws Exception {

        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        try {
            group = executorService.create(th.getUnauthorizedPerformerUser(), group);
            fail("ExecutorServiceDelegate.create(group) creates executor without permissions");
        } catch (AuthorizationException e) {
            // This is supposed result of operation
        }
        assertFalse("Executor exists ", th.isExecutorExist(group));
    }

    public void testCreateAlreadyExistedGroup() throws Exception {
        Group group2 = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        group = executorService.create(th.getAuthorizedPerformerUser(), group2);
        assertTrue("Executor does not exists ", th.isExecutorExist(group));
        try {
            executorService.create(th.getAuthorizedPerformerUser(), group2);
            fail("ExecutorServiceDelegate.create(group) creates already existed group");
        } catch (ExecutorAlreadyExistsException e) {
            // This is supposed result of operation
        }
        Group returnedGroup = executorService.getExecutor(th.getAuthorizedPerformerUser(), group.getId());
        assertEquals("Returned actor differes with created one", group, returnedGroup);
    }

    public void testCreateAlreadyExistedActor() throws Exception {
        Actor actor = th.createActorIfNotExist("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        group = new Group(actor.getName(), actor.getDescription());
        try {
            executorService.create(th.getAuthorizedPerformerUser(), group);
            fail("ExecutorServiceDelegate.create(group) creates already existed actor");
        } catch (ExecutorAlreadyExistsException e) {
            // This is supposed result of operation
        }
    }

    public void testCreateNullExecutorByAuthorizedPerformer() throws Exception {
        Group group2 = null;
        try {
            group = executorService.create(th.getAuthorizedPerformerUser(), group2);
            fail("null executor created");
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
    }

    public void testCreateExecutorWithNullSubject() throws Exception {
        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        try {
            group = executorService.create(null, group);
            fail("executor with null subject created");
        } catch (IllegalArgumentException e) {
            // This is supposed result of operation
        }
        assertFalse("Executor does not exists ", th.isExecutorExist(group));
    }

    public void testCreateExecutorWithFakeSubject() throws Exception {
        group = new Group("ExecutorServiceDelegateCreateGroupTest_Group", "description");
        try {
            group = executorService.create(th.getFakeUser(), group);
            fail("executor with fake subject created");
        } catch (AuthenticationException e) {
            // This is supposed result of operation
        }
        assertFalse("Executor does not exists ", th.isExecutorExist(group));
    }

    @Override
    protected void tearDown() throws Exception {
        th.removeExecutorIfExists(group);
        group = null;

        th.releaseResources();
        executorService = null;
        group = null;
        super.tearDown();
    }

}
