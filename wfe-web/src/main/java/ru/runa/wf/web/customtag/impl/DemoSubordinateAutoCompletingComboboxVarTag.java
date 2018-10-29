package ru.runa.wf.web.customtag.impl;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.extension.orgfunction.DemoSubordinateRecursive;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationComparator;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * <p>
 * Created on 20.03.2006 17:54:22
 * </p>
 * 
 */

public class DemoSubordinateAutoCompletingComboboxVarTag extends AbstractAutoCompletionComboBoxVarTag {

    @Autowired
    ExecutorDao executorDao;

    @Override
    public List<Actor> getActors(User user, String varName) {
        List<Actor> subordinates = getSubordinates(user);
        BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createDefault();
        int[] sortIds = { 1 };
        boolean[] sortOrder = { true };
        batchPresentation.setFieldsToSort(sortIds, sortOrder);
        Collections.sort(subordinates, new BatchPresentationComparator(batchPresentation));
        return subordinates;
    }

    private List<Actor> getSubordinates(User user) {
        Object[] parameters = new Object[1];
        parameters[0] = Long.toString(user.getActor().getCode());
        List<Actor> actors = new DemoSubordinateRecursive().getSubordinateActors(executorDao, parameters);
        actors.add(0, user.getActor());
        return actors;
    }

}
