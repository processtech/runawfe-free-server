package ru.runa.wfe.rest.impl;

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
import ru.runa.wfe.rest.dto.WfGroupDto;
import ru.runa.wfe.rest.dto.WfGroupDtoMapper;
import ru.runa.wfe.rest.dto.WfUserDto;
import ru.runa.wfe.rest.dto.WfUserDtoMapper;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.logic.ExecutorLogic;
import java.util.List;

@RestController
@RequestMapping("/executor/")
@Transactional
public class ExecutorApiController {

    @Autowired
    private ExecutorLogic executorLogic;

    @PutMapping
    public void create(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExecutorDto dto) {
        executorLogic.create(authUser.getUser(), Mappers.getMapper(ExecutorMapper.class).map(dto));
    }

    @PatchMapping
    public ExecutorDto update(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExecutorDto dto) {
        Executor executor = executorLogic.getExecutor(authUser.getUser(), dto.getId());
        ExecutorMapper mapper = Mappers.getMapper(ExecutorMapper.class);
        return mapper.map(executorLogic.update(authUser.getUser(), mapper.map(executor, dto)));
    }

    @DeleteMapping
    public void remove(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<Long> ids) {
        executorLogic.remove(authUser.getUser(), ids);
    }

    @GetMapping("{code}/byCode")
    public WfUserDto getByCode(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long code) {
        return Mappers.getMapper(WfUserDtoMapper.class).map(executorLogic.getActorByCode(authUser.getUser(), code));
    }

    @GetMapping("{id}")
    public ExecutorDto get(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.getExecutor(authUser.getUser(), id));
    }

    @GetMapping()
    public ExecutorDto get(@AuthenticationPrincipal AuthUser authUser, String name) {
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.getExecutor(authUser.getUser(), name));
    }

    @GetMapping("isExist")
    public boolean isExist(@AuthenticationPrincipal AuthUser authUser, String name) {
        return executorLogic.isExecutorExist(authUser.getUser(), name);
    }

    @GetMapping("{id}/isAdministrator")
    public boolean isAdministrator(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executorLogic.isAdministrator(executorLogic.getActor(authUser.getUser(), id));
    }

    @GetMapping("{id}/isInGroup")
    public boolean isInGroup(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, Long groupId) {
        return executorLogic.isExecutorInGroup(authUser.getUser(),
                executorLogic.getExecutor(authUser.getUser(), id), executorLogic.getGroup(authUser.getUser(), groupId));
    }

    @PostMapping("list")
    public List<ExecutorDto> getExecutors(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.EXECUTOR);
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.getExecutors(authUser.getUser(), batchPresentation));
    }

    @PostMapping("{id}/groups")
    public List<WfGroupDto> getExecutorGroups(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody BatchPresentationRequest request, @RequestParam(required = false) boolean isExclude) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.GROUP);
        return Mappers.getMapper(WfGroupDtoMapper.class).map(executorLogic.getExecutorGroups(
                authUser.getUser(), executorLogic.getExecutor(authUser.getUser(), id), batchPresentation, isExclude));
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
    public List<ExecutorDto> getGroupChildren(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId,
            @RequestBody BatchPresentationRequest request, @RequestParam(required = false) boolean isExclude) {
        BatchPresentation batchPresentation = request.toBatchPresentation(ClassPresentationType.EXECUTOR);
        return Mappers.getMapper(ExecutorMapper.class).map(executorLogic.getGroupChildren(
                authUser.getUser(), executorLogic.getGroup(authUser.getUser(), groupId), batchPresentation, isExclude));
    }

    @GetMapping("{groupId}/actors")
    public List<WfUserDto> getGroupActors(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId) {
        return Mappers.getMapper(WfUserDtoMapper.class).map(
                executorLogic.getGroupActors(authUser.getUser(), executorLogic.getGroup(authUser.getUser(), groupId)));
    }
}
