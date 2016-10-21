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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Strings;

/**
 * Substitution with this criteria applies when task process swimlane equals
 * configuration.
 */
@Entity
@DiscriminatorValue(value = "swimlane")
public class SubstitutionCriteriaSwimlane extends SubstitutionCriteria {
    private static final long serialVersionUID = 812323181231L;

    @Override
    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        String processDefinitionName = executionContext.getProcessDefinition().getName();
        if (task.getSwimlane() == null) {
            return false;
        }
        String taskSwimlaneName = task.getSwimlane().getName();
        String expectedSwimlaneName = processDefinitionName + "." + taskSwimlaneName;
        return expectedSwimlaneName.equals(getConfiguration());
    }

    @Override
    public void validate() {
        if (Strings.isNullOrEmpty(getConfiguration())) {
            throw new InternalApplicationException(getClass().getName() + ": invalid configuration");
        }
    }

}
