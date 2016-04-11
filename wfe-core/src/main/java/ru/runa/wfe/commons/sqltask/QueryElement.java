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
package ru.runa.wfe.commons.sqltask;

/**
 * 
 * Created on 08.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
abstract public class QueryElement {

    private final String variableName;

    private String fieldName;

    private boolean hasField = false;

    /**
     * @param variableName
     *            process variable name
     */
    public QueryElement(String variableName) {
        this.variableName = variableName;
    }

    /**
     * 
     * @param variableName
     *            process variable name
     * @param fieldName
     *            field of variable
     */
    public QueryElement(String variableName, String fieldName) {
        this(variableName);
        if ((fieldName != null) && (fieldName.length() > 0)) {
            this.fieldName = fieldName;
            hasField = true;
        }
    }

    public String getVariableName() {
        return variableName;
    }

    public boolean isFieldSetup() {
        return hasField;
    }

    public String getFieldName() {
        return fieldName;
    }
}
