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
package ru.runa.af.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.form.IdForm;

import com.google.common.base.Strings;

/**
 * @struts:form name = "substitutionCriteriaForm"
 */
public class SubstitutionCriteriaForm extends IdForm {
    private static final long serialVersionUID = 1L;
    private static final String ERROR_KEY = "substitutionCriteria.params.invalid";

    public static final String NAME_INPUT_NAME = "name";
    public static final String TYPE_INPUT_NAME = "type";
    public static final String CONF_INPUT_NAME = "conf";

    private String name;
    private String type;
    private String conf;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (Strings.isNullOrEmpty(name)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(ERROR_KEY));
        }
        return errors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }
}
