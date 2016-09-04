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

package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class FormatCommons {

    private static VariableFormat create(String className, UserType userType) {
        if (userType != null) {
            return new UserTypeFormat(userType);
        }
        if (className == null) {
            className = "ru.runa.wfe.var.format.StringFormat";
        }
        VariableFormat format = ClassLoaderUtil.instantiate(className);
        if (format instanceof VariableFormatContainer) {
            // see
            // ru.runa.wfe.var.VariableDefinition.initComponentUserTypes(IUserTypeLoader)
        }
        return format;
    }

    public static VariableFormat create(VariableDefinition variableDefinition) {
        VariableFormat format = create(variableDefinition.getFormatClassName(), variableDefinition.getUserType());
        if (format instanceof VariableFormatContainer) {
            ((VariableFormatContainer) format).setComponentClassNames(variableDefinition.getFormatComponentClassNames());
            ((VariableFormatContainer) format).setComponentUserTypes(variableDefinition.getFormatComponentUserTypes());
        }
        return format;
    }

    public static VariableFormat createComponent(VariableFormatContainer formatContainer, int index) {
        String elementFormatClassName = formatContainer.getComponentClassName(index);
        return create(elementFormatClassName, formatContainer.getComponentUserType(index));
    }

    public static VariableFormat createComponent(VariableDefinition variableDefinition, int index) {
        String elementFormatClassName = ((VariableFormatContainer) variableDefinition.getFormatNotNull()).getComponentClassName(index);
        return create(elementFormatClassName, variableDefinition.getFormatComponentUserTypes()[index]);
    }

    public static VariableFormat createComponent(WfVariable containerVariable, int index) {
        return createComponent(containerVariable.getDefinition(), index);
    }

    public static String formatComponentValue(WfVariable containerVariable, int index, Object value) {
        return createComponent(containerVariable, index).format(value);
    }
}
