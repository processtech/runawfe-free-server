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
package ru.runa.wfe.lang;

import java.util.List;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public abstract class VariableContainerNode extends Node {
    private static final long serialVersionUID = 1L;
    protected final List<VariableMapping> variableMappings = Lists.newArrayList();

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variableMappings) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variableMappings);
    }

    public boolean isInBaseIdProcessMode() {
        String baseProcessIdVariableName = SystemProperties.getBaseProcessIdVariableName();
        if (baseProcessIdVariableName != null) {
            for (VariableMapping variableMapping : variableMappings) {
                if (baseProcessIdVariableName.equals(variableMapping.getMappedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getNodeId()).add("name", getName()).add("mappings", getVariableMappings()).toString();
    }

}
