package ru.runa.wfe.rest.impl;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.converter.VariableValueUnwrapper;
import ru.runa.wfe.rest.converter.WfeNodeGraphElementMapper;
import ru.runa.wfe.rest.converter.WfeProcessMapper;
import ru.runa.wfe.rest.converter.WfeSwimlaneMapper;
import ru.runa.wfe.rest.converter.WfeTokenMapper;
import ru.runa.wfe.rest.converter.WfeVariableMapper;
import ru.runa.wfe.rest.dto.WfeNodeGraphElement;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeProcess;
import ru.runa.wfe.rest.dto.WfeSwimlane;
import ru.runa.wfe.rest.dto.WfeToken;
import ru.runa.wfe.rest.dto.WfeVariable;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.logic.VariableLogic;

@RestController
@RequestMapping("/process/")
@Transactional
public class ProcessController {
    
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private VariableLogic variableLogic;

    @ValidationException
    @PutMapping("start")
    public Long startProcessByName(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name,
            @RequestBody(required = false) Map<String, Object> variables) {
        Map<String, Object> converted = variables != null
                ? new VariableValueUnwrapper().process(processDefinitionLoader.getLatestDefinition(name), variables)
                : null;
        return executionLogic.startProcess(authUser.getUser(), name, converted);
    }

    @ValidationException
    @PostMapping("{definitionId}/start")
    public Long startProcessById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long definitionId,
            @RequestBody(required = false) Map<String, Object> variables) {
        Map<String, Object> converted = variables != null
                ? new VariableValueUnwrapper().process(processDefinitionLoader.getDefinition(definitionId), variables)
                : null;
        return executionLogic.startProcess(authUser.getUser(), definitionId, converted);
    }

    @PatchMapping("{id}/activate")
    public void activateProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        executionLogic.activateProcess(authUser.getUser(), id);
    }

    @PatchMapping("{id}/suspend")
    public void suspendProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        executionLogic.suspendProcess(authUser.getUser(), id);
    }

    @PatchMapping("{id}/restore")
    public RestoreProcessStatus restoreProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executionLogic.restoreProcess(authUser.getUser(), id);
    }

    @PatchMapping("{id}/upgrade")
    public boolean upgradeProcessToDefinitionVersion(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam Long version) {
        return executionLogic.upgradeProcessToDefinitionVersion(authUser.getUser(), id, version);
    }

    @PatchMapping("upgrade")
    public int upgradeProcessesToDefinitionVersion(@AuthenticationPrincipal AuthUser authUser, @RequestParam Long definitionId,
            @RequestParam Long version) {
        return executionLogic.upgradeProcessesToDefinitionVersion(authUser.getUser(), definitionId, version);
    }

    @PatchMapping("{id}/cancel")
    public void cancelProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        executionLogic.cancelProcess(authUser.getUser(), id);
    }

    @DeleteMapping("{id}")
    public void deleteProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        ProcessFilter filter = new ProcessFilter();
        filter.setId(id);
        executionLogic.deleteProcesses(authUser.getUser(), filter);
    }

    @PostMapping("list")
    public WfePagedList<WfeProcess> getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        BatchPresentation batchPresentation = filter.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS);
        List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
        int total = executionLogic.getProcessesCount(authUser.getUser(), batchPresentation);
        WfeProcessMapper mapper = Mappers.getMapper(WfeProcessMapper.class);
        return new WfePagedList<>(total, mapper.map(processes));
    }

    @GetMapping("{id}")
    public WfeProcess getProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeProcessMapper.class).map(executionLogic.getProcess(authUser.getUser(), id));
    }

    @GetMapping("{id}/parent")
    public WfeProcess getParentProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeProcessMapper.class).map(executionLogic.getParentProcess(authUser.getUser(), id));
    }

    @GetMapping("{id}/subprocesses")
    public List<WfeProcess> getProcessSubprocesses(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) boolean recursive) {
        List<WfProcess> subprocesses = executionLogic.getSubprocesses(authUser.getUser(), id, recursive);
        return Mappers.getMapper(WfeProcessMapper.class).map(subprocesses);
    }

    @GetMapping("{id}/swimlanes")
    public List<WfeSwimlane> getProcessSwimlanes(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<WfSwimlane> swimlanes = executionLogic.getProcessSwimlanes(authUser.getUser(), id);
        return Mappers.getMapper(WfeSwimlaneMapper.class).map(swimlanes);
    }

    @GetMapping("swimlanes")
    public List<WfeSwimlane> getActiveProcessesSwimlanes(@AuthenticationPrincipal AuthUser authUser, @RequestParam String namePattern) {
        List<WfSwimlane> swimlanes = executionLogic.getActiveProcessesSwimlanes(authUser.getUser(), namePattern);
        return Mappers.getMapper(WfeSwimlaneMapper.class).map(swimlanes);
    }

    @PatchMapping("{id}/swimlane")
    public boolean reassignSwimlaneByName(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String name) {
        return executionLogic.reassignSwimlane(authUser.getUser(), id, name);
    }

    @PatchMapping("swimlane/{id}")
    public boolean reassignSwimlaneById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executionLogic.reassignSwimlane(authUser.getUser(), id);
    }

    @PutMapping("swimlane/{id}")
    public void assignSwimlane(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String name,
            @RequestParam Long executorId) {
        executionLogic.assignSwimlane(authUser.getUser(), id, name, executorId);
    }

    @GetMapping("{id}/variables")
    public List<WfeVariable> getProcessVariables(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<WfVariable> variables = variableLogic.getVariables(authUser.getUser(), id);
        return Mappers.getMapper(WfeVariableMapper.class).map(variables);
    }

    @PatchMapping("{id}/variables")
    public void updateProcessVariables(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        WfProcess process = executionLogic.getProcess(authUser.getUser(), id);
        Map<String, Object> converted = new VariableValueUnwrapper().process(processDefinitionLoader.getDefinition(process.getDefinitionVersionId()), variables);
        variableLogic.updateVariables(authUser.getUser(), id, converted);
    }

    @GetMapping("{id}/variable")
    public WfeVariable getProcessVariable(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String name) {
        return Mappers.getMapper(WfeVariableMapper.class).map(variableLogic.getVariable(authUser.getUser(), id, name));
    }

    @GetMapping("{id}/variable/value/file")
    public byte[] getFileVariableValue(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String name) {
        WfVariable variable = variableLogic.getVariable(authUser.getUser(), id, name);
        return variable != null ? ((FileVariable) variable.getValue()).getData() : null;
    }

    @GetMapping("{id}/graph")
    public byte[] getProcessGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) Long childProcessId, @RequestParam(required = false) String subprocessId) {
        return executionLogic.getProcessDiagram(authUser.getUser(), id, taskId, childProcessId, Strings.emptyToNull(subprocessId));
    }

    @GetMapping("{id}/graph/elements")
    public List<WfeNodeGraphElement> getProcessGraphElements(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        List<NodeGraphElement> elements = executionLogic.getProcessDiagramElements(authUser.getUser(), id, Strings.emptyToNull(subprocessId));
        return Mappers.getMapper(WfeNodeGraphElementMapper.class).map(elements);
    }

    @GetMapping("{id}/graph/element")
    public WfeNodeGraphElement getProcessGraphElement(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam String nodeId) {
        return Mappers.getMapper(WfeNodeGraphElementMapper.class).map(executionLogic.getProcessDiagramElement(authUser.getUser(), id, nodeId));
    }

    @GetMapping("{id}/tokens")
    public List<WfeToken> getProcessTokens(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) boolean recursive) {
        List<WfToken> tokens = executionLogic.getTokens(authUser.getUser(), id, recursive, false);
        return Mappers.getMapper(WfeTokenMapper.class).map(tokens);
    }

    @PostMapping("sendSignal")
    public void sendSignal(@RequestBody Map<String, Map<String, ?>> request, @RequestParam Long ttlInSeconds) {
        Utils.sendBpmnMessageRest((Map<String, String>) request.get("routingData"), request.get("payloadData"), ttlInSeconds * 1000);
    }

    @PostMapping("signalReceiverIsActive")
    public boolean signalReceiverIsActive(@RequestBody Map<String, String> routingData) {
        return !executionLogic.findTokensForMessageSelector(routingData).isEmpty();
    }

}
