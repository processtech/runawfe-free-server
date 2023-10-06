package ru.runa.wfe.rest.impl;

import com.google.common.base.Strings;
import java.util.List;
import java.util.stream.Collectors;
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
import ru.runa.wfe.rest.converter.WfeNodeGraphElementMapper;
import ru.runa.wfe.rest.converter.WfeNodeMapper;
import ru.runa.wfe.rest.converter.WfeProcessDefinitionMapper;
import ru.runa.wfe.rest.converter.WfeSwimlaneDefinitionMapper;
import ru.runa.wfe.rest.converter.WfeTaskNodeInteractionMapper;
import ru.runa.wfe.rest.converter.WfeVariableUserTypeMapper;
import ru.runa.wfe.rest.converter.WfeVariableDefinitionMapper;
import ru.runa.wfe.rest.dto.WfeNode;
import ru.runa.wfe.rest.dto.WfeNodeGraphElement;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeProcessDefinition;
import ru.runa.wfe.rest.dto.WfeSwimlaneDefinition;
import ru.runa.wfe.rest.dto.WfeTaskNodeInteraction;
import ru.runa.wfe.rest.dto.WfeVariableDefinition;
import ru.runa.wfe.rest.dto.WfeVariableUserType;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.file.FileVariable;

@RestController
@RequestMapping("/definition/")
@Transactional
public class DefinitionController {
    
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;

    @PutMapping()
    public WfeProcessDefinition deployProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @RequestBody byte[] par,
            @RequestParam List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        WfDefinition definition = processDefinitionLogic.deployProcessDefinition(authUser.getUser(), par, categories, secondsBeforeArchiving);
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(definition);
    }

    @PatchMapping("{id}/redeploy")
    public WfeProcessDefinition redeployProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody byte[] par,
            @RequestParam List<String> categories, @RequestParam(required = false) Integer secondsBeforeArchiving) {
        WfDefinition definition = processDefinitionLogic.redeployProcessDefinition(authUser.getUser(), id, par, categories, secondsBeforeArchiving);
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(definition);
    }

    @PatchMapping("{id}/update")
    public WfeProcessDefinition updateProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestBody byte[] par) {
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(processDefinitionLogic.updateProcessDefinition(authUser.getUser(), id, par));
    }

    @DeleteMapping()
    public void undeployProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name, @RequestParam Long version) {
        processDefinitionLogic.undeployProcessDefinition(authUser.getUser(), name, version);
    }

    @PostMapping("list")
    public WfePagedList<WfeProcessDefinition> getProcessDefinitions(@AuthenticationPrincipal AuthUser authUser,
            @RequestBody WfePagedListFilter filter) {
        BatchPresentation batchPresentation = filter.toBatchPresentation(ClassPresentationType.DEFINITION);
        List<WfDefinition> definitions = processDefinitionLogic.getProcessDefinitions(authUser.getUser(), batchPresentation, true);
        WfeProcessDefinitionMapper mapper = Mappers.getMapper(WfeProcessDefinitionMapper.class);
        int total = processDefinitionLogic.getProcessDefinitionsCount(authUser.getUser(), batchPresentation);
        return new WfePagedList<>(total, mapper.map(definitions));
    }

    @GetMapping("history")
    public List<WfeProcessDefinition> getProcessDefinitionHistory(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        List<WfDefinition> history = processDefinitionLogic.getProcessDefinitionHistory(authUser.getUser(), name);
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(history);
    }

    @GetMapping("latest")
    public WfeProcessDefinition getLatestProcessDefinition(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(processDefinitionLogic.getLatestProcessDefinition(authUser.getUser(), name));
    }

    @GetMapping("version")
    public WfeProcessDefinition getProcessDefinitionByNameAndVersion(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name,
            @RequestParam Long version) {
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinitionVersion(authUser.getUser(), name, version));
    }

    @GetMapping("{id}")
    public WfeProcessDefinition getProcessDefinitionById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeProcessDefinitionMapper.class).map(processDefinitionLogic.getProcessDefinition(authUser.getUser(), id));
    }

    @GetMapping("{id}/node")
    public WfeNode getProcessDefinitionNode(@PathVariable Long id, @RequestParam String nodeId) {
        Node node = processDefinitionLogic.getDefinition(id).getNode(nodeId);
        return (node != null) ? Mappers.getMapper(WfeNodeMapper.class).map(new WfNode(node)) : null;
    }

    @GetMapping("{id}/nodes")
    public List<WfeNode> getProcessDefinitionNodes(@PathVariable Long id, @RequestParam boolean withEmbeddedSubprocesses) {
        List<Node> nodes = processDefinitionLogic.getDefinition(id).getNodes(withEmbeddedSubprocesses);
        return nodes.stream().map(WfNode::new).map(c -> Mappers.getMapper(WfeNodeMapper.class).map(c)).collect(Collectors.toList());
    }

    @GetMapping("{id}/file")
    public byte[] getProcessDefinitionFile(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam String name) {
        return processDefinitionLogic.getFile(authUser.getUser(), id, name);
    }

    @GetMapping("{id}/graph")
    public byte[] getProcessDefinitionGraph(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        return processDefinitionLogic.getGraph(authUser.getUser(), id, Strings.emptyToNull(subprocessId));
    }

    @GetMapping("{id}/graph/elements")
    public List<WfeNodeGraphElement> getProcessDefinitionGraphElements(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam(required = false) String subprocessId) {
        List<NodeGraphElement> elements = processDefinitionLogic.getProcessDefinitionGraphElements(authUser.getUser(), id, Strings.emptyToNull(subprocessId));
        return Mappers.getMapper(WfeNodeGraphElementMapper.class).map(elements);
    }

    @GetMapping("{id}/interaction/start")
    public WfeTaskNodeInteraction getProcessDefinitionStartInteraction(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeTaskNodeInteractionMapper.class).map(processDefinitionLogic.getStartInteraction(authUser.getUser(), id));
    }

    @GetMapping("{id}/interaction/task")
    public WfeTaskNodeInteraction getProcessDefinitionTaskNodeInteraction(@PathVariable Long id, @RequestParam String nodeId) {
        return Mappers.getMapper(WfeTaskNodeInteractionMapper.class).map(processDefinitionLogic.getDefinition(id).getInteractionNotNull(nodeId));
    }

    @GetMapping("{id}/swimlanes")
    public List<WfeSwimlaneDefinition> getProcessDefinitionSwimlanes(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<SwimlaneDefinition> swimlanes = processDefinitionLogic.getSwimlanes(authUser.getUser(), id);
        return Mappers.getMapper(WfeSwimlaneDefinitionMapper.class).map(swimlanes);
    }

    @GetMapping("{id}/userTypes")
    public List<WfeVariableUserType> getProcessDefinitionUserTypes(@PathVariable Long id) {
        List<UserType> userTypes = processDefinitionLogic.getDefinition(id).getUserTypes();
        return Mappers.getMapper(WfeVariableUserTypeMapper.class).map(userTypes);
    }

    @GetMapping("{id}/userType")
    public WfeVariableUserType getProcessDefinitionUserType(@PathVariable Long id, @RequestParam String name) {
        return Mappers.getMapper(WfeVariableUserTypeMapper.class).map(processDefinitionLogic.getDefinition(id).getUserType(name));
    }

    @GetMapping("{id}/variables")
    public List<WfeVariableDefinition> getProcessDefinitionVariableDefinitions(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        List<VariableDefinition> variableDefinitions = processDefinitionLogic.getProcessDefinitionVariables(authUser.getUser(), id);
        return Mappers.getMapper(WfeVariableDefinitionMapper.class).map(variableDefinitions);
    }

    @GetMapping("{id}/variable")
    public WfeVariableDefinition getProcessDefinitionVariableDefinition(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam String name) {
        VariableDefinition variableDefinition = processDefinitionLogic.getProcessDefinitionVariable(authUser.getUser(), id, name);
        return Mappers.getMapper(WfeVariableDefinitionMapper.class).map(variableDefinition);
    }

    @GetMapping("{id}/variable/value/file")
    public byte[] getProcessDefinitionVariableFileDefaultValue(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestParam String name) {
        VariableDefinition variableDefinition = processDefinitionLogic.getProcessDefinitionVariable(authUser.getUser(), id, name);
        return ((FileVariable) variableDefinition.getDefaultValue()).getData();
    }

}
