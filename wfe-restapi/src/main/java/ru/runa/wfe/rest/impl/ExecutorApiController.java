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
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.ExecutorDto;
import ru.runa.wfe.rest.dto.ExecutorMapper;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.WfGroupDto;
import ru.runa.wfe.rest.dto.WfGroupDtoMapper;
import ru.runa.wfe.rest.dto.WfUserDto;
import ru.runa.wfe.rest.dto.WfUserDtoMapper;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.logic.ExecutorLogic;

@RestController
@RequestMapping("/executor/")
@Transactional
public class ExecutorApiController {

    @Autowired
    private ExecutorLogic executorLogic;

    @PutMapping("actor")
    public WfUserDto createActor(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfUserDto dto) {
        WfUserDtoMapper mapper = Mappers.getMapper(WfUserDtoMapper.class);
        return mapper.map(executorLogic.create(authUser.getUser(), mapper.map(dto)));
    }

    @PutMapping("group")
    public WfGroupDto createGroup(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfGroupDto dto) {
        WfGroupDtoMapper mapper = Mappers.getMapper(WfGroupDtoMapper.class);
        return mapper.map(executorLogic.create(authUser.getUser(), mapper.map(dto)));
    }

    @PatchMapping
    public ExecutorDto updateExecutor(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExecutorDto dto) {
        Executor executor = executorLogic.getExecutor(authUser.getUser(), dto.getId());
        executor.setName(dto.getName());
        executor.setFullName(dto.getFullName());
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.update(authUser.getUser(), executor));
    }

    @DeleteMapping
    public void removeExecutors(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<Long> ids) {
        executorLogic.remove(authUser.getUser(), ids);
    }

    @GetMapping("{id}")
    public ExecutorDto getExecutor(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.getExecutor(authUser.getUser(), id));
    }

    @GetMapping("{code}/byCode")
    public WfUserDto getActorByCode(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long code) {
        return Mappers.getMapper(WfUserDtoMapper.class).map(executorLogic.getActorByCode(authUser.getUser(), code));
    }

    @GetMapping()
    public ExecutorDto getByName(@AuthenticationPrincipal AuthUser authUser, String name) {
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.getExecutor(authUser.getUser(), name));
    }

    @GetMapping("isExist")
    public boolean isExecutorExist(@AuthenticationPrincipal AuthUser authUser, String name) {
        return executorLogic.isExecutorExist(authUser.getUser(), name);
    }

    @GetMapping("{id}/isAdministrator")
    public boolean isAdministrator(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executorLogic.isAdministrator(executorLogic.getActor(authUser.getUser(), id));
    }

    @GetMapping("{id}/isInGroup")
    public boolean isExecutorInGroup(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, Long groupId) {
        Executor executor = executorLogic.getExecutor(authUser.getUser(), id);
        return executorLogic.isExecutorInGroup(authUser.getUser(), executor, executorLogic.getGroup(authUser.getUser(), groupId));
    }

    @PostMapping("list")
    public PagedList<ExecutorDto> getExecutors(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.EXECUTOR);
        List<? extends Executor> executors = executorLogic.getExecutors(authUser.getUser(), batchPresentation);
        return new PagedList<>(executors.size(), Mappers.getMapper(ExecutorMapper.class).map(executors));
    }

    @PostMapping("{id}/groups")
    public PagedList<WfGroupDto> getExecutorGroups(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody BatchPresentationRequest request, @RequestParam(required = false) boolean isExclude) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.GROUP);
        Executor executor = executorLogic.getExecutor(authUser.getUser(), id);
        List<Group> groups = executorLogic.getExecutorGroups(authUser.getUser(), executor, batchPresentation, isExclude);
        return new PagedList<>(groups.size(), Mappers.getMapper(WfGroupDtoMapper.class).map(groups));
    }

    @PutMapping("{id}/groups")
    public void addExecutorToGroups(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam List<Long> groupIds) {
        executorLogic.addExecutorToGroups(authUser.getUser(), id, groupIds);
    }

    @DeleteMapping("{id}/groups")
    public void removeExecutorFromGroups(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam List<Long> groupIds) {
        executorLogic.removeExecutorFromGroups(authUser.getUser(), id, groupIds);
    }

    @PutMapping("{groupId}/executors")
    public void addExecutorsToGroup(@AuthenticationPrincipal AuthUser authUser, @RequestParam List<Long> ids, @PathVariable Long groupId) {
        executorLogic.addExecutorsToGroup(authUser.getUser(), ids, groupId);
    }

    @DeleteMapping("{groupId}/executors")
    public void removeExecutorsFromGroup(@AuthenticationPrincipal AuthUser authUser, @RequestParam List<Long> ids, @PathVariable Long groupId) {
        executorLogic.removeExecutorsFromGroup(authUser.getUser(), ids, groupId);
    }

    @PostMapping("{groupId}/children")
    public PagedList<ExecutorDto> getGroupChildren(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId,
            @RequestBody BatchPresentationRequest request, @RequestParam(required = false) boolean isExclude) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.EXECUTOR);
        Group group = executorLogic.getGroup(authUser.getUser(), groupId);
        List<Executor> executors = executorLogic.getGroupChildren(authUser.getUser(), group, batchPresentation, isExclude);
        return new PagedList<>(executors.size(), Mappers.getMapper(ExecutorMapper.class).map(executors));
    }

    @GetMapping("{groupId}/actors")
    public PagedList<WfUserDto> getGroupActors(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId) {
        Group group = executorLogic.getGroup(authUser.getUser(), groupId);
        List<WfUserDto> users = Mappers.getMapper(WfUserDtoMapper.class).map(executorLogic.getGroupActors(authUser.getUser(), group));
        return new PagedList<>(users.size(), users);
    }
}
