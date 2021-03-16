package ru.runa.wfe.rest.impl;

import java.util.List;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
import ru.runa.wfe.rest.dto.WfProcessMapper;
import ru.runa.wfe.rest.dto.WfProcessesDto;

@RestController
@RequestMapping("/process/")
@Transactional
public class ProcessApiController {
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;
    
    @PostMapping("list")
    public WfProcessesDto getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request != null 
                ? request.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS)
                : BatchPresentationFactory.CURRENT_PROCESSES.createDefault();
        List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
        WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
        WfProcessesDto processesDto = new WfProcessesDto();
        processesDto.setProcesses(mapper.map(processes));
        List<WfProcess> total = executionLogic.getProcesses(authUser.getUser(), BatchPresentationFactory.CURRENT_PROCESSES.createDefault());
        processesDto.setTotal(total.size());
        return processesDto;
    }
    
    @PostMapping("definition/list")
    public List<WfDefinitionDto> getDefinitions(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request != null 
                ? request.toBatchPresentation(ClassPresentationType.DEFINITION)
                : BatchPresentationFactory.DEFINITIONS.createDefault();
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        return mapper.map(definitions);
    }
}
