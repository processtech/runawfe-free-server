package ru.runa.wfe.rest.impl;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfProcessDto;
import ru.runa.wfe.rest.dto.WfProcessMapper;

@RestController
@RequestMapping("/process/")
@Transactional
public class ProcessApiController {
    
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;
    
    @PostMapping("list")
    public PagedList<WfProcessDto> getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS);
        List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
        int total = executionLogic.getProcessesCount(authUser.getUser(), batchPresentation);
        WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
        return new PagedList<WfProcessDto>(total, mapper.map(processes));
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

    @PostMapping("{id}/graph")
    public String getProcessGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> request) {
        Long taskId = (Long) request.get("taskId");
        Long childProcessId = (Long) request.get("childProcessId");
        String subprocessId = (String) request.get("subprocessId");
        byte[] processDiagram = executionLogic.getProcessDiagram(authUser.getUser(), id, taskId, childProcessId, subprocessId);
        return Base64.getEncoder().encodeToString(processDiagram);
    }
}
