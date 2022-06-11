package ru.runa.wfe.rest.impl;

import java.util.Base64;
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
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.RestoreProcessStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.NodeGraphElementDto;
import ru.runa.wfe.rest.dto.NodeGraphElementMapper;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfProcessDto;
import ru.runa.wfe.rest.dto.WfProcessMapper;
import ru.runa.wfe.rest.dto.WfSwimlaneDto;
import ru.runa.wfe.rest.dto.WfSwimlaneMapper;
import ru.runa.wfe.rest.dto.WfTokenDto;
import ru.runa.wfe.rest.dto.WfTokenMapper;
import ru.runa.wfe.rest.dto.WfVariableDto;
import ru.runa.wfe.rest.dto.WfVariableHistoryStateDto;
import ru.runa.wfe.rest.dto.WfVariableHistoryStateMapper;
import ru.runa.wfe.rest.dto.WfVariableMapper;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.logic.VariableLogic;

@RestController
@RequestMapping("/process/")
@Transactional
public class ProcessController {
    
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private VariableLogic variableLogic;

    @PutMapping("{name}")
    public Long startProcessByName(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name,
            @RequestBody(required = false) Map<String, Object> variables) {
        return executionLogic.startProcess(authUser.getUser(), name, variables);
    }

    @PostMapping("{definitionId}/start")
    public Long startProcessById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long definitionId,
            @RequestBody(required = false) Map<String, Object> variables) {
        return executionLogic.startProcess(authUser.getUser(), definitionId, variables);
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
    public boolean upgradeProcessToDefinitionVersion(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, Long version) {
        return executionLogic.upgradeProcessToDefinitionVersion(authUser.getUser(), id, version);
    }

    @PatchMapping("upgrade")
    public int upgradeProcessesToDefinitionVersion(@AuthenticationPrincipal AuthUser authUser, Long definitionId, Long version) {
        return executionLogic.upgradeProcessesToDefinitionVersion(authUser.getUser(), definitionId, version);
    }

    @PatchMapping("{id}/cancel")
    public void cancelProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        executionLogic.cancelProcess(authUser.getUser(), id);
    }

    @DeleteMapping
    public void deleteProcess(@AuthenticationPrincipal AuthUser authUser, @RequestBody ProcessFilter filter) {
        executionLogic.deleteProcesses(authUser.getUser(), filter);
    }

    @PostMapping("list")
    public PagedList<WfProcessDto> getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS);
        List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
        int total = executionLogic.getProcessesCount(authUser.getUser(), batchPresentation);
        WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
        return new PagedList<>(total, mapper.map(processes));
    }

    @GetMapping("{id}")
    public WfProcessDto getProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfProcessMapper.class).map(executionLogic.getProcess(authUser.getUser(), id));
    }

    @GetMapping("{id}/parent")
    public WfProcessDto getParentProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfProcessMapper.class).map(executionLogic.getParentProcess(authUser.getUser(), id));
    }

    @GetMapping("{id}/subprocesses")
    public PagedList<WfProcessDto> getProcessSubprocesses(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) boolean recursive) {
        List<WfProcess> subprocesses = executionLogic.getSubprocesses(authUser.getUser(), id, recursive);
        return new PagedList<>(subprocesses.size(), Mappers.getMapper(WfProcessMapper.class).map(subprocesses));
    }

    @GetMapping("{id}/swimlanes")
    public PagedList<WfSwimlaneDto> getProcessSwimlanes(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<WfSwimlane> swimlanes = executionLogic.getProcessSwimlanes(authUser.getUser(), id);
        return new PagedList<>(swimlanes.size(), Mappers.getMapper(WfSwimlaneMapper.class).map(swimlanes));
    }

    @GetMapping("swimlanes")
    public PagedList<WfSwimlaneDto> getActiveProcessesSwimlanes(@AuthenticationPrincipal AuthUser authUser, String namePattern) {
        List<WfSwimlane> swimlanes = executionLogic.getActiveProcessesSwimlanes(authUser.getUser(), namePattern);
        return new PagedList<>(swimlanes.size(), Mappers.getMapper(WfSwimlaneMapper.class).map(swimlanes));
    }

    @PatchMapping("swimlane/{id}")
    public boolean reassignSwimlane(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executionLogic.reassignSwimlane(authUser.getUser(), id);
    }

    @PutMapping("swimlane/{id}")
    public void assignSwimlane(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String name, Long executorId) {
        executionLogic.assignSwimlane(authUser.getUser(), id, name, executorId);
    }

    @GetMapping("{id}/variables")
    public PagedList<WfVariableDto> getProcessVariables(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<WfVariable> variables = variableLogic.getVariables(authUser.getUser(), id);
        return new PagedList<>(variables.size(), Mappers.getMapper(WfVariableMapper.class).map(variables));
    }

    @PatchMapping("{id}/variables")
    public void updateProcessVariables(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        variableLogic.updateVariables(authUser.getUser(), id, variables);
    }

    @GetMapping("{id}/variable")
    public WfVariableDto getVariable(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String name) {
        return Mappers.getMapper(WfVariableMapper.class).map(variableLogic.getVariable(authUser.getUser(), id, name));
    }

    @GetMapping("{id}/taskVariable")
    public WfVariableDto getTaskVariable(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, Long taskId, String name) {
        return Mappers.getMapper(WfVariableMapper.class).map(variableLogic.getTaskVariable(authUser.getUser(), id, taskId, name));
    }

    @GetMapping("{id}/fileVariableValue")
    public byte[] getFileVariableValue(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String name) {
        WfVariable variable = variableLogic.getVariable(authUser.getUser(), id, name);
        return variable != null ? ((FileVariable) variable.getValue()).getData() : null;
    }

    @PostMapping("historicalVariables")
    public WfVariableHistoryStateDto getHistoricalVariables(@AuthenticationPrincipal AuthUser authUser, @RequestBody ProcessLogFilter filter) {
        return Mappers.getMapper(WfVariableHistoryStateMapper.class).map(variableLogic.getHistoricalVariables(authUser.getUser(), filter));
    }

    @GetMapping("historicalVariables")
    public WfVariableHistoryStateDto getHistoricalVariables(@AuthenticationPrincipal AuthUser authUser, Long id,
            @RequestParam(required = false) Long taskId) {
        return Mappers.getMapper(WfVariableHistoryStateMapper.class).map(variableLogic.getHistoricalVariables(authUser.getUser(), id, taskId));
    }

    @PostMapping("{id}/graph")
    public String getProcessGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long taskId = (Long) request.get("taskId");
        Long childProcessId = (Long) request.get("childProcessId");
        String subprocessId = (String) request.get("subprocessId");
        byte[] processDiagram = executionLogic.getProcessDiagram(authUser.getUser(), id, taskId, childProcessId, subprocessId);
        return Base64.getEncoder().encodeToString(processDiagram);
    }

    @GetMapping("{id}/graph/elements")
    public PagedList<NodeGraphElementDto> getProcessGraphElements(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        List<NodeGraphElement> elements = executionLogic.getProcessDiagramElements(authUser.getUser(), id, subprocessId);
        return new PagedList<>(elements.size(), Mappers.getMapper(NodeGraphElementMapper.class).map(elements));
    }

    @GetMapping("{id}/graph/element")
    public NodeGraphElementDto getProcessGraphElement(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String nodeId) {
        return Mappers.getMapper(NodeGraphElementMapper.class).map(executionLogic.getProcessDiagramElement(authUser.getUser(), id, nodeId));
    }

    @GetMapping("{id}/jobs")
    public PagedList<WfJob> getProcessJobs(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) boolean recursive) {
        List<WfJob> jobs = executionLogic.getJobs(authUser.getUser(), id, recursive);
        return new PagedList<>(jobs.size(), jobs);
    }

    @GetMapping("{id}/tokens")
    public PagedList<WfTokenDto> getProcessTokens(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) boolean recursive) {
        List<WfToken> tokens = executionLogic.getTokens(authUser.getUser(), id, recursive, false);
        return new PagedList<>(tokens.size(), Mappers.getMapper(WfTokenMapper.class).map(tokens));
    }

    @PostMapping("sendSignal")
    public void sendSignal(@RequestBody Map<String, Map<String, ?>> request, Long ttlInSeconds) {
        Utils.sendBpmnMessageRest((Map<String, String>) request.get("routingData"), request.get("payloadData"), ttlInSeconds * 1000);
    }

    @PostMapping("signalReceiverIsActive")
    public boolean signalReceiverIsActive(@RequestBody Map<String, String> routingData) {
        return !executionLogic.findTokensForMessageSelector(routingData).isEmpty();
    }
}
