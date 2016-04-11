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

import ru.runa.common.WebResources;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;

import com.google.common.base.Strings;

/**
 * @struts:form name = "updateExecutorDetailsForm"
 */
public class UpdateExecutorDetailsForm extends IdForm {
    private static final long serialVersionUID = 7419045992229129932L;

    public static final String NEW_NAME_INPUT_NAME = "newName";

    private String newName;

    public static final String DESCRIPTION_INPUT_NAME = "description";

    private String description;

    public static final String FULL_NAME_INPUT_NAME = "fullName";

    private String fullName;

    public static final String CODE_INPUT_NAME = "code";

    private Long code;

    private String email;

    private String phone;

    public static final String EMAIL_INPUT_NAME = "email";

    public static final String PHONE_INPUT_NAME = "phone";

    public String getDescription() {
        return description;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNewName() {
        return newName;
    }

    public void setDescription(String string) {
        description = string;
    }

    public void setFullName(String string) {
        fullName = string;
    }

    public void setNewName(String string) {
        newName = string;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (Strings.isNullOrEmpty(getNewName())) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_FILL_REQUIRED_VALUES));
        } else if (getNewName().length() > WebResources.VALIDATOR_STRING_255) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_VALIDATION));
        }

        if (getDescription() == null) {
            setDescription("");
        } else if (getDescription().length() > 1024) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_VALIDATION));
        }
        if (getFullName() == null) {
            setFullName("");
        } else if (getFullName().length() > WebResources.VALIDATOR_STRING_255) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_VALIDATION));
        }
        if (getEmail() == null) {
            setEmail("");
        } else if (getEmail().length() > WebResources.VALIDATOR_STRING_255) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(Messages.ERROR_VALIDATION));
        }
        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        setNewName("");
        setDescription("");
        setFullName("");
        setCode(null);
        setEmail("");
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }
}
