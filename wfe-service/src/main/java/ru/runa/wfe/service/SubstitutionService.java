package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Service for operations with {@link Substitution},
 * {@link SubstitutionCriteria}.
 * 
 * @since 3.0
 */
public interface SubstitutionService {

    /**
     * Creates new substitution rule for user.
     */
    Substitution createSubstitution(User user, Substitution substitution);

    /**
     * Gets all substitutions for user specified by id.
     */
    List<Substitution> getSubstitutions(User user, Long ownerId);

    /**
     * Gets substitution rule by id.
     */
    Substitution getSubstitution(User user, Long substitutionId);

    /**
     * Updates substitution rule.
     */
    void updateSubstitution(User user, Substitution substitution);

    /**
     * Deletes substitution rules by ids.
     */
    void deleteSubstitutions(User user, List<Long> substitutionIds) throws SubstitutionDoesNotExistException;

    /**
     * Creates new criteria for substitution rules.
     */
    <T extends SubstitutionCriteria> void createCriteria(User user, T substitutionCriteria);

    /**
     * Gets criteria by id.
     */
    SubstitutionCriteria getCriteria(User user, Long criteriaId);

    /**
     * Gets criteria by name.
     */
    SubstitutionCriteria getCriteriaByName(User user, String name);

    /**
     * Gets all criterias.
     */
    List<SubstitutionCriteria> getAllCriterias(User user);

    /**
     * Updates criteria.
     */
    void updateCriteria(User user, SubstitutionCriteria criteria);

    /**
     * Deletes criterias.
     */
    void deleteCriterias(User user, List<SubstitutionCriteria> criterias);

    /**
     * Deletes criteria.
     */
    void deleteCriteria(User user, SubstitutionCriteria criteria);

    /**
     * Gets all substitution rules which uses criteria.
     */
    List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria criteria);
}
