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
package ru.runa.af.organizationfunction.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class OrganizationFunctionFactoryTest extends ServletTestCase {
    private final static String PREFIX = OrganizationFunctionFactoryTest.class.getName();

    private WfServiceTestHelper th;

    private Map<String, Object> legalVariables;

    public static Test suite() {
        return new TestSuite(OrganizationFunctionFactoryTest.class);
    }

    @Override
    protected void setUp() throws Exception {

        th = new WfServiceTestHelper(PREFIX);

        th.addExecutorToGroup(th.getHrOperator(), th.getBossGroup());

        th.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        th.setPermissionsToAuthorizedPerformerOnDefinitionByName(
                Lists.newArrayList(DefinitionPermission.READ, DefinitionPermission.START_PROCESS, DefinitionPermission.UNDEPLOY_DEFINITION),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        th.getExecutionService().startProcess(th.getAuthorizedPerformerUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        legalVariables = new HashMap<String, Object>();
        legalVariables.put("approved", "true");

        super.setUp();
    }

    private User getBossActorUser() {
        return th.getHrOperatorUser();
    }

    @Override
    protected void tearDown() throws Exception {
        th.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        th.releaseResources();
        th = null;
        super.tearDown();
    }

    public void testOrganizationFunction() throws Exception {
        List<WfTask> tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(getBossActorUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "evaluating", tasks.get(0).getName());

        th.getTaskService().completeTask(getBossActorUser(), tasks.get(0).getId(), legalVariables, null);

        tasks = th.getTaskService().getMyTasks(getBossActorUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "updating erp asynchronously", tasks.get(0).getName());

        th.getTaskService().completeTask(th.getErpOperatorUser(), tasks.get(0).getId(), legalVariables, null);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());

        th.getTaskService().completeTask(th.getAuthorizedPerformerUser(), tasks.get(0).getId(), legalVariables, null);

        tasks = th.getTaskService().getMyTasks(getBossActorUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = th.getTaskService().getMyTasks(th.getErpOperatorUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "notify", tasks.get(0).getName());

        th.getTaskService().completeTask(th.getErpOperatorUser(), tasks.get(0).getId(), legalVariables, null);

        tasks = th.getTaskService().getMyTasks(th.getAuthorizedPerformerUser(), th.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());
    }
}
