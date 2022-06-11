package ru.runa.wfe.rest.impl;

import java.util.List;
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
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.InteractionDto;
import ru.runa.wfe.rest.dto.InteractionMapper;
import ru.runa.wfe.rest.dto.NodeGraphElementDto;
import ru.runa.wfe.rest.dto.NodeGraphElementMapper;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.SwimlaneDefinitionDto;
import ru.runa.wfe.rest.dto.SwimlaneDefinitionMapper;
import ru.runa.wfe.rest.dto.UserTypeDto;
import ru.runa.wfe.rest.dto.UserTypeMapper;
import ru.runa.wfe.rest.dto.VariableDefinitionDto;
import ru.runa.wfe.rest.dto.VariableDefinitionMapper;
import ru.runa.wfe.rest.dto.WfDefinitionDto;
import ru.runa.wfe.rest.dto.WfDefinitionMapper;
import ru.runa.wfe.rest.dto.WfNodeDto;
import ru.runa.wfe.rest.dto.WfNodeMapper;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

@RestController
@RequestMapping("/definition/")
@Transactional
public class ProcessDefinitionController {
    
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;

    @PutMapping()
    public WfDefinitionDto deployProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @RequestBody byte[] par,
            @RequestParam List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        WfDefinition definition = processDefinitionLogic.deployProcessDefinition(authUser.getUser(), par, categories, secondsBeforeArchiving);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @PatchMapping("{id}/redeploy")
    public WfDefinitionDto redeployProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody byte[] par,
            @RequestParam List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        WfDefinition definition = processDefinitionLogic.redeployProcessDefinition(authUser.getUser(), id, par, categories, secondsBeforeArchiving);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @PatchMapping("{id}/update")
    public WfDefinitionDto updateProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody byte[] par) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.updateProcessDefinition(authUser.getUser(), id, par));
    }

    @DeleteMapping()
    public void undeployProcessDefinition(@AuthenticationPrincipal AuthUser authUser, String name, Long version) {
        processDefinitionLogic.undeployProcessDefinition(authUser.getUser(), name, version);
    }

    @PostMapping("list")
    public PagedList<WfDefinitionDto> getProcessDefinitions(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.DEFINITION);
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        int total = processDefinitionLogic.getProcessDefinitionsCount(authUser.getUser(), batchPresentation);
        return new PagedList<>(total, mapper.map(definitions));
    }

    @GetMapping("{name}/history")
    public PagedList<WfDefinitionDto> getProcessDefinitionHistory(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name) {
        List<WfDefinition> history = processDefinitionLogic.getProcessDefinitionHistory(authUser.getUser(), name);
        return new PagedList<>(history.size(), Mappers.getMapper(WfDefinitionMapper.class).map(history));
    }

    @GetMapping("{name}/latest")
    public WfDefinitionDto getLatestProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getLatestProcessDefinition(authUser.getUser(), name));
    }

    @GetMapping("{name}/version")
    public WfDefinitionDto getProcessDefinitionByNameAndVersion(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name, Long version) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinitionVersion(authUser.getUser(), name, version));
    }

    @GetMapping("{versionId}")
    public WfDefinitionDto getProcessDefinitionByVersionId(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long versionId) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinition(authUser.getUser(), versionId));
    }

    @GetMapping("{id}/node")
    public WfNodeDto getProcessDefinitionNode(@PathVariable Long id, String nodeId) {
        Node node = processDefinitionLogic.getDefinition(id).getNode(nodeId);
        return (node != null) ? Mappers.getMapper(WfNodeMapper.class).map(new WfNode(node)) : null;
    }

    @GetMapping("{id}/file")
    public byte[] getProcessDefinitionFile(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String fileName) {
        return processDefinitionLogic.getFile(authUser.getUser(), id, fileName);
    }

    @GetMapping("{id}/graph")
    public byte[] getProcessDefinitionGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        return processDefinitionLogic.getGraph(authUser.getUser(), id, subprocessId);
    }

    @GetMapping("{id}/graph/elements")
    public PagedList<NodeGraphElementDto> getProcessDefinitionGraphElements(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        List<NodeGraphElement> elements = processDefinitionLogic.getProcessDefinitionGraphElements(authUser.getUser(), id, subprocessId);
        return new PagedList<>(elements.size(), Mappers.getMapper(NodeGraphElementMapper.class).map(elements));
    }

    @GetMapping("{id}/interaction")
    public InteractionDto getProcessDefinitionStartInteraction(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(InteractionMapper.class).map(processDefinitionLogic.getStartInteraction(authUser.getUser(), id));
    }

    @GetMapping("{id}/interaction/{nodeId}")
    public InteractionDto getProcessDefinitionTaskNodeInteraction(@PathVariable Long id, @PathVariable String nodeId) {
        return Mappers.getMapper(InteractionMapper.class).map(processDefinitionLogic.getDefinition(id).getInteractionNotNull(nodeId));
    }

    @GetMapping("{id}/swimlanes")
    public PagedList<SwimlaneDefinitionDto> getProcessDefinitionSwimlanes(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<SwimlaneDefinition> swimlanes = processDefinitionLogic.getSwimlanes(authUser.getUser(), id);
        return new PagedList<>(swimlanes.size(), Mappers.getMapper(SwimlaneDefinitionMapper.class).map(swimlanes));
    }

    @GetMapping("{id}/userTypes")
    public PagedList<UserTypeDto> getProcessDefinitionUserTypes(@PathVariable Long id) {
        List<UserType> userTypes = processDefinitionLogic.getDefinition(id).getUserTypes();
        return new PagedList<>(userTypes.size(), Mappers.getMapper(UserTypeMapper.class).map(userTypes));
    }

    @GetMapping("{id}/userType")
    public UserTypeDto getProcessDefinitionUserType(@PathVariable Long id, String name) {
        return Mappers.getMapper(UserTypeMapper.class).map(processDefinitionLogic.getDefinition(id).getUserType(name));
    }

    @GetMapping("{id}/variables")
    public PagedList<VariableDefinitionDto> getProcessDefinitionVariableDefinitions(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<VariableDefinition> variableDefinitions = processDefinitionLogic.getProcessDefinitionVariables(authUser.getUser(), id);
        return new PagedList<>(variableDefinitions.size(), Mappers.getMapper(VariableDefinitionMapper.class).map(variableDefinitions));
    }

    @GetMapping("{id}/variable")
    public VariableDefinitionDto getProcessDefinitionVariableDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String name) {
        VariableDefinition variableDefinition = processDefinitionLogic.getProcessDefinitionVariable(authUser.getUser(), id, name);
        return Mappers.getMapper(VariableDefinitionMapper.class).map(variableDefinition);
    }

}
