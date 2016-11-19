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

import ru.runa.wfe.validation.FieldValidator;

public class StringLengthFieldValidator extends FieldValidator {

    // also used in wfform-validate.ftl
    public boolean getTrim() {
        return getParameter(boolean.class, "doTrim", true);
    }

    public int getMinLength() {
        return getParameter(int.class, "minLength", -1);
    }

    public int getMaxLength() {
        return getParameter(int.class, "maxLength", -1);
    }

    @Override
    public void validate() {
        String val = (String) getFieldValue();
        if (val == null) {
            // use a required validator for these
            return;
        }
        if (getTrim()) {
            val = val.trim();
            if (val.length() == 0) {
                return;
            }
        }
        int minLength = getMinLength();
        int maxLength = getMaxLength();
        if ((minLength > -1) && (val.length() < minLength)) {
            addError();
        } else if ((maxLength > -1) && (val.length() > maxLength)) {
            addError();
        }
    }
}
