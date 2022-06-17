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
import ru.runa.wfe.rest.converter.WfeExecutorMapper;
import ru.runa.wfe.rest.converter.WfeGroupMapper;
import ru.runa.wfe.rest.converter.WfeUserMapper;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.rest.dto.WfeGroup;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeUser;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.logic.ExecutorLogic;

@RestController
@RequestMapping("/executor/")
@Transactional
public class ExecutorController {

    @Autowired
    private ExecutorLogic executorLogic;

    @PutMapping("user")
    public WfeUser createUser(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeUser dto) {
        WfeUserMapper mapper = Mappers.getMapper(WfeUserMapper.class);
        return mapper.map(executorLogic.create(authUser.getUser(), mapper.map(dto)));
    }

    @PutMapping("group")
    public WfeGroup createGroup(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeGroup dto) {
        WfeGroupMapper mapper = Mappers.getMapper(WfeGroupMapper.class);
        return mapper.map(executorLogic.create(authUser.getUser(), mapper.map(dto)));
    }

    @GetMapping("user")
    public WfeUser getUser(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        return Mappers.getMapper(WfeUserMapper.class).map(executorLogic.getActor(authUser.getUser(), name));
    }

    @GetMapping("group")
    public WfeGroup getGroup(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        return Mappers.getMapper(WfeGroupMapper.class).map(executorLogic.getGroup(authUser.getUser(), name));
    }

    @PatchMapping("user")
    public void updateUser(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeUser dto) {
        Actor actor = executorLogic.getActor(authUser.getUser(), dto.getId());
        Mappers.getMapper(WfeUserMapper.class).fill(actor, dto);
        executorLogic.update(authUser.getUser(), actor);
    }

    @PatchMapping("group")
    public void updateGroup(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeGroup dto) {
        Group group = executorLogic.getGroup(authUser.getUser(), dto.getId());
        Mappers.getMapper(WfeGroupMapper.class).fill(group, dto);
        executorLogic.update(authUser.getUser(), group);
    }

    @PatchMapping("user/status")
    public void updateUserStatus(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name, @RequestParam boolean active) {
        Actor actor = executorLogic.getActor(authUser.getUser(), name);
        executorLogic.setStatus(authUser.getUser(), actor, active, true);
    }

    @DeleteMapping
    public void removeExecutors(@AuthenticationPrincipal AuthUser authUser, @RequestBody List<Long> ids) {
        executorLogic.remove(authUser.getUser(), ids);
    }

    @GetMapping("{code}/byCode")
    public WfeUser getUserByCode(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long code) {
        return Mappers.getMapper(WfeUserMapper.class).map(executorLogic.getActorByCode(authUser.getUser(), code));
    }

    @GetMapping()
    public WfeExecutor getByName(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        return Mappers.getMapper(WfeExecutorMapper.class).map(executorLogic.getExecutor(authUser.getUser(), name));
    }

    @GetMapping("isExist")
    public boolean isExecutorExist(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        return executorLogic.isExecutorExist(authUser.getUser(), name);
    }

    @GetMapping("{id}/isAdministrator")
    public boolean isAdministrator(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return executorLogic.isAdministrator(executorLogic.getActor(authUser.getUser(), id));
    }

    @GetMapping("{id}/isInGroup")
    public boolean isExecutorInGroup(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id, @RequestParam Long groupId) {
        Executor executor = executorLogic.getExecutor(authUser.getUser(), id);
        return executorLogic.isExecutorInGroup(authUser.getUser(), executor, executorLogic.getGroup(authUser.getUser(), groupId));
    }

    @PostMapping("list")
    public WfePagedList<WfeExecutor> getExecutors(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        BatchPresentation batchPresentation = filter.toBatchPresentation(ClassPresentationType.EXECUTOR);
        List<? extends Executor> executors = executorLogic.getExecutors(authUser.getUser(), batchPresentation);
        return new WfePagedList<>(executors.size(), Mappers.getMapper(WfeExecutorMapper.class).map(executors));
    }

    @PostMapping("{id}/groups")
    public WfePagedList<WfeGroup> getExecutorGroups(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id,
            @RequestBody WfePagedListFilter filter, @RequestParam(required = false) boolean isExclude) {
        BatchPresentation batchPresentation = filter.toBatchPresentation(ClassPresentationType.GROUP);
        Executor executor = executorLogic.getExecutor(authUser.getUser(), id);
        List<Group> groups = executorLogic.getExecutorGroups(authUser.getUser(), executor, batchPresentation, isExclude);
        return new WfePagedList<>(groups.size(), Mappers.getMapper(WfeGroupMapper.class).map(groups));
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
    public WfePagedList<WfeExecutor> getGroupChildren(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long groupId,
            @RequestBody WfePagedListFilter filter, @RequestParam(required = false) boolean isExclude) {
        BatchPresentation batchPresentation = filter.toBatchPresentation(ClassPresentationType.EXECUTOR);
        Group group = executorLogic.getGroup(authUser.getUser(), groupId);
        List<Executor> executors = executorLogic.getGroupChildren(authUser.getUser(), group, batchPresentation, isExclude);
        return new WfePagedList<>(executors.size(), Mappers.getMapper(WfeExecutorMapper.class).map(executors));
    }

    @GetMapping("group/users")
    public List<WfeUser> getGroupUsers(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        Group group = executorLogic.getGroup(authUser.getUser(), name);
        return Mappers.getMapper(WfeUserMapper.class).map(executorLogic.getGroupActors(authUser.getUser(), group));
    }
}
