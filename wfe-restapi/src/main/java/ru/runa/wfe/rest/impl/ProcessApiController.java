package ru.runa.wfe.rest.impl;

import java.util.List;
import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ru.runa.wfe.rest.dto.WfDefinitionMapper;
import ru.runa.wfe.rest.dto.WfDefinitionsDto;
import ru.runa.wfe.rest.dto.WfProcessMapper;
import ru.runa.wfe.rest.dto.WfProcessesDto;

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
    public ResponseEntity<WfProcessesDto> getProcesses(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        WfProcessesDto processesDto = new WfProcessesDto();
        try {
            BatchPresentation batchPresentation = request != null 
                    ? request.toBatchPresentation(ClassPresentationType.CURRENT_PROCESS)
                    : BatchPresentationFactory.CURRENT_PROCESSES.createDefault();
            List<WfProcess> processes = executionLogic.getProcesses(authUser.getUser(), batchPresentation);
            WfProcessMapper mapper = Mappers.getMapper(WfProcessMapper.class);
            processesDto.setProcesses(mapper.map(processes));
            List<WfProcess> total = executionLogic.getProcesses(authUser.getUser(), BatchPresentationFactory.CURRENT_PROCESSES.createDefault());
            processesDto.setTotal(total.size());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(processesDto, HttpStatus.OK);
    }
    
    @PostMapping("definition/list")
    public ResponseEntity<WfDefinitionsDto> getDefinitions(@AuthenticationPrincipal AuthUser authUser, @RequestBody(required = false) BatchPresentationRequest request) {
        WfDefinitionsDto definitionsDto = new WfDefinitionsDto();
        try {
            BatchPresentation batchPresentation = request != null 
                    ? request.toBatchPresentation(ClassPresentationType.DEFINITION)
                    : BatchPresentationFactory.DEFINITIONS.createDefault();
            List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
            WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
            definitionsDto.setDefinitions(mapper.map(definitions));
            List<WfDefinition> total = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), BatchPresentationFactory.DEFINITIONS.createDefault(), true);
            definitionsDto.setTotal(total.size());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(definitionsDto, HttpStatus.OK);
    }
}
