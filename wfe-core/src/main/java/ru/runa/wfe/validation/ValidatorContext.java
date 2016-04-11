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
package ru.runa.wfe.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValidatorContext {
    private Collection<String> globalErrors = Lists.newArrayList();
    private Map<String, List<String>> fieldErrors = Maps.newHashMap();

    public Collection<String> getGlobalErrors() {
        return globalErrors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    public void addGlobalError(String anErrorMessage) {
        globalErrors.add(anErrorMessage);
    }

    public void addFieldError(String fieldName, String errorMessage) {
        List<String> thisFieldErrors = fieldErrors.get(fieldName);
        if (thisFieldErrors == null) {
            thisFieldErrors = new ArrayList<String>();
            fieldErrors.put(fieldName, thisFieldErrors);
        }
        thisFieldErrors.add(errorMessage);
    }

    public boolean hasGlobalErrors() {
        return !globalErrors.isEmpty();
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }

}
