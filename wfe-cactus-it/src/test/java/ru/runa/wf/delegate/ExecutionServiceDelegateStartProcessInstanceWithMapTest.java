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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import lombok.var;
import org.apache.cactus.ServletTestCase;
import ru.runa.junit.ArrayAssert;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Created on 23.04.2005
 * 
 * @author Gritsenko_S
 */
public class ExecutionServiceDelegateStartProcessInstanceWithMapTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private ExecutionService executionService;
    private Map<String, Object> startVariables;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        executionService = Delegates.getExecutionService();

        h.createDefaultExecutorsMap();
        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(
                Lists.newArrayList(Permission.UPDATE_PERMISSIONS, Permission.START_PROCESS, Permission.READ_PROCESS),
                WfServiceTestHelper.VALID_PROCESS_NAME);

        val pp = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActor(pp, h.getBaseGroupActor());
        h.setPermissionsToAuthorizedActor(pp, h.getSubGroupActor());

        startVariables = new HashMap<>();
        startVariables.put("var1start", "var1Value");
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        executionService = null;
    }

    public void testStartProcessInstanceWithMapByUnauthorizedUser() {
        try {
            executionService.startProcess(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testStartProcessInstanceWithMapByFakeUser() {
        try {
            executionService.startProcess(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedUserWithInvalidProcessDefinitionName() {
        try {
            executionService.startProcess(h.getAuthorizedUser(), "INVALID_PROCESS_NAME", startVariables);
            fail();
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }

    public void testStartProcessInstanceWithMapByAuthorizedUserWithNullVariables() {
        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
    }

    public void testStartProcessInstanceWithMapByAuthorizedUser() {
        Long processId = executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);

        List<WfProcess> processInstances = executionService.getProcesses(h.getAuthorizedUser(), h.getProcessInstanceBatchPresentation());
        assertEquals("Process not started", 1, processInstances.size());

        List<WfVariable> variables = executionService.getVariables(h.getAuthorizedUser(), processId);

        val actualVariables = new HashMap<String, Object>();
        for (WfVariable v : variables) {
            actualVariables.put(v.getDefinition().getName(), v.getValue());
        }

        for (Map.Entry<String, Object> entry : startVariables.entrySet()) {
            assertEquals("No predefined variable", actualVariables.get(entry.getKey()), entry.getValue());
        }
        // TODO assertEquals("No swimlane variable",
        // String.valueOf(h.getAuthorizedActor().getCode()),
        // actualVariables.get("requester"));
    }

    public void testStartProcessInstanceWithMapInstancePermissions() {
        WfDefinition defintiion = h.getDefinitionService().getLatestProcessDefinition(h.getAuthorizedUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);

        h.getAuthorizationService().setPermissions(h.getAuthorizedUser(), h.getBaseGroupActor().getId(),
                Lists.newArrayList(Permission.READ_PROCESS), defintiion);
        h.getAuthorizationService().setPermissions(h.getAuthorizedUser(), h.getSubGroupActor().getId(),
                Lists.newArrayList(Permission.READ_PROCESS, Permission.CANCEL_PROCESS), defintiion);

        executionService.startProcess(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, startVariables);

        WfProcess instance = null;
        List<WfProcess> stubs = h.getExecutionService().getProcesses(h.getAuthorizedUser(), h.getProcessInstanceBatchPresentation());
        for (WfProcess processInstance : stubs) {
            if (WfServiceTestHelper.VALID_PROCESS_NAME.equals(processInstance.getName())) {
                instance = processInstance;
                break;
            }
        }
        if (instance == null) {
            throw new ProcessDoesNotExistException(WfServiceTestHelper.VALID_PROCESS_NAME);
        }

        var actual = h.getAuthorizationService().getIssuedPermissions(h.getAuthorizedUser(),
                h.getBaseGroupActor(), instance);
        ArrayAssert.assertWeakEqualArrays("startProcessInstance() does not grant permissions on instance",
                Lists.newArrayList(Permission.READ), actual);

        actual = h.getAuthorizationService().getIssuedPermissions(h.getAuthorizedUser(), h.getSubGroupActor(), instance);
        ArrayAssert.assertWeakEqualArrays("startProcessInstance() does not grant permissions on instance",
                Lists.newArrayList(Permission.READ, Permission.CANCEL), actual);
    }
}
