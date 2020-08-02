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

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class OrganizationFunctionFactoryTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private Map<String, Object> legalVariables;

    public static Test suite() {
        return new TestSuite(OrganizationFunctionFactoryTest.class);
    }

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());

        h.addExecutorToGroup(h.getHrOperator(), h.getBossGroup());

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        h.getExecutionService().startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, null);

        legalVariables = new HashMap<>();
        legalVariables.put("approved", "true");
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        h = null;
    }

    private User getBossActorUser() {
        return h.getHrOperatorUser();
    }

    public void testOrganizationFunction() {
        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(getBossActorUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "evaluating", tasks.get(0).getName());

        h.getTaskService().completeTask(getBossActorUser(), tasks.get(0).getId(), legalVariables);

        tasks = h.getTaskService().getMyTasks(getBossActorUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "updating erp asynchronously", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getErpOperatorUser(), tasks.get(0).getId(), legalVariables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "treating collegues on cake and pie", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getAuthorizedUser(), tasks.get(0).getId(), legalVariables);

        tasks = h.getTaskService().getMyTasks(getBossActorUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());

        tasks = h.getTaskService().getMyTasks(h.getErpOperatorUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 1, tasks.size());
        assertEquals("task name differs from expected", "notify", tasks.get(0).getName());

        h.getTaskService().completeTask(h.getErpOperatorUser(), tasks.get(0).getId(), legalVariables);

        tasks = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());
    }
}
