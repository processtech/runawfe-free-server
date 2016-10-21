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
import ru.runa.wfe.var.file.IFileVariable;

public class FileSizeValidator extends FieldValidator {

    @Override
    public void validate() {
        IFileVariable fileVariable = (IFileVariable) getFieldValue();
        if (fileVariable == null) {
            // use a required validator for these
            return;
        }

        int fileSize = fileVariable.getData().length;
        int minLength = getParameter(int.class, "minLength", -1);
        int maxLength = getParameter(int.class, "maxLength", -1);
        if ((minLength > -1) && (fileSize < minLength)) {
            addError();
        } else if ((maxLength > -1) && (fileSize > maxLength)) {
            addError();
        }
    }
}
