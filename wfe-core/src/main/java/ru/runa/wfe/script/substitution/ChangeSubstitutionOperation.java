package ru.runa.wfe.script.substitution;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ExecutorsSetContainerOperation;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@XmlType(name = ChangeSubstitutionOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class ChangeSubstitutionOperation extends ExecutorsSetContainerOperation {

    private final static String ACTOR_CODE_VARIABLE = "%self_code%";
    private final static String ACTOR_ID_VARIABLE = "%self_id%";
    private final static String ACTOR_NAME_VARIABLE = "%self_name%";

    public static final String SCRIPT_NAME = "changeSubstitutions";

    @XmlElement(name = "delete", namespace = AdminScriptConstants.NAMESPACE)
    public List<XmlSubstitution> substitutionsToDelete = Lists.newArrayList();

    @XmlElement(name = "add", namespace = AdminScriptConstants.NAMESPACE)
    public List<XmlSubstitution> substitutionsToAdd = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
        super.validate(true);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        Set<Actor> actors = Sets.newHashSet();
        for (Executor ex : getExecutors(context)) {
            actors.add((Actor) ex);
        }
        List<Long> idsToDelete = getSubstitutionIdsToDelete(context, substitutionsToDelete, actors);
        context.getSubstitutionLogic().delete(context.getUser(), idsToDelete);
        addSubstitutions(context, substitutionsToAdd, actors);
    }

    private List<Long> getSubstitutionIdsToDelete(ScriptExecutionContext context, List<XmlSubstitution> substitutions, Set<Actor> actors) {
        if (substitutions.size() == 0) {
            return new ArrayList<Long>();
        }
        List<Long> result = Lists.newArrayList();
        List<SubstitutionCriteria> criterias = Lists.newArrayList();
        List<String> orgFunctions = Lists.newArrayList();
        for (XmlSubstitution substitution : substitutions) {
            orgFunctions.add(substitution.orgFunction);
            if (substitution.criteriaId != null) {
                criterias.add(context.getSubstitutionLogic().getCriteria(context.getUser(), substitution.criteriaId));
            } else {
                criterias.add(null);
            }
        }
        for (Actor actor : actors) {
            for (Substitution substitution : context.getSubstitutionLogic().getSubstitutions(context.getUser(), actor.getId())) {
                for (int i = 0; i < substitutions.size(); ++i) {
                    if (isCriteriaMatch(substitution.getCriteria(), criterias.get(i))
                            && isStringMatch(substitution.getOrgFunction(), tuneOrgFunc(orgFunctions.get(i), actor))) {
                        result.add(substitution.getId());
                        break;
                    }
                }
            }
        }
        return result;
    }

    private void addSubstitutions(ScriptExecutionContext context, List<XmlSubstitution> xmlSubstitutions, Set<Actor> actors) {
        for (XmlSubstitution xmlSubstitution : xmlSubstitutions) {
            SubstitutionCriteria criteria = null;
            if (xmlSubstitution.criteriaId != null) {
                criteria = context.getSubstitutionLogic().getCriteria(context.getUser(), xmlSubstitution.criteriaId);
            }
            for (Actor actor : actors) {
                Substitution substitution;
                if (xmlSubstitution.orgFunction == null) {
                    substitution = new TerminatorSubstitution();
                } else {
                    substitution = new Substitution();
                    substitution.setOrgFunction(xmlSubstitution.orgFunction);
                }
                substitution.setActorId(actor.getId());
                substitution.setCriteria(criteria);
                substitution.setEnabled(xmlSubstitution.isEnabled);
                if (xmlSubstitution.isFirst) {
                    substitution.setPosition(0);
                }
                context.getSubstitutionLogic().create(context.getUser(), substitution);
            }
        }
    }

    private boolean isCriteriaMatch(SubstitutionCriteria substitutionCriteria, SubstitutionCriteria matcher) {
        if (substitutionCriteria == null && matcher == null) {
            return true;
        } else if (substitutionCriteria == null || matcher == null) {
            return false;
        } else {
            return (isStringMatch(substitutionCriteria.getName(), matcher.getName()) && isStringMatch(substitutionCriteria.getConfiguration(),
                matcher.getConfiguration()));
        }
    }

    private String tuneOrgFunc(String orgFunction, Actor actor) {
        if (orgFunction == null) {
            return null;
        }
        String result = orgFunction.replaceAll(ACTOR_CODE_VARIABLE, Long.toString(actor.getCode()));
        result = result.replaceAll(ACTOR_ID_VARIABLE, Long.toString(actor.getId()));
        result = result.replaceAll(ACTOR_NAME_VARIABLE, actor.getName());
        return result;
    }

    private boolean isStringMatch(String criteria, String matcher) {
        if (matcher == null) {
            return true;
        }
        if (criteria == null) {
            return false;
        }
        return criteria.equals(matcher);
    }
}
