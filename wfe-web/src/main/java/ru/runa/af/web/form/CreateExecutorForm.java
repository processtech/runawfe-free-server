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

import ru.runa.common.web.Messages;

/*
 * Created on 20.08.2004
 */
/**
 * @struts:form name = "createExecutorForm"
 */
public class CreateExecutorForm extends UpdateExecutorDetailsForm {

    private static final long serialVersionUID = 2563437284265955525L;

    public static final String EXECUTOR_TYPE_INPUT_NAME = "executorType";

    private String executorType;

    public static final String TYPE_GROUP = "group";

    public static final String TYPE_ACTOR = "actor";

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (getExecutorType() == null || getExecutorType().length() < 1) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_NULL_VALUE));
        }
        if (getExecutorType() != null && !CreateExecutorForm.TYPE_ACTOR.equals(getExecutorType())
                && !CreateExecutorForm.TYPE_GROUP.equals(getExecutorType())) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.EXCEPTION_UNKNOWN, "Unknown type " + getExecutorType()));
        }
        return errors;
    }

    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String string) {
        executorType = string;
    }
}
