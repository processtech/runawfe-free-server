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
package ru.runa.wf.service;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class WfServiceTestHelper extends ServiceTestHelper {

    private static final String PROCESS_DEFINITION_ADMINISTRATORS = "Process Definition Administrators";

    public final static String VALID_FILE_NAME = "validProcess.par";
    public final static String ONE_SWIMLANE_FILE_NAME = "oneSwimlaneProcess.par";
    public final static String INVALID_FILE_NAME = "invalidProcess.par";
    public final static String NONEXISTINGSWIMLINES_FILE_NAME = "NonExistingSwimlanes.par";
    public final static String ORGANIZATION_FUNCTION_PAR_FILE_NAME = "organizationfunction.par";

    public final static String VALID_PROCESS_NAME = "validProcess";
    public final static String ONE_SWIMLANE_PROCESS_NAME = "oneSwimlaneProcess";
    public final static String INVALID_PROCESS_NAME = "invalidProcess";
    public final static String SWIMLANE_PROCESS_NAME = "swimlaneProcess";
    public final static String LONG_WITH_VARIABLES_PROCESS_NAME = "longProcessWithVariables";
    public final static String SWIMLANE_PROCESS_FILE_NAME = "swimlaneProcess.par";
    public final static String DECISION_JPDL_PROCESS_NAME = "jpdlDecisionTestProcess";
    public final static String DECISION_JPDL_PROCESS_FILE_NAME = "jpdlDecisionTestProcess.par";
    public final static String FORK_JPDL_1_PROCESS_NAME = "jpdlFork1TestProcess";
    public final static String FORK_JPDL_1_PROCESS_FILE_NAME = "jpdlFork1TestProcess.par";
    public final static String FORK_JPDL_2_PROCESS_NAME = "jpdlFork2TestProcess";
    public final static String FORK_JPDL_2_PROCESS_FILE_NAME = "jpdlFork2TestProcess.par";
    public final static String FORK_FAULT_JPDL_PROCESS_NAME = "jpdlForkFaultTestProcess";
    public final static String FORK_FAULT_JPDL_PROCESS_FILE_NAME = "jpdlForkFaultTestProcess.par";
    public final static String LONG_WITH_VARIABLES_PROCESS_FILE_NAME = "longProcessWithVariables.par";
    public final static String TIMER_PROCESS_NAME = "timerProcess";
    public final static String SWIMLANE_SAME_GROUP_SEQ_PROCESS_NAME = "sameGroupRoleStateSequence";

    @Getter
    protected DefinitionService definitionService;
    @Getter
    protected ExecutionService executionService;
    @Getter
    protected TaskService taskService;

    private byte[] validDefinition;
    private byte[] invalidDefinition;
    private byte[] nonExistingSwimlanesDefinition;

    private final static String HR_ACTOR_NAME = "HrOperator";
    private final static String HR_ACTOR_PWD = "HrOperator";

    private final static String SWIMLANE2_ACTOR_NAME = "ErpOperator";
    private final static String SWIMLANE2_ACTOR_PWD = "ErpOperator";
    private final static String SWIMLANE1_GROUP_NAME = "BossGroup";

    private Actor erpOperator = null;
    @Getter
    private User  erpOperatorUser = null;
    private Actor hrOperator = null;
    @Getter
    private User  hrOperatorUser = null;
    private Group bossGroup = null;

    public WfServiceTestHelper(String testClassPrefixName) {
        super(testClassPrefixName);
        createDelegates();
        createSampleDefinitions();
        createSwimlaneExecutors();
    }

    private void createSwimlaneExecutors() {
        erpOperator = createActorIfNotExist(SWIMLANE2_ACTOR_NAME, "Actor in swimlane of test process");
        getExecutorService().setPassword(getAdminUser(), erpOperator, SWIMLANE2_ACTOR_PWD);
        hrOperator = createActorIfNotExist(HR_ACTOR_NAME, "Actor in HR");
        getExecutorService().setPassword(getAdminUser(), hrOperator, HR_ACTOR_PWD);
        bossGroup = createGroupIfNotExist(SWIMLANE1_GROUP_NAME, "Group in swimlane of test process");
        getAuthorizationService().setPermissions(getAdminUser(), erpOperator.getId(), Lists.newArrayList(Permission.LOGIN), SecuredSingleton.SYSTEM);
        erpOperatorUser = Delegates.getAuthenticationService().authenticateByLoginPassword(erpOperator.getName(), SWIMLANE2_ACTOR_PWD);
        hrOperatorUser = Delegates.getAuthenticationService().authenticateByLoginPassword(hrOperator.getName(), HR_ACTOR_PWD);
    }

    public Actor getHrOperator() {
        return getExecutorService().getExecutor(getAdminUser(), hrOperator.getId());
    }

    public Group getBossGroup() {
        return getExecutorService().getExecutor(getAdminUser(), bossGroup.getId());
    }

    public Actor getErpOperator() {
        return getExecutorService().getExecutor(getAdminUser(), erpOperator.getId());
    }

    private void createSampleDefinitions() {
        validDefinition = readBytesFromFile(VALID_FILE_NAME);
        invalidDefinition = readBytesFromFile(INVALID_FILE_NAME);
        nonExistingSwimlanesDefinition = readBytesFromFile(NONEXISTINGSWIMLINES_FILE_NAME);
    }

    @Override
    public void releaseResources() {
        definitionService = null;
        executionService = null;
        erpOperator = null;
        erpOperatorUser = null;
        bossGroup = null;
        super.releaseResources();
    }

    public void setPermissionsToAuthorizedActorOnDefinition(Collection<Permission> permissions, WfDefinition definition) {
        authorizationService.setPermissions(adminUser, getAuthorizedActor().getId(), permissions, definition);
    }

    public void setPermissionsToAuthorizedActorOnProcessInstance(Collection<Permission> permissions, WfProcess instance) {
        authorizationService.setPermissions(adminUser, getAuthorizedActor().getId(), permissions, instance);
    }

    public void setPermissionsToAuthorizedActorOnDefinitionByName(Collection<Permission> permissions, String processDefinitionName) {
        WfDefinition definition = definitionService.getLatestProcessDefinition(adminUser, processDefinitionName);
        authorizationService.setPermissions(adminUser, getAuthorizedActor().getId(), permissions, definition);
    }

    public byte[] getValidProcessDefinition() {
        return validDefinition;
    }

    public byte[] getInValidProcessDefinition() {
        return invalidDefinition;
    }

    public byte[] getProcessDefinitionWithNonExistingSwimlanes() {
        return nonExistingSwimlanesDefinition;
    }

    public void deployValidProcessDefinition() {
        try {
            setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_DEFINITION), SecuredSingleton.SYSTEM);
            definitionService.deployProcessDefinition(getAuthorizedUser(), getValidProcessDefinition(), Lists.newArrayList("testProcess"));
            setPermissionsToAuthorizedActor(Lists.newArrayList(), SecuredSingleton.SYSTEM);
        } catch (DefinitionAlreadyExistException e) {
            // Ignore.
        }
    }

    public void undeployValidProcessDefinition() {
        setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.DELETE), WfServiceTestHelper.VALID_PROCESS_NAME);
        definitionService.undeployProcessDefinition(getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
    }

    public void deployValidProcessDefinition(String parResourceName) {
        try {
            setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_DEFINITION), SecuredSingleton.SYSTEM);
            definitionService.deployProcessDefinition(getAuthorizedUser(), readBytesFromFile(parResourceName),
                    Lists.newArrayList("testProcess"));
            setPermissionsToAuthorizedActor(Lists.newArrayList(), SecuredSingleton.SYSTEM);
        } catch (DefinitionAlreadyExistException e) {
            // Ignore.
        }
    }

    public void undeployValidProcessDefinition(String parDefinitionName) {
        setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.DELETE), parDefinitionName);
        definitionService.undeployProcessDefinition(getAuthorizedUser(), parDefinitionName, null);
    }

    private void createDelegates() {
        definitionService = Delegates.getDefinitionService();
        executionService = Delegates.getExecutionService();
        taskService = Delegates.getTaskService();
        PropertyResources.setDatabaseAvailable(true);
        Delegates.getSystemService().setSetting(SystemProperties.CONFIG_FILE_NAME, "undefined.variables.allowed", "true");
    }

    public Group getProcessDefinitionAdministratorsGroup() {
        return (Group) getExecutor(PROCESS_DEFINITION_ADMINISTRATORS);
    }

    public BatchPresentation getProcessDefinitionBatchPresentation() {
        return BatchPresentationFactory.DEFINITIONS.createDefault();
    }

    public BatchPresentation getProcessDefinitionBatchPresentation(String presentationId) {
        return BatchPresentationFactory.DEFINITIONS.createDefault(presentationId);
    }

    public BatchPresentation getProcessInstanceBatchPresentation() {
        return BatchPresentationFactory.PROCESSES.createDefault();
    }

    public BatchPresentation getProcessInstanceBatchPresentation(String presentationId) {
        return BatchPresentationFactory.PROCESSES.createDefault(presentationId);
    }

    public BatchPresentation getTaskBatchPresentation() {
        return BatchPresentationFactory.TASKS.createDefault();
    }

    public BatchPresentation getTaskBatchPresentation(String presentationId) {
        return BatchPresentationFactory.PROCESSES.createDefault(presentationId);
    }

    @SneakyThrows
    public static byte[] readBytesFromFile(String fileName) {
        InputStream is = WfServiceTestHelper.class.getResourceAsStream(fileName);
        return ByteStreams.toByteArray(is);
    }

    public static Map<String, Object> createVariablesMap(String variableName, Object variableValue) {
        val vars = new HashMap<String, Object>();
        vars.put(variableName, variableValue);
        return vars;
    }
}
