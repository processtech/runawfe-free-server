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

import com.google.common.base.Strings;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesException;
import ru.runa.common.web.form.IdForm;

/**
 * Created on 24.08.2004
 * 
 * @struts:form name = "updatePasswordForm"
 */
public class UpdatePasswordForm extends IdForm {
    private static final long serialVersionUID = -6455786382107126804L;

    public static final String PASSWORD_INPUT_NAME = "password";

    private String password;

    public static final String PASSWORD_CONFIRM_INPUT_NAME = "passwordConfirm";

    private String passwordConfirm;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (Strings.isNullOrEmpty(password)) {
            // ok, allow empty passwords
        } else if (password.length() > WebResources.VALIDATOR_STRING_255) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_VALIDATION.getKey()));
        } else if (passwordConfirm == null || passwordConfirm.length() < 1) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        } else if (!password.equals(passwordConfirm)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_PASSWORDS_NOT_MATCH.getKey()));
        }
        return errors;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPassword(String string) {
        password = Strings.nullToEmpty(string).trim();
    }

    public void setPasswordConfirm(String string) {
        passwordConfirm = string;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
    }
}
