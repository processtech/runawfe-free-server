package ru.runa.wf.web.ftl.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Sets;

public abstract class ChooseByRelationBase extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        String relationName = getRichComboParameterAs(String.class, 1);
        Executor relationParam = getRichComboParameterAs(Executor.class, 2);
        if (relationParam == null) {
            // TODO right way?
            relationParam = user.getActor();
        }
        boolean inversed = getParameterAs(boolean.class, 3);
        List<Executor> executors = getExecutors(relationName, relationParam, inversed);
        return ViewUtil.createExecutorSelect(variableName, executors, variableProvider.getValue(variableName), true, true);
    }

    private List<Executor> getExecutors(String relationName, Executor relationParam, boolean inversed) {
        List<Executor> executors = new ArrayList<Executor>();
        executors.add(relationParam);
        BatchPresentation batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        executors.addAll(Delegates.getExecutorService().getExecutorGroups(user, relationParam, batchPresentation, false));
        List<RelationPair> pairs;
        if (inversed) {
            pairs = Delegates.getRelationService().getExecutorsRelationPairsLeft(user, relationName, executors);
        } else {
            pairs = Delegates.getRelationService().getExecutorsRelationPairsRight(user, relationName, executors);
        }
        Set<Executor> result = Sets.newHashSet();
        for (RelationPair pair : pairs) {
            Executor executor = inversed ? pair.getRight() : pair.getLeft();
            try {
                Delegates.getExecutorService().getExecutor(user, executor.getId());
                fillExecutors(result, executor);
            } catch (AuthorizationException e) {
                // TODO may be filter executors in logic?
                // http://sourceforge.net/tracker/?func=detail&aid=3478716&group_id=125156&atid=701698
            }
        }
        return new ArrayList<Executor>(result);
    }

    protected abstract void fillExecutors(Set<Executor> result, Executor executor);

}
