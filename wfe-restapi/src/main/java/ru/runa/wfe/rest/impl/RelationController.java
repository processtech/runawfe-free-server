package ru.runa.wfe.rest.impl;

import java.util.Date;
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
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.converter.WfeRelationMapper;
import ru.runa.wfe.rest.converter.WfeRelationPairMapper;
import ru.runa.wfe.rest.dto.WfePagedList;
import ru.runa.wfe.rest.dto.WfePagedListFilter;
import ru.runa.wfe.rest.dto.WfeRelation;
import ru.runa.wfe.rest.dto.WfeRelationPair;

@RestController
@RequestMapping("/relation/")
@Transactional
public class RelationController {

    @Autowired
    private RelationLogic relationLogic;

    @PutMapping
    public WfeRelation createRelation(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeRelation relation) {
        WfeRelationMapper mapper = Mappers.getMapper(WfeRelationMapper.class);
        relation.setCreateDate(new Date());
        return mapper.map(relationLogic.createRelation(authUser.getUser(), mapper.map(relation)));
    }

    @PatchMapping
    public WfeRelation updateRelation(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeRelation relation) {
        WfeRelationMapper mapper = Mappers.getMapper(WfeRelationMapper.class);
        return mapper.map(relationLogic.updateRelation(authUser.getUser(), mapper.map(relation)));
    }

    @DeleteMapping("{id}")
    public void removeRelation(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        relationLogic.removeRelation(authUser.getUser(), id);
    }

    @GetMapping("{id}")
    public WfeRelation getRelationById(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeRelationMapper.class).map(relationLogic.getRelation(authUser.getUser(), id));
    }

    @GetMapping("byName")
    public WfeRelation getRelationByName(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        return Mappers.getMapper(WfeRelationMapper.class).map(relationLogic.getRelation(authUser.getUser(), name));
    }

    @PostMapping("list")
    public WfePagedList<WfeRelation> getRelations(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfePagedListFilter filter) {
        List<Relation> relations = relationLogic.getRelations(authUser.getUser(), filter.toBatchPresentation(ClassPresentationType.RELATION));
        return new WfePagedList<>(relations.size(), Mappers.getMapper(WfeRelationMapper.class).map(relations));
    }

    @PutMapping("pair")
    public WfeRelationPair createRelationPair(@AuthenticationPrincipal AuthUser authUser, @RequestParam Long id, @RequestParam Long leftId,
            @RequestParam Long rightId) {
        return Mappers.getMapper(WfeRelationPairMapper.class).map(relationLogic.addRelationPair(authUser.getUser(), id, leftId, rightId));
    }

    @DeleteMapping("pair")
    public void removeRelationPair(@AuthenticationPrincipal AuthUser authUser, @RequestParam Long pairId) {
        relationLogic.removeRelationPair(authUser.getUser(), pairId);
    }

    @PostMapping("pair/list")
    public WfePagedList<WfeRelationPair> getRelationPairs(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name,
            @RequestBody WfePagedListFilter request) {
        List<RelationPair> relations = relationLogic.getRelations(authUser.getUser(), name,
                request.toBatchPresentation(ClassPresentationType.RELATIONPAIR));
        return new WfePagedList<>(relations.size(), Mappers.getMapper(WfeRelationPairMapper.class).map(relations));
    }
}
