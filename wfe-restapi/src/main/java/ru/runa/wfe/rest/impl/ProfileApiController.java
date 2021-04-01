package ru.runa.wfe.rest.impl;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.WfProfileDto;
import ru.runa.wfe.rest.dto.WfUserDtoMapper;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profile")
@Transactional
public class ProfileApiController {
    @Autowired
    private RelationLogic relationLogic;
    @Autowired
    private ExecutorLogic executorLogic;

    @PostMapping
    public WfProfileDto getProfile(@AuthenticationPrincipal AuthUser authUser) {
        User user = authUser.getUser();
        WfUserDtoMapper mapper = Mappers.getMapper(WfUserDtoMapper.class);
        WfProfileDto profileDto = new WfProfileDto();

        profileDto.setUser(mapper.map(user.getActor()));
        profileDto.setRelations(getRelations(user));
        profileDto.setGroups(getGroups(user));

        return profileDto;
    }

    private List<String> getGroups(User user) {
        return executorLogic.getExecutorGroups(user, user.getActor(), BatchPresentationFactory.GROUPS.createDefault(), false)
                .stream()
                .map(Group::getName)
                .collect(Collectors.toList());
    }

    private List<String> getRelations(User user) {
        return relationLogic.getRelations(user, BatchPresentationFactory.RELATIONS.createDefault())
                .stream()
                .map(Relation::getName)
                .collect(Collectors.toList());
    }
}
