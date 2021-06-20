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
import ru.runa.wfe.lang.Node;
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

@RestController
@RequestMapping("/definition/")
@Transactional
public class ProcessDefinitionApiController {
    
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;

    @PutMapping()
    public WfDefinitionDto deploy(@AuthenticationPrincipal AuthUser authUser, @RequestBody byte[] par,
            @RequestParam List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(
                processDefinitionLogic.deployProcessDefinition(authUser.getUser(), par, categories, secondsBeforeArchiving));
    }

    @PatchMapping("{id}/redeploy")
    public WfDefinitionDto redeploy(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody byte[] par,
            @RequestParam List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(
                processDefinitionLogic.redeployProcessDefinition(authUser.getUser(), id, par, categories, secondsBeforeArchiving));
    }

    @PatchMapping("{id}/update")
    public WfDefinitionDto update(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody byte[] par) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.updateProcessDefinition(authUser.getUser(), id, par));
    }

    @DeleteMapping()
    public void undeploy(@AuthenticationPrincipal AuthUser authUser, String name, Long version) {
        processDefinitionLogic.undeployProcessDefinition(authUser.getUser(), name, version);
    }

    @GetMapping("{name}/latest")
    public WfDefinitionDto getLatest(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getLatestProcessDefinition(authUser.getUser(), name));
    }

    @GetMapping("{name}/version")
    public WfDefinitionDto getVersion(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name, Long version) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinitionVersion(authUser.getUser(), name, version));
    }

    @GetMapping("{name}/history")
    public List<WfDefinitionDto> getHistory(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinitionHistory(authUser.getUser(), name));
    }

    @GetMapping("{id}")
    public WfDefinitionDto getDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinition(authUser.getUser(), id));
    }

    @GetMapping("{id}/node")
    public WfNodeDto getNode(@PathVariable Long id, String nodeId) {
        Node node = processDefinitionLogic.getDefinition(id).getNode(nodeId);
        return (node != null) ? Mappers.getMapper(WfNodeMapper.class).map(new WfNode(node)) : null;
    }

    @GetMapping("{id}/file")
    public byte[] getFile(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String fileName) {
        return processDefinitionLogic.getFile(authUser.getUser(), id, fileName);
    }

    @GetMapping("{id}/graph")
    public byte[] getGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        return processDefinitionLogic.getGraph(authUser.getUser(), id, subprocessId);
    }

    @GetMapping("{id}/graph/elements")
    public List<NodeGraphElementDto> getGraphElements(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        return Mappers.getMapper(NodeGraphElementMapper.class).map(
                processDefinitionLogic.getProcessDefinitionGraphElements(authUser.getUser(), id, subprocessId));
    }

    @GetMapping("{id}/interaction")
    public InteractionDto getStartInteraction(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(InteractionMapper.class).map(processDefinitionLogic.getStartInteraction(authUser.getUser(), id));
    }

    @GetMapping("{id}/interaction/{nodeId}")
    public InteractionDto getTaskNodeInteraction(@PathVariable Long id, @PathVariable String nodeId) {
        return Mappers.getMapper(InteractionMapper.class).map(processDefinitionLogic.getDefinition(id).getInteractionNotNull(nodeId));
    }

    @GetMapping("{id}/swimlanes")
    public List<SwimlaneDefinitionDto> getSwimlanes(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(SwimlaneDefinitionMapper.class).map(processDefinitionLogic.getSwimlanes(authUser.getUser(), id));
    }

    @GetMapping("{id}/userTypes")
    public List<UserTypeDto> getUserTypes(@PathVariable Long id) {
        return Mappers.getMapper(UserTypeMapper.class).map(processDefinitionLogic.getDefinition(id).getUserTypes());
    }

    @GetMapping("{id}/userType")
    public UserTypeDto getUserType(@PathVariable Long id, String name) {
        return Mappers.getMapper(UserTypeMapper.class).map(processDefinitionLogic.getDefinition(id).getUserType(name));
    }

    @GetMapping("{id}/variables")
    public List<VariableDefinitionDto> getVariableDefinitions(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(VariableDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinitionVariables(authUser.getUser(), id));
    }

    @GetMapping("{id}/variable")
    public VariableDefinitionDto getVariableDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String name) {
        return Mappers.getMapper(VariableDefinitionMapper.class).map(
                processDefinitionLogic.getProcessDefinitionVariable(authUser.getUser(), id, name));
    }

    @PostMapping("list")
    public PagedList<WfDefinitionDto> getDefinitions(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.DEFINITION);
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
        WfDefinitionMapper mapper = Mappers.getMapper(WfDefinitionMapper.class);
        int total = processDefinitionLogic.getProcessDefinitionsCount(authUser.getUser(), batchPresentation);
        return new PagedList<WfDefinitionDto>(total, mapper.map(definitions));
    }
    
}
