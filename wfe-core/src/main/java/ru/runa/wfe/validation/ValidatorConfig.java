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

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValidatorConfig {
    private final String type;
    private final List<String> transitionNames = Lists.newArrayList();
    private final Map<String, String> params = Maps.newHashMap();
    private String message;

    public ValidatorConfig(String type) {
        this.type = type;
    }

    public ValidatorConfig(String validatorType, String message, Map<String, String> params) {
        this.type = validatorType;
        this.message = message;
        this.params.putAll(params);
    }
    
    public Map<String, String> getParams() {
        return params;
    }

    public String getType() {
        return type;
    }

    public List<String> getTransitionNames() {
        return transitionNames;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("type", type).add("params", params).toString();
    }
}
