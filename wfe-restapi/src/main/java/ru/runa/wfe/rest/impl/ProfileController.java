package ru.runa.wfe.rest.impl;

import java.util.List;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.WfGroupDto;
import ru.runa.wfe.rest.dto.WfGroupDtoMapper;
import ru.runa.wfe.rest.dto.WfProfileDto;
import ru.runa.wfe.rest.dto.WfRelationDto;
import ru.runa.wfe.rest.dto.WfRelationDtoMapper;
import ru.runa.wfe.rest.dto.WfUserDtoMapper;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

@RestController
@RequestMapping("/profile")
@Transactional
public class ProfileController {

    @Autowired
    private RelationLogic relationLogic;
    @Autowired
    private ExecutorLogic executorLogic;

    @PostMapping
    public WfProfileDto getProfile(@AuthenticationPrincipal AuthUser authUser) {
        User user = authUser.getUser();
        WfProfileDto profileDto = new WfProfileDto();
        WfUserDtoMapper userDtoMapper = Mappers.getMapper(WfUserDtoMapper.class);
        profileDto.setUser(userDtoMapper.map(user.getActor()));
        profileDto.setRelations(getRelations(user));
        profileDto.setGroups(getGroupsDto(user));
        return profileDto;
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@AuthenticationPrincipal AuthUser authUser, @RequestParam String password) {
        User user = authUser.getUser();
        executorLogic.setPassword(user, user.getActor(), password);
    }

    private List<WfGroupDto> getGroupsDto(User user) {
        WfGroupDtoMapper groupDtoMapper = Mappers.getMapper(WfGroupDtoMapper.class);
        List<Group> groups = executorLogic.getExecutorGroups(
user, user.getActor(), BatchPresentationFactory.GROUPS.createNonPaged(), false);
        return groupDtoMapper.map(groups);
    }

    private List<WfRelationDto> getRelations(User user) {
        WfRelationDtoMapper relationDtoMapper = Mappers.getMapper(WfRelationDtoMapper.class);
        List<Relation> relations = relationLogic.getRelations(user, BatchPresentationFactory.RELATIONS.createNonPaged());
        return relationDtoMapper.map(relations);
    }
}
