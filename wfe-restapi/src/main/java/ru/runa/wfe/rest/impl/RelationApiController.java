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
import org.springframework.web.bind.annotation.RestController;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.dto.BatchPresentationRequest;
import ru.runa.wfe.rest.dto.PagedList;
import ru.runa.wfe.rest.dto.RelationPairDto;
import ru.runa.wfe.rest.dto.RelationPairMapper;
import ru.runa.wfe.rest.dto.WfRelationDto;
import ru.runa.wfe.rest.dto.WfRelationDtoMapper;
import java.util.List;

@RestController
@RequestMapping("/relation/")
@Transactional
public class RelationApiController {

    @Autowired
    private RelationLogic relationLogic;

    @PutMapping
    public WfRelationDto create(@AuthenticationPrincipal AuthUser authUser, @RequestBody Relation relation) {
        return Mappers.getMapper(WfRelationDtoMapper.class).map(relationLogic.createRelation(authUser.getUser(), relation));
    }

    @PatchMapping
    public WfRelationDto update(@AuthenticationPrincipal AuthUser authUser, @RequestBody Relation relation) {
        return Mappers.getMapper(WfRelationDtoMapper.class).map(relationLogic.updateRelation(authUser.getUser(), relation));
    }

    @DeleteMapping("{id}")
    public void remove(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        relationLogic.removeRelation(authUser.getUser(), id);
    }

    @GetMapping("{id}")
    public WfRelationDto get(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfRelationDtoMapper.class).map(relationLogic.getRelation(authUser.getUser(), id));
    }

    @GetMapping("byName")
    public WfRelationDto get(@AuthenticationPrincipal AuthUser authUser, String name) {
        return Mappers.getMapper(WfRelationDtoMapper.class).map(relationLogic.getRelation(authUser.getUser(), name));
    }

    @PostMapping("list")
    public PagedList<WfRelationDto> get(@AuthenticationPrincipal AuthUser authUser, @RequestBody BatchPresentationRequest request) {
        List<Relation> relations = relationLogic.getRelations(authUser.getUser(), request.toBatchPresentation(ClassPresentationType.RELATION));
        return new PagedList<>(relations.size(), Mappers.getMapper(WfRelationDtoMapper.class).map(relations));
    }

    @PutMapping("pair")
    public RelationPairDto createPair(@AuthenticationPrincipal AuthUser authUser, Long id, Long leftId, Long rightId) {
        return Mappers.getMapper(RelationPairMapper.class).map(relationLogic.addRelationPair(authUser.getUser(), id, leftId, rightId));
    }

    @DeleteMapping("pair")
    public void removePair(@AuthenticationPrincipal AuthUser authUser, Long pairId) {
        relationLogic.removeRelationPair(authUser.getUser(), pairId);
    }

    @PostMapping("pair/list")
    public PagedList<RelationPairDto> getPairs(@AuthenticationPrincipal AuthUser authUser, String name,
            @RequestBody BatchPresentationRequest request) {
        List<RelationPair> relations = relationLogic.getRelations(authUser.getUser(), name,
                request.toBatchPresentation(ClassPresentationType.RELATIONPAIR));
        return new PagedList<>(relations.size(), Mappers.getMapper(RelationPairMapper.class).map(relations));
    }
}
