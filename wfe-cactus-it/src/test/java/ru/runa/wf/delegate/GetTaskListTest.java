/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.delegate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wf.service.WfServiceTestHelper;

import com.google.common.collect.Lists;
import ru.runa.wfe.user.User;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 * @author kana <a href="mailto:kana@ptc.ru">
 */
public class GetTaskListTest extends ServletTestCase {
    private final static String PREFIX = GetTaskListTest.class.getName();

    private WfServiceTestHelper th;

    private byte[] parBytes;

    // see par file for explanation
    private final static String PROCESS_NAME = "simple process";

    private final static String ACTOR3_NAME = "actor3";

    private final static String ACTOR3_PASSWD = "actor3";

    private final static String GROUP1_NAME = "group1";

    private final static String GROUP2_NAME = "group2";

    private Group group1;

    private Group group2;

    private Actor actor3;

    private User actor3User;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        th = new WfServiceTestHelper(PREFIX);

        actor3 = th.createActorIfNotExist(ACTOR3_NAME, PREFIX);
        th.getExecutorService().setPassword(th.getAdminUser(), actor3, ACTOR3_PASSWD);
        group1 = th.createGroupIfNotExist(GROUP1_NAME, PREFIX);
        group2 = th.createGroupIfNotExist(GROUP2_NAME, PREFIX);

        parBytes = WfServiceTestHelper.readBytesFromFile(WfServiceTestHelper.ORGANIZATION_FUNCTION_PAR_FILE_NAME);

        Collection<Permission> p = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
        th.getAuthorizationService().setPermissions(th.getAdminUser(), actor3.getId(), p, ASystem.INSTANCE);
        actor3User = Delegates.getAuthenticationService().authenticateByLoginPassword(actor3.getName(), ACTOR3_PASSWD);
        th.setPermissionsToAuthorizedPerformerOnSystem(ASystem.INSTANCE.getSecuredObjectType().getAllPermissions());

        th.getDefinitionService().deployProcessDefinition(th.getAuthorizedPerformerUser(), parBytes, Lists.newArrayList("testProcess"));
        batchPresentation = th.getTaskBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        th.getDefinitionService().undeployProcessDefinition(th.getAuthorizedPerformerUser(), PROCESS_NAME, null);
        parBytes = null;
        th.releaseResources();
        th = null;
        actor3 = null;
        group1 = null;
        group2 = null;
        actor3User = null;
        batchPresentation = null;
        super.tearDown();
    }

    public void testEqualsFunctionTest() throws Exception {

        th.getExecutionService().startProcess(th.getAuthorizedPerformerUser(), PROCESS_NAME, null);
        // assignment handler creates secured object for swimlane and grants
        // group2 permissions on it, so we have to update reference
        group2 = (Group) th.getExecutor(group2.getName());

        List<WfTask> tasks;

        tasks = th.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        th.addExecutorToGroup(th.getAuthorizedPerformerActor(), group2);
        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", group2, tasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>(), null);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", actor3, tasks.get(0).getOwner());

        th.getTaskService().completeTask(actor3User, tasks.get(0).getId(), new HashMap<String, Object>(), null);
        // same as commented higher
        actor3 = (Actor) th.getExecutor(actor3.getName());
        group2 = (Group) th.getExecutor(group2.getName());

        tasks = th.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        th.addExecutorToGroup(actor3, group1);
        tasks = th.getTaskService().getMyTasks(actor3User, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        th.removeExecutorFromGroup(th.getAuthorizedPerformerActor(), group2);
        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), batchPresentation);
        assertEquals("getTasks() returns wrong tasks number", 1, tasks.size());

        assertEquals("task assigned", th.getAuthorizedPerformerActor(), tasks.get(0).getOwner());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), new HashMap<String, Object>(), null);
    }
}
