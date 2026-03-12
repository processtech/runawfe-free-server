package ru.runa.wfe.execution;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.audit.CurrentProcessLog;
import ru.runa.wfe.audit.CurrentVariableDeleteLog;
import ru.runa.wfe.audit.CurrentVariableLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.dao.CurrentNodeProcessDao;
import ru.runa.wfe.execution.dao.CurrentSwimlaneDao;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.job.DueDateInProcessTimerJob;
import ru.runa.wfe.job.dao.TimerJobDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.CurrentVariableDao;
import ru.runa.wfe.var.dao.VariableDao;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.logic.InternalStorageReferenceService;

@CommonsLog
public class ExecutionContext {
    private final ParsedProcessDefinition parsedProcessDefinition;
    private final Token token;
    private final Map<String, Object> transientVariables = Maps.newHashMap();

    private final VariableLoader variableLoader;
    /**
     * This component is used for loading variables with subprocess variables state support.
     */
    private final BaseProcessVariableLoader baseProcessVariableLoader;

    @Autowired
    private VariableCreator variableCreator;
    @Autowired
    private CurrentTokenDao currentTokenDao;
    @Autowired
    private CurrentNodeProcessDao currentNodeProcessDao;
    @Autowired
    private ProcessLogDao processLogDao;
    @Autowired
    private CurrentVariableDao currentVariableDao;
    @Autowired
    private VariableDao variableDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TimerJobDao timerJobDao;
    @Autowired
    private CurrentSwimlaneDao currentSwimlaneDao;
    @Autowired
    private SwimlaneDao swimlaneDao;

    protected ExecutionContext(
            ApplicationContext applicationContext, ParsedProcessDefinition parsedProcessDefinition, Token token,
            Map<Process, Map<String, Variable>> loadedVariables, boolean disableVariableDaoLoading
    ) {
        Preconditions.checkArgument(token != null);
        Preconditions.checkArgument(token.getProcess() != null);
        this.parsedProcessDefinition = parsedProcessDefinition;
        this.token = token;
        Preconditions.checkNotNull(token, "token");
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
        if (disableVariableDaoLoading) {
            this.variableLoader = new VariableLoader(loadedVariables);
        } else {
            this.variableLoader = new VariableLoader(variableDao, loadedVariables);
        }
        this.baseProcessVariableLoader = new BaseProcessVariableLoader(variableLoader, getParsedProcessDefinition(), getProcess());
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token) {
        this(ApplicationContextFactory.getContext(), parsedProcessDefinition, token, null, false);
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process, Map<Process, Map<String, Variable>> loadedVariables,
            boolean disableVariableDaoLoading) {
        this(ApplicationContextFactory.getContext(), parsedProcessDefinition, process.getRootToken(), loadedVariables, disableVariableDaoLoading);
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process) {
        this(parsedProcessDefinition, process.getRootToken());
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Task task) {
        this(parsedProcessDefinition, task.getToken());
    }

    public Map<String, Object> getTransientVariables() {
        return transientVariables;
    }

    public Object getTransientVariable(String name) {
        return transientVariables.get(name);
    }

    public void setTransientVariables(Map<String, Object> transientVariables) {
        if (transientVariables != null) {
            this.transientVariables.putAll(transientVariables);
        }
    }

    public void setTransientVariable(String name, Object value) {
        transientVariables.put(name, value);
    }

    public Node getNode() {
        return getToken().getNodeNotNull(getParsedProcessDefinition());
    }

    public ParsedProcessDefinition getParsedProcessDefinition() {
        return parsedProcessDefinition;
    }

    public Process getProcess() {
        Process process = getToken().getProcess();
        Preconditions.checkNotNull(process, "process");  // TODO Remove this if Precondition inside constructor always succeeds.
        return process;
    }

    public CurrentProcess getCurrentProcess() {
        return (CurrentProcess) getProcess();
    }

    public Token getToken() {
        return token;
    }

    public CurrentToken getCurrentToken() {
        return (CurrentToken) getToken();
    }

    /**
     * @return task or <code>null</code>
     */
    public Task getTask() {
        List<Task> tasks = taskDao.findByProcessAndNodeId(getCurrentProcess(), token.getNodeId());
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    public CurrentNodeProcess getCurrentParentNodeProcess() {
        return currentNodeProcessDao.findBySubProcessId(getCurrentProcess().getId());
    }

    public List<CurrentProcess> getCurrentTokenSubprocesses() {
        return currentNodeProcessDao.getSubprocesses(getCurrentToken());
    }

    public List<CurrentProcess> getCurrentSubprocesses() {
        return currentNodeProcessDao.getSubprocesses(getCurrentProcess());
    }

    public List<CurrentProcess> getCurrentNotEndedSubprocesses() {
        return currentNodeProcessDao.getSubprocesses(getCurrentProcess(), getToken().getNodeId(), getCurrentToken(), false);
    }

    public List<CurrentProcess> getCurrentSubprocessesRecursively() {
        return currentNodeProcessDao.getSubprocessesRecursive(getCurrentProcess());
    }

    /**
     * @return the variable value with the given name.
     */
    public WfVariable getVariable(String name, boolean searchInSwimlanes) {
        if (searchInSwimlanes) {
            SwimlaneDefinition swimlaneDefinition = getParsedProcessDefinition().getSwimlane(name);
            if (swimlaneDefinition != null) {
                Swimlane swimlane = swimlaneDao.findByProcessAndName(getProcess(), swimlaneDefinition.getName());
                if (swimlane == null && !getProcess().isArchived() && SystemProperties.isSwimlaneAutoInitializationEnabled()) {
                    swimlane = currentSwimlaneDao.findOrCreateInitialized(this, swimlaneDefinition, false);
                }
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), swimlane != null ? swimlane.getExecutor() : null);
            }
        }
        WfVariable wfVariable = baseProcessVariableLoader.get(name);
        if (wfVariable != null && wfVariable.getDefinition().isUserType()
                && wfVariable.getDefinition().getUserType().isByReference()) {
            wfVariable = resolveByReferenceVariable(wfVariable);
        }
        if (wfVariable != null && isContainerOfByReference(wfVariable.getDefinition())) {
            wfVariable = resolveByReferenceContainer(wfVariable);
        }
        return wfVariable;
    }

    /**
     * @return the variable or swimlane value with the given name.
     */
    public Object getVariableValue(String name) {
        WfVariable variable = getVariable(name, true);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    public void setVariableValue(@NonNull String name, Object value) {
        Preconditions.checkState(!token.isArchived());
        SwimlaneDefinition swimlaneDefinition = getParsedProcessDefinition().getSwimlane(name);
        if (swimlaneDefinition != null) {
            log.debug("Assigning swimlane '" + name + "' value '" + value + "'");
            CurrentSwimlane swimlane = currentSwimlaneDao.findOrCreate(getCurrentProcess(), swimlaneDefinition);
            swimlane.assignExecutor(this, (Executor) convertValueForVariableType(swimlaneDefinition.toVariableDefinition(), value), true);
            return;
        }
        VariableDefinition variableDefinition = getParsedProcessDefinition().getVariable(name, false);
        if (variableDefinition == null) {
            if (value == null) {
                return;
            }
            throw new InternalApplicationException("Variable '" + name + "' is not defined in process definition");
        }
        setVariableValue(variableDefinition, value);
    }

    /**
     * Adds all the given variables. It doesn't remove any existing variables unless they are overwritten by the given variables.
     */
    public void setVariableValues(Map<String, Object> variables) {
        Preconditions.checkState(!token.isArchived());
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariableValue(entry.getKey(), entry.getValue());
        }
    }

    public VariableProvider getVariableProvider() {
        return new ExecutionVariableProvider(this);
    }

    public void addLog(CurrentProcessLog processLog) {
        processLogDao.addLog(processLog, getCurrentProcess(), getCurrentToken());
    }

    public void activateTokenIfHasPreviousError() {
        val p = getCurrentProcess();
        val t = getCurrentToken();

        if (t.getExecutionStatus() == ExecutionStatus.FAILED) {
            t.setExecutionStatus(ExecutionStatus.ACTIVE);
            t.setErrorDate(null);
            t.setErrorMessage(null);
            List<CurrentToken> failedTokens = currentTokenDao.findByProcessAndExecutionStatus(p, ExecutionStatus.FAILED);
            if (failedTokens.isEmpty()) {
                p.setExecutionStatus(ExecutionStatus.ACTIVE);
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("processId", getToken().getProcess().getId()).add("tokenId", getToken().getId()).toString();
    }

    private void setVariableValue(VariableDefinition variableDefinition, Object value) {
        Preconditions.checkNotNull(variableDefinition, "variableDefinition");
        if (variableDefinition.isUserType() && variableDefinition.getUserType().isByReference()) {
            handleByReferenceWrite(variableDefinition, value);
            return;
        }
        if (isContainerOfByReference(variableDefinition)) {
            handleByReferenceContainerWrite(variableDefinition, value);
            return;
        }
        switch (variableDefinition.getStoreType()) {
            case BLOB: {
                setSimpleVariableValue(getCurrentToken(), variableDefinition, value);
                break;
            }
            case TRANSIENT: {
                setTransientVariable(variableDefinition.getName(), value);
                break;
            }
            case DEFAULT: {
                ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesOnSaveContext(
                        variableDefinition, value, getCurrentProcess(), baseProcessVariableLoader, currentVariableDao
                );
                VariableFormat variableFormat = variableDefinition.getFormatNotNull();
                for (ConvertToSimpleVariablesResult simpleVariables : variableFormat.processBy(new ConvertToSimpleVariables(), context)) {
                    Object convertedValue = convertValueForVariableType(simpleVariables.variableDefinition, simpleVariables.value);
                    setSimpleVariableValue(getCurrentToken(), simpleVariables.variableDefinition, convertedValue);
                }
                break;
            }
            default: {
                throw new InternalApplicationException("Unexpected " + variableDefinition.getStoreType());
            }
        }
    }

    private Object convertValueForVariableType(final VariableDefinition variableDefinition, final Object value) {
        if (SystemProperties.isV3CompatibilityMode() || value == null) {
            return value;
        }
        if (!SystemProperties.isStrongVariableFormatEnabled()) {
            return value;
        }
        Class<?> definedClass = variableDefinition.getFormatNotNull().getJavaClass();
        if (!definedClass.isAssignableFrom(value.getClass())) {
            if (SystemProperties.isVariableAutoCastingEnabled()) {
                try {
                    if (Executor.class.isAssignableFrom(definedClass) && value instanceof List) {
                        Group tmpGroup = TemporaryGroup.create(getProcess().getId(), variableDefinition.getName());
                        List<Executor> executors = Lists.newArrayList();
                        for (Object executorIdentity : (List) value) {
                            executors.add(TypeConversionUtil.convertTo(Executor.class, executorIdentity));
                        }
                        tmpGroup = ApplicationContextFactory.getExecutorLogic().saveTemporaryGroup(tmpGroup, executors);
                        return tmpGroup;
                    }
                    return TypeConversionUtil.convertTo(definedClass, value);
                } catch (Exception e) {
                    throw new InternalApplicationException("Variable '" + variableDefinition.getName() + "' defined as '" + definedClass
                            + "' but value is instance of '" + value.getClass() + "' can't be converted", e);
                }
            } else {
                throw new InternalApplicationException("Variable '" + variableDefinition.getName() + "' defined as '" + definedClass
                        + "' but value is instance of '" + value.getClass() + "'");
            }
        }
        return value;
    }

    private CurrentVariableLog setSimpleVariableValue(CurrentToken token, VariableDefinition variableDefinition, Object value) {
        CurrentVariableLog resultingVariableLog = null;
        CurrentVariable<?> variable = (CurrentVariable<?>) variableLoader.get(token.getProcess(), variableDefinition.getName());
        // if there is exist variable and it doesn't support the current type
        if (variable != null && !variable.supports(value)) {
            String converterStr = variable.getConverter() == null ? "" : " converter is " + variable.getConverter();
            log.debug("Variable type is changing: deleting old variable '" + variableDefinition.getName() + "' in " + token.getProcess()
                    + " variable value is " + value + converterStr);
            currentVariableDao.delete(variable);
            currentVariableDao.flushPendingChanges();
            resultingVariableLog = new CurrentVariableDeleteLog(variable);
            variable = null;
        }
        final BaseProcessVariableLoader.SubprocessSyncCache subprocessSyncCache = baseProcessVariableLoader.getSubprocessSyncCache();
        if (variable == null) {
            VariableDefinition syncVariableDefinition = subprocessSyncCache.getParentProcessSyncVariableDefinition(
                    token.getProcess(), variableDefinition
            );
            if (syncVariableDefinition != null) {
                CurrentToken parentToken = (CurrentToken) subprocessSyncCache.getParentProcessToken(token.getProcess());
                log.debug("Setting " + token.getProcess().getId() + "." + variableDefinition.getName() + " in parent process "
                        + parentToken.getProcess().getId() + "." + syncVariableDefinition.getName());
                CurrentVariableLog parentVariableLog = setSimpleVariableValue(parentToken, syncVariableDefinition, value);
                if (parentVariableLog != null) {
                    CurrentVariableLog markingVariableLog = parentVariableLog.getContentCopy();
                    markingVariableLog.setVariableName(variableDefinition.getName());
                    resultingVariableLog = markingVariableLog;
                }
            }
            if (value != null) {
                if (syncVariableDefinition == null || !subprocessSyncCache.isInBaseProcessIdMode(token.getProcess())) {
                    variable = (CurrentVariable) variableCreator.create(token.getProcess(), variableDefinition, value);
                    resultingVariableLog = variable.setValue(this, value, variableDefinition);
                    currentVariableDao.create(variable);
                }
            }
        } else {
            if (Objects.equal(value, variable.getValue())) {
                // order is valuable due to Timestamp.equals implementation
                return null;
            }
            if (ApplicationContextFactory.getDbType() == DbType.ORACLE && Utils.isNullOrEmpty(value) && Utils.isNullOrEmpty(variable.getValue())) {
                // ignore changes "" -> " " for Oracle
                return null;
            }
            log.debug("Updating variable '" + variableDefinition.getName() + "' in '" + getProcess() + "' to '" + value + "'"
                    + (value != null ? " of " + value.getClass() : ""));
            resultingVariableLog = variable.setValue(this, value, variableDefinition);
            VariableDefinition syncVariableDefinition = subprocessSyncCache.getParentProcessSyncVariableDefinition(
                    token.getProcess(), variableDefinition
            );
            if (syncVariableDefinition != null) {
                CurrentToken parentToken = (CurrentToken) subprocessSyncCache.getParentProcessToken(token.getProcess());
                log.debug("Setting " + token.getProcess().getId() + "." + variableDefinition.getName() + " in parent process "
                        + parentToken.getProcess().getId() + "." + syncVariableDefinition.getName());
                setSimpleVariableValue(parentToken, syncVariableDefinition, value);
            }
        }
        if (value instanceof Date) {
            updateRelatedObjectsDueToDateVariableChange(variableDefinition.getName());
        }
        if (resultingVariableLog != null) {
            processLogDao.addLog(resultingVariableLog, token.getProcess(), token);
        }
        return resultingVariableLog;
    }

    private WfVariable resolveByReferenceVariable(WfVariable wfVariable) {
        Object value = wfVariable.getValue();
        if (value == null) {
            return wfVariable;
        }
        Long id = null;
        if (value instanceof UserTypeMap) {
            Object rawId = ((UserTypeMap) value).get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
            if (rawId != null) {
                id = TypeConversionUtil.convertTo(Long.class, rawId);
            }
        }
        if (id == null) {
            return wfVariable;
        }
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();
        UserTypeMap fullMap = refService.loadById(wfVariable.getDefinition().getUserType(), id);
        if (fullMap == null) {
            log.warn("byReference: row with id=" + id + " not found in InternalStorage for type "
                    + wfVariable.getDefinition().getUserType().getName());
            return wfVariable;
        }
        return new WfVariable(wfVariable.getDefinition(), fullMap);
    }

    private WfVariable resolveByReferenceContainer(WfVariable wfVariable) {
        Object value = wfVariable.getValue();
        if (value == null) {
            return wfVariable;
        }
        UserType[] componentUserTypes = wfVariable.getDefinition().getFormatComponentUserTypes();
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();

        if (value instanceof List) {
            UserType componentUserType = componentUserTypes.length > 0 ? componentUserTypes[0] : null;
            if (componentUserType == null || !componentUserType.isByReference()) {
                return wfVariable;
            }
            List<Object> list = (List<Object>) value;
            List<Object> resolvedList = Lists.newArrayListWithCapacity(list.size());
            for (Object element : list) {
                if (element instanceof UserTypeMap) {
                    Object rawId = ((UserTypeMap) element).get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
                    if (rawId != null) {
                        Long id = TypeConversionUtil.convertTo(Long.class, rawId);
                        UserTypeMap fullMap = refService.loadById(componentUserType, id);
                        if (fullMap != null) {
                            resolvedList.add(fullMap);
                        } else {
                            log.warn("byReference: row with id=" + id + " not found for list component type " + componentUserType.getName());
                            resolvedList.add(element);
                        }
                    } else {
                        resolvedList.add(element);
                    }
                } else {
                    resolvedList.add(element);
                }
            }

            return new WfVariable(wfVariable.getDefinition(), resolvedList);
        } else if (value instanceof Map) {
            UserType keyUserType = componentUserTypes.length > 0 ? componentUserTypes[0] : null;
            UserType valueUserType = componentUserTypes.length > 1 ? componentUserTypes[1] : null;
            Map<Object, Object> map = (Map<Object, Object>) value;
            Map<Object, Object> resolvedMap = Maps.newLinkedHashMap();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Object resolvedKey = resolveByReferenceComponent(entry.getKey(), keyUserType, refService);
                Object resolvedValue = resolveByReferenceComponent(entry.getValue(), valueUserType, refService);
                resolvedMap.put(resolvedKey, resolvedValue);
            }
            return new WfVariable(wfVariable.getDefinition(), resolvedMap);
        }
        return wfVariable;
    }

    private Object resolveByReferenceComponent(Object component, UserType userType, InternalStorageReferenceService refService) {
        if (userType == null || !userType.isByReference() || !(component instanceof UserTypeMap)) {
            return component;
        }
        Object rawId = ((UserTypeMap) component).get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
        if (rawId == null) {
            return component;
        }
        Long id = TypeConversionUtil.convertTo(Long.class, rawId);
        UserTypeMap fullMap = refService.loadById(userType, id);
        if (fullMap != null) {
            return fullMap;
        }
        log.warn("byReference: row with id=" + id + " not found for map component type " + userType.getName());
        return component;
    }

    private void handleByReferenceWrite(VariableDefinition variableDefinition, Object value) {
        UserType userType = variableDefinition.getUserType();
        InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();

        if (value == null) {
            Long oldId = getExistingByReferenceId(variableDefinition);
            if (oldId != null) {
                refService.delete(userType, oldId);
                log.info("byReference: implicit delete from Excel for variable '" + variableDefinition.getName()
                        + "', type=" + userType.getName() + ", old id=" + oldId);
                UserTypeMap deletedMap = new UserTypeMap(userType);
                deletedMap.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME, oldId);
                logByReferenceFullValues("DELETE", variableDefinition, oldId, deletedMap);
            }
            saveByReferenceIdToDb(variableDefinition, null);
            return;
        }
        if (!(value instanceof UserTypeMap)) {
            throw new InternalApplicationException(
                    "byReference variable '" + variableDefinition.getName() + "' expects UserTypeMap, got " + value.getClass());
        }

        UserTypeMap fullMap = (UserTypeMap) value;
        Long id = getExistingByReferenceId(variableDefinition);

        if (id == null) {
            if (!hasNonIdAttributes(fullMap)) {
                log.debug("byReference: skipping INSERT for variable '" + variableDefinition.getName()
                        + "' — all attributes are null (uninitialized variable)");
                return;
            }
            fullMap.remove(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
            long newId = refService.insert(userType, fullMap);
            saveByReferenceIdToDb(variableDefinition, newId);
            logByReferenceFullValues("INSERT", variableDefinition, newId, fullMap);
        } else {
            if (hasNonIdAttributes(fullMap)) {
                refService.update(userType, id, fullMap);
                logByReferenceFullValues("UPDATE", variableDefinition, id, fullMap);
            }
            saveByReferenceIdToDb(variableDefinition, id);
        }
    }

    private Long getExistingByReferenceId(VariableDefinition variableDefinition) {
        String idVariableName = variableDefinition.getName() + UserType.DELIM
                + InternalStorageReferenceService.ID_ATTRIBUTE_NAME;
        Variable<?, ?> idVar = variableLoader.get(getProcess(), idVariableName);
        if (idVar != null && idVar.getValue() != null) {
            return TypeConversionUtil.convertTo(Long.class, idVar.getValue());
        }
        return null;
    }

    private void logByReferenceFullValues(String operation, VariableDefinition variableDefinition, Long id, UserTypeMap fullMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("byReference ").append(operation).append(": variable='").append(variableDefinition.getName())
                .append("', type='").append(variableDefinition.getUserType().getName())
                .append("', id=").append(id).append(", values={");
        boolean first = true;
        for (VariableDefinition attr : variableDefinition.getUserType().getAttributes()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(attr.getName()).append("=").append(fullMap.get(attr.getName()));
            first = false;
        }
        sb.append("}");
        String fullValuesString = sb.toString();
        log.info(fullValuesString);

        try {
            CurrentActionLog processLog = new CurrentActionLog();
            processLog.setVariableName(variableDefinition.getName());
            String attrValue = fullValuesString;
            if (attrValue.length() > 4000) {
                attrValue = attrValue.substring(0, 3997) + "...";
            }
            processLog.getAttributes().put(CurrentActionLog.ATTR_ACTION, attrValue);
            processLogDao.addLog(processLog, getCurrentProcess(), getCurrentToken());
        } catch (Exception e) {
            log.warn("byReference: failed to write process log for " + operation + " of variable '"
                    + variableDefinition.getName() + "'", e);
        }
    }

    private void saveByReferenceIdToDb(VariableDefinition variableDefinition, Long id) {
        UserTypeMap idOnlyMap = null;
        if (id != null) {
            idOnlyMap = new UserTypeMap(variableDefinition.getUserType());
            idOnlyMap.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME, id);
        }
        ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesOnSaveContext(
                variableDefinition, idOnlyMap, getCurrentProcess(), baseProcessVariableLoader, currentVariableDao
        );
        VariableFormat variableFormat = variableDefinition.getFormatNotNull();
        for (ConvertToSimpleVariablesResult simpleVariables : variableFormat.processBy(new ConvertToSimpleVariables(), context)) {
            Object convertedValue = convertValueForVariableType(simpleVariables.variableDefinition, simpleVariables.value);
            setSimpleVariableValue(getCurrentToken(), simpleVariables.variableDefinition, convertedValue);
        }
    }

    private boolean isContainerOfByReference(VariableDefinition variableDefinition) {
        UserType[] componentUserTypes = variableDefinition.getFormatComponentUserTypes();
        if (componentUserTypes == null || componentUserTypes.length == 0) {
            return false;
        }
        for (UserType componentUserType : componentUserTypes) {
            if (componentUserType != null && componentUserType.isByReference()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNonIdAttributes(UserTypeMap map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (InternalStorageReferenceService.ID_ATTRIBUTE_NAME.equals(entry.getKey())) {
                continue;
            }
            Object val = entry.getValue();
            if (val == null) {
                continue;
            }
            if (val instanceof String && ((String) val).isEmpty()) {
                continue;
            }
            return true;
        }
        return false;
    }

    private void handleByReferenceContainerWrite(VariableDefinition variableDefinition, Object value) {
        UserType[] componentUserTypes = variableDefinition.getFormatComponentUserTypes();

        if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            UserType componentUserType = componentUserTypes.length > 0 ? componentUserTypes[0] : null;
            List<Object> idOnlyList = Lists.newArrayListWithCapacity(list.size());
            if (componentUserType != null && componentUserType.isByReference()) {
                InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();

                for (int i = 0; i < list.size(); i++) {
                    Object element = list.get(i);
                    if (element instanceof UserTypeMap) {
                        UserTypeMap fullMap = (UserTypeMap) element;

                        Long id = null;
                        Object rawId = fullMap.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
                        if (rawId != null) {
                            try {
                                id = TypeConversionUtil.convertTo(Long.class, rawId);
                            } catch (Exception e) {
                                log.debug("byReference container: cannot parse id '" + rawId + "' for [" + i
                                        + "], treating as new element");
                            }
                        }

                        if (id != null && id > 0) {
                            if (hasNonIdAttributes(fullMap)) {
                                log.info("byReference container write [" + i + "]: UPDATE id=" + id);
                                refService.update(componentUserType, id, fullMap);
                            }
                        } else {
                            if (!hasNonIdAttributes(fullMap)) {
                                log.debug("byReference container: skipping empty element [" + i + "]");
                                continue;
                            }
                            fullMap.remove(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
                            long newId = refService.insert(componentUserType, fullMap);
                            log.info("byReference container write [" + i + "]: INSERT → id=" + newId);
                            id = newId;
                        }

                        UserTypeMap idOnly = new UserTypeMap(componentUserType);
                        idOnly.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME, id);
                        idOnlyList.add(idOnly);
                    } else {
                        idOnlyList.add(element);
                    }
                }
            } else {
                idOnlyList.addAll(list);
            }
            saveContainerToDb(variableDefinition, idOnlyList);
        } else if (value instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) value;
            UserType keyUserType = componentUserTypes.length > 0 ? componentUserTypes[0] : null;
            UserType valueUserType = componentUserTypes.length > 1 ? componentUserTypes[1] : null;
            Map<Object, Object> idOnlyMap = Maps.newLinkedHashMap();
            InternalStorageReferenceService refService = ApplicationContextFactory.getInternalStorageReferenceService();
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Object mapKey = processMapComponent(entry.getKey(), keyUserType, refService);
                Object mapValue = processMapComponent(entry.getValue(), valueUserType, refService);
                idOnlyMap.put(mapKey, mapValue);
            }
            saveContainerToDb(variableDefinition, idOnlyMap);
        } else if (value == null) {
            saveContainerToDb(variableDefinition, null);
        } else {
            throw new InternalApplicationException(
                    "byReference container variable '" + variableDefinition.getName() + "' expects List or Map, got " + value.getClass());
        }
    }

    private Object processMapComponent(Object component, UserType userType, InternalStorageReferenceService refService) {
        if (userType == null || !userType.isByReference() || !(component instanceof UserTypeMap)) {
            return component;
        }
        UserTypeMap fullMap = (UserTypeMap) component;
        Object rawId = fullMap.get(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
        Long id = (rawId != null) ? TypeConversionUtil.convertTo(Long.class, rawId) : null;
        if (id == null) {
            fullMap.remove(InternalStorageReferenceService.ID_ATTRIBUTE_NAME);
            id = refService.insert(userType, fullMap);
        } else if (hasNonIdAttributes(fullMap)) {
            refService.update(userType, id, fullMap);
        }
        UserTypeMap idOnly = new UserTypeMap(userType);
        idOnly.put(InternalStorageReferenceService.ID_ATTRIBUTE_NAME, id);
        return idOnly;
    }

    private void saveContainerToDb(VariableDefinition variableDefinition, Object idOnlyValue) {
        ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesOnSaveContext(
                variableDefinition, idOnlyValue, getCurrentProcess(), baseProcessVariableLoader, currentVariableDao
        );
        VariableFormat variableFormat = variableDefinition.getFormatNotNull();
        for (ConvertToSimpleVariablesResult simpleVariables : variableFormat.processBy(new ConvertToSimpleVariables(), context)) {
            Object convertedValue = convertValueForVariableType(simpleVariables.variableDefinition, simpleVariables.value);
            setSimpleVariableValue(getCurrentToken(), simpleVariables.variableDefinition, convertedValue);
        }
    }

    private void updateRelatedObjectsDueToDateVariableChange(String variableName) {
        List<Task> tasks = taskDao.findByProcessAndDeadlineExpressionContaining(getCurrentProcess(), variableName);
        for (Task task : tasks) {
            Date oldDate = task.getDeadlineDate();
            task.setDeadlineDate(ExpressionEvaluator.evaluateDueDate(getVariableProvider(), task.getDeadlineDateExpression()));
            log.info(String.format("Changed deadlineDate for %s from %s to %s", task, oldDate, task.getDeadlineDate()));
        }
        List<DueDateInProcessTimerJob> jobs = timerJobDao.findByProcessAndDeadlineExpressionContaining(getCurrentProcess(), variableName);
        for (val job : jobs) {
            Date oldDate = job.getDueDate();
            job.setDueDate(ExpressionEvaluator.evaluateDueDate(getVariableProvider(), job.getDueDateExpression()));
            log.info(String.format("Changed dueDate for %s from %s to %s", job, oldDate, job.getDueDate()));
        }
    }
}
