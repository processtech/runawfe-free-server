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
package ru.runa.wfe.ss;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

import com.google.common.base.Strings;

/**
 * Substitution with this criteria applies when substitutor not in actor list. Actor list contains executors from process variable with 'conf' name.
 */
@Entity
@DiscriminatorValue(value = "not_equals")
public class SubstitutionCriteriaNotEquals extends SubstitutionCriteria {
    private static final long serialVersionUID = 1L;
    private static final String SWIMLANE_PREFIX = "swimlane:";

    @Override
    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        String variableName = getConfiguration();
        Executor executor;
        if (variableName.startsWith(SWIMLANE_PREFIX)) {
            String swimlaneName = variableName.substring(SWIMLANE_PREFIX.length());
            CurrentSwimlane swimlane = ApplicationContextFactory.getSwimlaneDao().findByProcessAndName(executionContext.getProcess(), swimlaneName);
            if (swimlane == null) {
                return true;
            }
            executor = swimlane.getExecutor();
        } else {
            Object variableValue = executionContext.getVariableProvider().getValue(variableName);
            if (variableValue == null) {
                return true;
            }
            executor = TypeConversionUtil.convertTo(Executor.class, variableValue);
        }
        Set<Executor> confActors = new HashSet<Executor>();
        if (executor instanceof Group) {
            confActors.addAll(ApplicationContextFactory.getExecutorDao().getGroupActors((Group) executor));
        } else {
            confActors.add(executor);
        }
        return !confActors.contains(substitutorActor);
    }

    @Override
    public void validate() {
        if (Strings.isNullOrEmpty(getConfiguration())) {
            throw new InternalApplicationException(getClass().getName() + ": invalid configuration");
        }
    }

}
