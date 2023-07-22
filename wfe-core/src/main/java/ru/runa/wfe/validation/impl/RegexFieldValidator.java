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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.validation.FieldValidator;

import com.google.common.base.Strings;

public class RegexFieldValidator extends FieldValidator {

    protected String getExpression() {
        return getParameterNotNull(String.class, "expression");
    }

    protected boolean isCaseSensitive() {
        return getParameter(boolean.class, "caseSensitive", true);
    }

    @Override
    public void validate() {
        String value = TypeConversionUtil.convertTo(String.class, getFieldValue());
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (Strings.isNullOrEmpty(value)) {
            return;
        }
        String expression = getExpression();
        boolean caseSensitive = isCaseSensitive();
        boolean trim = getParameter(boolean.class, "trim", true);

        // match against expression
        Pattern pattern;
        if (caseSensitive) {
            pattern = Pattern.compile(expression);
        } else {
            pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        }

        String compare = value;
        if (trim) {
            compare = compare.trim();
        }
        Matcher matcher = pattern.matcher(compare);

        if (!matcher.matches()) {
            addError();
        }
    }

}
