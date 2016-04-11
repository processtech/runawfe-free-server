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
package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.Messages;

/**
 * @struts:form name = "substitutionCriteriasForm"
 */
public class SubstitutionCriteriasForm extends ActionForm {

    private static final long serialVersionUID = 57003922541843531L;

    public static final String IDS_INPUT_NAME = "ids";
    public static final String REMOVE_METHOD_INPUT_NAME = "removeMethod";

    public static final String REMOVE_METHOD_CONFIRM = "confirm";
    public static final String REMOVE_METHOD_ALL = "all";
    public static final String REMOVE_METHOD_ONLY = "only";

    private Long[] ids;
    private String removeMethod;

    public Long[] getIds() {
        return ids.clone();
    }

    public void setIds(Long[] ids) {
        this.ids = ids.clone();
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        ids = new Long[0];
    }

    public String getRemoveMethod() {
        return removeMethod;
    }

    public void setRemoveMethod(String removeMethod) {
        this.removeMethod = removeMethod;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (ids == null || getRemoveMethod() == null || getRemoveMethod().isEmpty()) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_NULL_VALUE));
        }
        return errors;
    }
}
