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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateGetVariablesTest extends ServletTestCase {
    private static final String variableName = "var1";
    private static final String variableValue = "var1Value";

    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Long processId;
    private Long taskId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.deployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_FILE_NAME);

        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.SWIMLANE_PROCESS_NAME);

        val vars = new HashMap<String, Object>();
        vars.put(variableName, variableValue);
        processId = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.SWIMLANE_PROCESS_NAME, vars);

        h.addExecutorToGroup(h.getAuthorizedActor(), h.getBossGroup());
        WfTask taskStub = h.getTaskService().getMyTasks(h.getAuthorizedUser(), h.getTaskBatchPresentation()).get(0);
        taskId = taskStub.getId();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(WfServiceTestHelper.SWIMLANE_PROCESS_NAME);
        h.releaseResources();
        executionService = null;
    }

    public void testGetVariablesByUnauthorizedUser() {
        try {
            executionService.getVariables(h.getUnauthorizedUser(), processId);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetVariablesByFakeUser() {
        try {
            executionService.getVariables(h.getFakeUser(), processId);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetVariablesByAuthorizedUserWithInvalidProcessId() {
        try {
            executionService.getVariables(h.getAuthorizedUser(), -1L);
            fail();
        } catch (ProcessDoesNotExistException e) {
            // Expected.
        }
    }

    public void testGetVariablesByAuthorizedUser() {
        List<WfVariable> vars = executionService.getVariables(h.getAuthorizedUser(), processId);
        val names = new ArrayList<String>();
        for (WfVariable v : vars) {
            names.add(v.getDefinition().getName());
        }

        List<String> expectedNames = Lists.newArrayList(variableName);
        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, names);

        val vars2 = new HashMap<String, Object>();
        vars2.put("var2", "var2Value");
        vars2.put("var3", "var3Value");
        vars2.put("approved", "true");
        h.getTaskService().completeTask(h.getAuthorizedUser(), taskId, vars2);

        taskId = h.getTaskService().getMyTasks(h.getErpOperatorUser(), h.getTaskBatchPresentation()).get(0).getId();

        vars = executionService.getVariables(h.getAdminUser(), processId);

        names.clear();
        val vars3 = new HashMap<String, Object>();
        for (WfVariable v : vars) {
            names.add(v.getDefinition().getName());
            vars3.put(v.getDefinition().getName(), v.getValue());
        }
        expectedNames = Lists.newArrayList("var2", "var3", "approved", variableName);
        ArrayAssert.assertWeakEqualArrays("variable names are not equal", expectedNames, names);

        assertEquals(" variable value: <var1> differs from expected", "var1Value", vars3.get("var1"));
        assertEquals(" variable value: <var2> differs from expected", "var2Value", vars3.get("var2"));
        assertEquals(" variable value: <var3> differs from expected", "var3Value", vars3.get("var3"));
        assertEquals(" variable value: <approved> differs from expected", "true", vars3.get("approved"));
    }
}
