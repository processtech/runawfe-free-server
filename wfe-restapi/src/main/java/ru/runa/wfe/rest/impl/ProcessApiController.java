package ru.runa.wfe.rest.impl;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.WfDefinitionDto;
import ru.runa.wfe.rest.dto.WfDefinitionMapper;
import ru.runa.wfe.rest.dto.WfDefinitionsDto;
import ru.runa.wfe.rest.dto.WfProcessMapper;
import ru.runa.wfe.rest.dto.WfProcessesDto;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/process/")
@Transactional
public class ProcessApiController {
    Logger log = LoggerFactory.getLogger(ProcessApiController.class);
    
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;
    
    @PostMapping("list")
    public WfProcessesDto getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        WfProcessesDto processesDto = new WfProcessesDto();
        BatchPresentation batchPresentation = request != null 
                ? request.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS)
                : BatchPresentationFactory.CURRENT_PROCESSES.createDefault();
        List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
        WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
        processesDto.setProcesses(mapper.map(processes));
        List<WfProcess> total = executionLogic.getProcesses(authUser.getUser(), BatchPresentationFactory.CURRENT_PROCESSES.createDefault());
        processesDto.setTotal(total.size());
        return processesDto;
    }
    
    @PostMapping("definition/list")
    public WfDefinitionsDto getDefinitions(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        WfDefinitionsDto definitionsDto = new WfDefinitionsDto();
        BatchPresentation batchPresentation = request != null 
                ? request.toBatchPresentation(ClassPresentationType.DEFINITION)
                : BatchPresentationFactory.DEFINITIONS.createDefault();
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        definitionsDto.setDefinitions(mapper.map(definitions));
        List<WfDefinition> total = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), BatchPresentationFactory.DEFINITIONS.createDefault(), true);
        definitionsDto.setTotal(total.size());
        return definitionsDto;
    }
    
    @PostMapping("{id}/start")
    public Long start(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> variables) {
        User user = authUser.getUser();
        WfProcess process = executionLogic.getProcess(user, id);
        return executionLogic.startProcess(user, process.getName(), variables);
    }

    @GetMapping("{id}/definition")
    public WfDefinitionDto getDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        User user = authUser.getUser();
        WfProcess process = executionLogic.getProcess(user, id);
        WfDefinition definition = processDefinitionLogic.getProcessDefinition(user, process.getDefinitionVersionId());
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        WfDefinitionDto definitionDto = mapper.map(definition);
        return definitionDto;
    }

    @PostMapping("{id}/graph")
    public String getProcessGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody Map<String, Object> request) {
        User user = authUser.getUser();
        Long childProcessId = null;
        if (request.containsKey("childProcessId")) {
            childProcessId = (Long) request.get("childProcessId");
        }
        String subprocessId = null;
        if (request.containsKey("subprocessId")) {
            subprocessId = (String) request.get("subprocessId");
        }
        WfProcess process = executionLogic.getProcess(user, id);
        //TODO Как получить TaskID?
        Long taskId = null;
        byte[] processDiagram = executionLogic.getProcessDiagram(user, process.getId(), taskId, childProcessId, subprocessId);
        
        return Base64.getEncoder().encodeToString(processDiagram);
    }
}
