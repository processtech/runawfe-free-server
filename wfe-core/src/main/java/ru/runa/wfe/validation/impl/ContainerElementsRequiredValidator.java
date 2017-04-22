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
package ru.runa.wfe.validation.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class ContainerElementsRequiredValidator extends FieldValidator {

    @Override
    public void validate() {
        Object container = getFieldValue();
        if (container == null) {
            // use a required validator for these
            return;
        }
        checkValue(getFieldName(), container, true);
    }

    private void checkValue(String variableName, Object value, boolean requireContainerType) {
        if (value == null) {
            getValidatorContext().addFieldError(variableName, getMessage());
        } else if (value instanceof Collection) {
            int index = 0;
            for (Object object : (Collection<?>) value) {
                String itemVariableName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                checkValue(itemVariableName, object, false);
                index++;
            }
        } else if (value.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(value); i++) {
                String itemVariableName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                checkValue(itemVariableName, Array.get(value, i), false);
            }
        } else if (value instanceof UserTypeMap) {
            for (Map.Entry<String, Object> entry : ((UserTypeMap) value).entrySet()) {
                checkValue(variableName + UserType.DELIM + entry.getKey(), entry.getValue(), false);
            }
        } else if (value instanceof Map<?, ?>) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                checkValue(variableName, entry.getKey(), false);
                checkValue(variableName, entry.getValue(), false);
            }
        } else if (requireContainerType) {
            addError("Unexpected variable type: " + value.getClass());
        } else if (value instanceof String && ((String) value).isEmpty()) {
            getValidatorContext().addFieldError(variableName, getMessage());
        }
    }

}
