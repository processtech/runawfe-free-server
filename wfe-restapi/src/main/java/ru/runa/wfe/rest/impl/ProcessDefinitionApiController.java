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
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.SwimlaneDefinitionDto;
import ru.runa.wfe.rest.dto.SwimlaneDefinitionMapper;
import ru.runa.wfe.rest.dto.WfDefinitionDto;
import ru.runa.wfe.rest.dto.WfDefinitionMapper;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

@RestController
@RequestMapping("/definition/")
@Transactional
public class ProcessDefinitionApiController {
    
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;

    @PutMapping()
    public WfDefinitionDto deployDefinition(@AuthenticationPrincipal AuthUser authUser, byte[] par,
            List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        WfDefinition definition = processDefinitionLogic.deployProcessDefinition(authUser.getUser(), par, categories, secondsBeforeArchiving);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @PatchMapping("{id}/redeploy")
    public WfDefinitionDto redeployDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, byte[] par,
            List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        WfDefinition definition = processDefinitionLogic.redeployProcessDefinition(authUser.getUser(), id, par, categories, secondsBeforeArchiving);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @PatchMapping("{id}/update")
    public WfDefinitionDto updateDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, byte[] par) {
        WfDefinition definition = processDefinitionLogic.updateProcessDefinition(authUser.getUser(), id, par);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @DeleteMapping()
    public void undeployDefinition(@AuthenticationPrincipal AuthUser authUser, String name, Long version) {
        processDefinitionLogic.undeployProcessDefinition(authUser.getUser(), name, version);
    }

    @GetMapping("{name}/latest")
    public WfDefinitionDto getLatestDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name) {
        WfDefinition definition = processDefinitionLogic.getLatestProcessDefinition(authUser.getUser(), name);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @GetMapping("{name}/version")
    public WfDefinitionDto getDefinitionVersion(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name, Long version) {
        WfDefinition definition = processDefinitionLogic.getProcessDefinitionVersion(authUser.getUser(), name, version);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @GetMapping("{name}/history")
    public List<WfDefinitionDto> getDefinitionHistory(@AuthenticationPrincipal AuthUser authUser, @PathVariable String name) {
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitionHistory(authUser.getUser(), name);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definitions);
    }

    @GetMapping("{id}")
    public WfDefinitionDto getDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        WfDefinition definition = processDefinitionLogic.getProcessDefinition(authUser.getUser(), id);
        return Mappers.getMapper(WfDefinitionMapper.class).map(definition);
    }

    @GetMapping("{id}/node")
    public WfNode getNode(@PathVariable Long id, String nodeId) {
        Node node = processDefinitionLogic.getDefinition(id).getNode(nodeId);
        return (node != null) ? new WfNode(node) : null;
    }

    @GetMapping("{id}/file")
    public byte[] getDefinitionFile(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, String fileName) {
        return processDefinitionLogic.getFile(authUser.getUser(), id, fileName);
    }

    @GetMapping("{id}/graph")
    public byte[] getDefinitionGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        return processDefinitionLogic.getGraph(authUser.getUser(), id, subprocessId);
    }

    @GetMapping("{id}/graph/elements")
    public List<NodeGraphElement> getDefinitionGraphElements(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        return processDefinitionLogic.getProcessDefinitionGraphElements(authUser.getUser(), id, subprocessId);
    }

    @GetMapping("{id}/interaction")
    public Interaction getStartInteraction(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return processDefinitionLogic.getStartInteraction(authUser.getUser(), id);
    }

    @GetMapping("{id}/interaction/{nodeId}")
    public Interaction getTaskNodeInteraction(@PathVariable Long id, @PathVariable String nodeId) {
        return processDefinitionLogic.getDefinition(id).getInteractionNotNull(nodeId);
    }

    @GetMapping("{id}/swimlane")
    public List<SwimlaneDefinitionDto> getSwimlaneDefinitions(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<SwimlaneDefinition> definitions = processDefinitionLogic.getSwimlanes(authUser.getUser(), id);
        return Mappers.getMapper(SwimlaneDefinitionMapper.class).map(definitions);
    }

    @GetMapping("{id}/userType")
    public List<UserType> getUserTypes(@PathVariable Long id) {
        return processDefinitionLogic.getDefinition(id).getUserTypes();
    }

    @GetMapping("{id}/userType/{name}")
    public UserType getUserType(@PathVariable Long id, @PathVariable String name) {
        return processDefinitionLogic.getDefinition(id).getUserType(name);
    }

    @GetMapping("{id}/variable")
    public List<VariableDefinition> getVariableDefinitions(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return processDefinitionLogic.getProcessDefinitionVariables(authUser.getUser(), id);
    }

    @GetMapping("{id}/variable/{name}")
    public VariableDefinition getVariableDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @PathVariable String name) {
        return processDefinitionLogic.getProcessDefinitionVariable(authUser.getUser(), id, name);
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
