package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class GetTaskListTest extends ServletTestCase {
    // see par file for explanation
    private final static String GROUP1_NAME = "group1";
    private final static String GROUP2_NAME = "group2";
    private final static String ACTOR3_NAME = "actor3";
    private final static String ACTOR3_PASSWD = "actor3";
    private final static String PROCESS_NAME = "simple process";

    private WfServiceTestHelper h;
    private BatchPresentation batchPresentation;

    private Group group1;
    private Group group2;
    private Actor actor3;
    private User actor3User;

    @Override
    protected void setUp() {
        val prefix = getClass().getName();
        h = new WfServiceTestHelper(prefix);
        batchPresentation = h.getTaskBatchPresentation();

        group1 = h.createGroupIfNotExist(GROUP1_NAME, prefix);
        group2 = h.createGroupIfNotExist(GROUP2_NAME, prefix);
        actor3 = h.createActorIfNotExist(ACTOR3_NAME, prefix);
        h.getExecutorService().setPassword(h.getAdminUser(), actor3, ACTOR3_PASSWD);

        h.getAuthorizationService().setPermissions(h.getAdminUser(), actor3.getId(), Lists.newArrayList(Permission.LOGIN), SecuredSingleton.SYSTEM);
        actor3User = Delegates.getAuthenticationService().authenticateByLoginPassword(actor3.getName(), ACTOR3_PASSWD);

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_DEFINITION), SecuredSingleton.SYSTEM);

        val parBytes = WfServiceTestHelper.readBytesFromFile(WfServiceTestHelper.ORGANIZATION_FUNCTION_PAR_FILE_NAME);
        val def = h.getDefinitionService().deployProcessDefinition(h.getAuthorizedUser(), parBytes, Lists.newArrayList("testProcess"), null);

        h.setPermissionsToAuthorizedActorOnDefinition(Lists.newArrayList(Permission.DELETE, Permission.START_PROCESS), def);
    }

    @Override
    protected void tearDown() {
        h.getDefinitionService().undeployProcessDefinition(h.getAuthorizedUser(), PROCESS_NAME, null);
        h.releaseResources();
        h = null;
        actor3 = null;
        group1 = null;
        group2 = null;
        actor3User = null;
        batchPresentation = null;
    }

    public void testEqualsFunctionTest() {

        h.getExecutionService().startProcess(h.getAuthorizedUser(), PROCESS_NAME, null);
        // assignment handler creates secured object for swimlane and grants
        // group2 permissions on it, so we have to update reference
        group2 = (Group) h.getExecutor(group2.getName());

        List<WfTask> tasks;

        tasks = h.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        h.addExecutorToGroup(h.getAuthorizedActor(), group2);
        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", group2, tasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", actor3, tasks.get(0).getOwner());

        h.getTaskService().completeTask(actor3User, tasks.get(0).getId(), null);
        // same as commented higher
        actor3 = (Actor) h.getExecutor(actor3.getName());
        group2 = (Group) h.getExecutor(group2.getName());

        tasks = h.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        h.addExecutorToGroup(actor3, group1);
        tasks = h.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        h.removeExecutorFromGroup(h.getAuthorizedActor(), group2);
        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", h.getAuthorizedActor(), tasks.get(0).getOwner());

        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), null);
    }
}
