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
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfProcessDto;
import ru.runa.wfe.rest.dto.WfProcessMapper;
import ru.runa.wfe.rest.dto.WfSwimlaneDto;
import ru.runa.wfe.rest.dto.WfSwimlaneMapper;
import ru.runa.wfe.user.Executor;

@RestController
@RequestMapping("/process/")
@Transactional
public class ProcessApiController {
    
    @Autowired
    private ExecutionLogic executionLogic;
    
    @PostMapping("list")
    public PagedList<WfProcessDto> getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS);
        List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
        int total = executionLogic.getProcessesCount(authUser.getUser(), batchPresentation);
        WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
        return new PagedList<WfProcessDto>(total, mapper.map(processes));
    }

    @PutMapping("{name}")
    public Long start(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name,
            @RequestBody(required = false) Map<String, Object> variables) {
        return executionLogic.startProcess(authUser.getUser(), name, variables);
    }

    @DeleteMapping("{id}")
    public void cancel(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        executionLogic.cancelProcess(authUser.getUser(), id);
    }

    @PostMapping("{definitionId}/start")
    public Long start(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long definitionId, @RequestBody Map<String, Object> variables) {
        return executionLogic.startProcess(authUser.getUser(), definitionId, variables);
    }

    @GetMapping("{id}")
    public WfProcessDto getProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfProcess process = executionLogic.getProcess(authUser.getUser(), id);
        WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
        WfProcessDto processDto = mapper.map(process);
        return processDto;
    }

    @GetMapping("{id}/parent")
    public WfProcessDto getParentProcess(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfProcess process = executionLogic.getParentProcess(authUser.getUser(), id);
        return Mappers.getMapper(WfProcessMapper.class).map(process);
    }

    @GetMapping("{id}/subprocesses")
    public List<WfProcessDto> getSubprocesses(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) boolean recursive) {
        List<WfProcess> subprocesses = executionLogic.getSubprocesses(authUser.getUser(), id, recursive);
        return Mappers.getMapper(WfProcessMapper.class).map(subprocesses);
    }

    @GetMapping("{id}/swimlanes")
    public List<WfSwimlaneDto> getSwimlanes(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<WfSwimlane> swimlanes = executionLogic.getProcessSwimlanes(authUser.getUser(), id);
        return Mappers.getMapper(WfSwimlaneMapper.class).map(swimlanes);
    }

    @GetMapping("swimlanes")
    public List<WfSwimlaneDto> getActiveProcessesSwimlanes(@AuthenticationPrincipal AuthUser authUser, String namePattern) {
        List<WfSwimlane> swimlanes = executionLogic.getActiveProcessesSwimlanes(authUser.getUser(), namePattern);
        return Mappers.getMapper(WfSwimlaneMapper.class).map(swimlanes);
    }

    @PatchMapping("swimlane/{id}")
    public boolean reassignSwimlane(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executionLogic.reassignSwimlane(authUser.getUser(), id);
    }

    @PutMapping("swimlane/{id}")
    public void assignSwimlane(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String name, Executor executor) {
        executionLogic.assignSwimlane(authUser.getUser(), id, name, executor);
    }

    @PostMapping("{id}/graph")
    public String getProcessGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long taskId = (Long) request.get("taskId");
        Long childProcessId = (Long) request.get("childProcessId");
        String subprocessId = (String) request.get("subprocessId");
        byte[] processDiagram = executionLogic.getProcessDiagram(authUser.getUser(), id, taskId, childProcessId, subprocessId);
        return Base64.getEncoder().encodeToString(processDiagram);
    }
}
