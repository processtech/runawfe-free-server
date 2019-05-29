package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.User;

/**
 * Created on 30.01.2006
 * 
 */
public class SubstitutionServiceDelegate extends Ejb3Delegate implements SubstitutionService {

    public SubstitutionServiceDelegate() {
        super(SubstitutionService.class);
    }

    private SubstitutionService getSubstitutionService() {
        return getService();
    }

    @Override
    public Substitution createSubstitution(User user, Substitution substitution) {
        try {
            return getSubstitutionService().createSubstitution(user, substitution);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Substitution> getSubstitutions(User user, Long actorId) {
        try {
            return getSubstitutionService().getSubstitutions(user, actorId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deleteSubstitutions(User user, List<Long> substitutionIds) {
        try {
            getSubstitutionService().deleteSubstitutions(user, substitutionIds);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Substitution getSubstitution(User user, Long substitutionId) {
        try {
            return getSubstitutionService().getSubstitution(user, substitutionId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void updateSubstitution(User user, Substitution substitution) {
        try {
            getSubstitutionService().updateSubstitution(user, substitution);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public <T extends SubstitutionCriteria> void createCriteria(User user, T substitutionCriteria) {
        try {
            getSubstitutionService().createCriteria(user, substitutionCriteria);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public SubstitutionCriteria getCriteria(User user, Long criteriaId) {
        try {
            return getSubstitutionService().getCriteria(user, criteriaId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public SubstitutionCriteria getCriteriaByName(User user, String name) {
        try {
            return getSubstitutionService().getCriteriaByName(user, name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<SubstitutionCriteria> getAllCriterias(User user) {
        try {
            return getSubstitutionService().getAllCriterias(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void updateCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        try {
            getSubstitutionService().updateCriteria(user, substitutionCriteria);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias) {
        try {
            getSubstitutionService().deleteCriterias(user, criterias);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void deleteCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        try {
            getSubstitutionService().deleteCriteria(user, substitutionCriteria);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria substitutionCriteria) {
        try {
            return getSubstitutionService().getSubstitutionsByCriteria(user, substitutionCriteria);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
