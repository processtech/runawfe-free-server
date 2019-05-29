package ru.runa.wf.web.customtag.impl;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * Created on 10.11.2005
 * 
 */
public class ActorComboboxVarTag extends AbstractActorComboBoxVarTag {

    @Override
    public List<Actor> getActors(User user, String varName) {
        BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createDefault();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        return (List<Actor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
    }

    @Override
    public String getActorPropertyToUse() {
        return "code";
    }

    @Override
    public String getActorPropertyToDisplay() {
        return "fullName";
    }
}
