package ru.runa.wfe.rest.impl;

import java.util.List;
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
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfDefinitionDto;
import ru.runa.wfe.rest.dto.WfDefinitionMapper;

@RestController
@RequestMapping("/definition/")
@Transactional
public class ProcessDefinitionApiController {
    
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;

    @PostMapping("list")
    public PagedList<WfDefinitionDto> getDefinitions(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.DEFINITION);
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        int total = processDefinitionLogic.getProcessDefinitionsCount(authUser.getUser(), batchPresentation);
        return new PagedList<WfDefinitionDto>(total, mapper.map(definitions));
    }
    
    @GetMapping("{versionId}")
    public WfDefinitionDto getDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long versionId) {
        WfDefinition definition = processDefinitionLogic.getProcessDefinition(authUser.getUser(), versionId);
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        WfDefinitionDto definitionDto = mapper.map(definition);
        return definitionDto;
    }

}
