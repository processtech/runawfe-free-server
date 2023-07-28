package ru.runa.wf.web.customtag.impl;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Created 19.05.2005
 * 
 */
public class GroupMembersComboboxVarTag extends AbstractActorComboBoxVarTag {

    @Override
    public List<Actor> getActors(User user, String varName) {
        BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        Group group = Delegates.getExecutorService().getExecutorByName(user, varName);
        List<Executor> executors = Delegates.getExecutorService().getGroupChildren(user, group, batchPresentation, false);
        List<Actor> actors = Lists.newArrayList();
        for (Executor executor : executors) {
            actors.add((Actor) executor);
        }
        return actors;
    }
}
