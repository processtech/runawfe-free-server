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
