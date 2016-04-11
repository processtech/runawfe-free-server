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
package ru.runa.wf.logic.bot.assigner;

import java.util.List;

import com.google.common.collect.Lists;

public class AssignerSettings {
    private final List<Condition> conditions = Lists.newArrayList();

    public void addAssignerCondition(Condition condition) {
        conditions.add(condition);
    }

    public List<Condition> getAssignerConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        return conditions.toString();
    }

    public static class Condition {
        private final String swimlaneName;
        private final String functionClassName;
        private final String variableName;

        public Condition(String swimlaneName, String functionClassName, String variableName) {
            this.swimlaneName = swimlaneName;
            this.functionClassName = functionClassName;
            this.variableName = variableName;
        }

        public String getFunctionClassName() {
            return functionClassName;
        }

        public String getSwimlaneName() {
            return swimlaneName;
        }

        public String getVariableName() {
            return variableName;
        }

        @Override
        public String toString() {
            return swimlaneName + " | " + functionClassName + " | " + variableName;
        }
    }
}
