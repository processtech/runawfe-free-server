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
package ru.runa.af.web.tag;

import java.util.ArrayList;
import java.util.List;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;

import com.google.common.base.Strings;

public class ActorSelect extends Select {
    private static final long serialVersionUID = 1L;

    public ActorSelect(User user, String name, String current, boolean actorsOnly) {
        super(name);
        boolean exist = false;
        BatchPresentation batchPresentation;
        if (actorsOnly) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
        } else {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        }
        batchPresentation.setFieldsToSort(new int[] { 0 }, new boolean[] { true });
        List<Executor> executors = (List<Executor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        ArrayList<Option> options = new ArrayList<Option>();
        for (Executor executor : executors) {
            if (executor instanceof TemporaryGroup) {
                continue;
            }
            Option option = new Option();
            option.setValue(executor.getName());
            String label = executor.getName();
            if (executor instanceof Actor && !Strings.isNullOrEmpty(((Actor) executor).getLabel())) {
                label += " (" + executor.getLabel() + ")";
            }
            option.addElement(label);
            if (executor.getName().equals(current)) {
                option.setSelected(true);
                exist = true;
            }
            options.add(option);
        }
        if (!exist && !Strings.isNullOrEmpty(current)) {
            Option option = new Option();
            option.setValue(current);
            option.addElement(current);
            option.setSelected(true);
            option.setDisabled(true);
            options.add(option);
        }
        addElement(options.toArray(new Option[options.size()]));
    }
}
