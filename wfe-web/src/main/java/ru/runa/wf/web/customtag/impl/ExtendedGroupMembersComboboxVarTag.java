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

public class ExtendedGroupMembersComboboxVarTag extends AbstractActorComboBoxVarTag {

    @Override
    public List<Actor> getActors(User user, String varName) {
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);

        Group group = Delegates.getExecutorService().getExecutorByName(user, varName);
        List<Actor> actors = Lists.newArrayList();
        addActorsRecursive(user, group, batchPresentation, actors);
        return actors;
    }

    private void addActorsRecursive(User user, Executor executor, BatchPresentation batchPresentation, List<Actor> actors) {
        if (executor instanceof Actor) {
            actors.add((Actor) executor);
        } else {
            Group group = (Group) executor;
            List<Executor> executors = Delegates.getExecutorService().getGroupChildren(user, group, batchPresentation, false);
            for (Executor child : executors) {
                addActorsRecursive(user, child, batchPresentation, actors);
            }
        }
    }
}
