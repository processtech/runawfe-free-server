package ru.runa.wf.service;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.af.service.ServiceTestHelper;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
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

    protected DefinitionService definitionService;

    protected ExecutionService executionService;

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

    private User erpOperatorUser = null;

    private Actor hrOperator = null;

    private User hrOperatorUser = null;

    private Group bossGroup = null;

    public WfServiceTestHelper(String testClassPrefixName) throws Exception {
        super(testClassPrefixName);
        createDelegates();
        createSampleDefinitions();
        createSwimlaneExecutors();
    }

    private void createSwimlaneExecutors() throws InternalApplicationException {
        erpOperator = createActorIfNotExist(SWIMLANE2_ACTOR_NAME, "Actor in swimlane of test process");
        getExecutorService().setPassword(getAdminUser(), erpOperator, SWIMLANE2_ACTOR_PWD);
        hrOperator = createActorIfNotExist(HR_ACTOR_NAME, "Actor in HR");
        getExecutorService().setPassword(getAdminUser(), hrOperator, HR_ACTOR_PWD);
        bossGroup = createGroupIfNotExist(SWIMLANE1_GROUP_NAME, "Group in swimlane of test process");
        List<Permission> p = Lists.newArrayList(Permission.LOGIN);
        getAuthorizationService().setPermissions(getAdminUser(), erpOperator.getId(), p, SecuredSingleton.EXECUTORS);
        erpOperatorUser = Delegates.getAuthenticationService().authenticateByLoginPassword(erpOperator.getName(), SWIMLANE2_ACTOR_PWD);
        hrOperatorUser = Delegates.getAuthenticationService().authenticateByLoginPassword(hrOperator.getName(), HR_ACTOR_PWD);
    }

    /**
     * @return Returns the hrOperator.
     * @throws ExecutorDoesNotExistException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Actor getHrOperator() throws InternalApplicationException {
        return getExecutorService().getExecutor(getAdminUser(), hrOperator.getId());
    }

    /**
     * @return Returns the hrOperatorUser.
     */
    public User getHrOperatorUser() {
        return hrOperatorUser;
    }

    /**
     * @return Returns the bossGroup.
     * @throws ExecutorDoesNotExistException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Group getBossGroup() throws InternalApplicationException {
        return getExecutorService().getExecutor(getAdminUser(), bossGroup.getId());
    }

    /**
     * @return Returns the erpOperator.
     * @throws ExecutorDoesNotExistException
     * @throws AuthenticationException
     * @throws AuthorizationException
     * @throws InternalApplicationException
     */
    public Actor getErpOperator() throws InternalApplicationException {
        return getExecutorService().getExecutor(getAdminUser(), erpOperator.getId());
    }

    /**
     * @return Returns the erpOperatorUser.
     */
    public User getErpOperatorUser() {
        return erpOperatorUser;
    }

    private void createSampleDefinitions() throws IOException {
        validDefinition = readBytesFromFile(VALID_FILE_NAME);
        invalidDefinition = readBytesFromFile(INVALID_FILE_NAME);
        nonExistingSwimlanesDefinition = readBytesFromFile(NONEXISTINGSWIMLINES_FILE_NAME);
    }

    @Override
    public void releaseResources() throws InternalApplicationException {
        definitionService = null;

        executionService = null;
        erpOperator = null;
        erpOperatorUser = null;
        bossGroup = null;
        super.releaseResources();
    }

    public void setPermissionsToAuthorizedPerformerOnDefinition(Collection<Permission> permissions, WfDefinition definition)
            throws InternalApplicationException {
        authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, definition);
    }

    public void setPermissionsToAuthorizedPerformerOnDefinitions(Collection<Permission> permissions) throws InternalApplicationException {
        authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, SecuredSingleton.DEFINITIONS);
    }

    public void setPermissionsToAuthorizedPerformerOnProcessInstance(Collection<Permission> permissions, WfProcess instance)
            throws InternalApplicationException {
        authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, instance);
    }

    public void setPermissionsToAuthorizedPerformerOnDefinitionByName(Collection<Permission> permissions, String processDefinitionName)
            throws InternalApplicationException {
        WfDefinition definition = definitionService.getLatestProcessDefinition(adminUser, processDefinitionName);
        authorizationService.setPermissions(adminUser, getAuthorizedPerformerActor().getId(), permissions, definition);
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
            Collection<Permission> deployPermissions = Lists.newArrayList(Permission.CREATE);
            setPermissionsToAuthorizedPerformerOnDefinitions(deployPermissions);
            definitionService.deployProcessDefinition(getAuthorizedPerformerUser(), getValidProcessDefinition(), Lists.newArrayList("testProcess"),
                    null);
            Collection<Permission> clearPermissions = Lists.newArrayList();
            setPermissionsToAuthorizedPerformerOnDefinitions(clearPermissions);
        } catch (DefinitionAlreadyExistException e) {
        }
    }

    public void undeployValidProcessDefinition() throws InternalApplicationException {
        Collection<Permission> undeployPermissions = Lists.newArrayList(Permission.ALL);
        setPermissionsToAuthorizedPerformerOnDefinitions(undeployPermissions);
        definitionService.undeployProcessDefinition(getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
    }

    public void deployValidProcessDefinition(String parResourceName) throws IOException {
        try {
            Collection<Permission> deployPermissions = Lists.newArrayList(Permission.CREATE);
            setPermissionsToAuthorizedPerformerOnDefinitions(deployPermissions);
            definitionService.deployProcessDefinition(getAuthorizedPerformerUser(), readBytesFromFile(parResourceName),
                    Lists.newArrayList("testProcess"), null);
            Collection<Permission> clearPermissions = Lists.newArrayList();
            setPermissionsToAuthorizedPerformerOnDefinitions(clearPermissions);
        } catch (DefinitionAlreadyExistException e) {
        }
    }

    public void undeployValidProcessDefinition(String parDefinitionName) throws InternalApplicationException {
        Collection<Permission> undeployPermissions = Lists.newArrayList(Permission.ALL);
        setPermissionsToAuthorizedPerformerOnDefinitions(undeployPermissions);
        definitionService.undeployProcessDefinition(getAuthorizedPerformerUser(), parDefinitionName, null);
    }

    private void createDelegates() {
        definitionService = Delegates.getDefinitionService();
        executionService = Delegates.getExecutionService();
        taskService = Delegates.getTaskService();
        PropertyResources.setDatabaseAvailable(true);
        Delegates.getSystemService().setSetting(SystemProperties.CONFIG_FILE_NAME, "undefined.variables.allowed", "true");
    }

    public DefinitionService getDefinitionService() {
        return definitionService;
    }

    public ExecutionService getExecutionService() {
        return executionService;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public Group getProcessDefinitionAdministratorsGroup() throws InternalApplicationException {
        return (Group) getExecutor(PROCESS_DEFINITION_ADMINISTRATORS);
    }

    public BatchPresentation getProcessDefinitionBatchPresentation() {
        return BatchPresentationFactory.DEFINITIONS.createDefault();
    }

    public BatchPresentation getProcessDefinitionBatchPresentation(String presentationId) {
        return BatchPresentationFactory.DEFINITIONS.createDefault(presentationId);
    }

    public BatchPresentation getProcessInstanceBatchPresentation() {
        return BatchPresentationFactory.CURRENT_PROCESSES.createDefault();
    }

    public BatchPresentation getProcessInstanceBatchPresentation(String presentationId) {
        return BatchPresentationFactory.CURRENT_PROCESSES.createDefault(presentationId);
    }

    public BatchPresentation getTaskBatchPresentation() {
        return BatchPresentationFactory.TASKS.createDefault();
    }

    public BatchPresentation getTaskBatchPresentation(String presentationId) {
        return BatchPresentationFactory.CURRENT_PROCESSES.createDefault(presentationId);
    }

    public static byte[] readBytesFromFile(String fileName) throws IOException {
        InputStream is = WfServiceTestHelper.class.getResourceAsStream(fileName);
        return ByteStreams.toByteArray(is);
    }

    public static Map<String, Object> createVariablesMap(String variableName, Object variableValue) {
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(variableName, variableValue);
        return variablesMap;
    }
}
