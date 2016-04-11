/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

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
import ru.runa.wfe.user.dao.ExecutorDAO;

/**
 * <p>
 * Created on 20.03.2006 17:54:22
 * </p>
 * 
 */

public class DemoSubordinateAutoCompletingComboboxVarTag extends AbstractAutoCompletionComboBoxVarTag {

    @Autowired
    ExecutorDAO executorDAO;

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
        List<Actor> actors = new DemoSubordinateRecursive().getSubordinateActors(executorDAO, parameters);
        actors.add(0, user.getActor());
        return actors;
    }

}
