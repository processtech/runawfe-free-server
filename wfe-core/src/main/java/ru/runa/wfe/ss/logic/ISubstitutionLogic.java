package ru.runa.wfe.ss.logic;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public interface ISubstitutionLogic {

    public List<Substitution> getSubstitutions(User user, Long actorId);

    public Substitution getSubstitution(User user, Long id);

    public Set<Long> getSubstituted(Actor actor);

    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor);

    public SubstitutionCriteria getCriteria(User user, Long id);

    public SubstitutionCriteria getCriteria(User user, String name);

    public List<SubstitutionCriteria> getAllCriterias(User user);

    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria criteria);

}
