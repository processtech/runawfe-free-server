package ru.runa.af.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

import com.google.common.base.Strings;

/**
 * Created on 09.08.2004
 * 
 * @struts:form name="loginForm"
 */
public class LoginForm extends ActionForm {

    private static final long serialVersionUID = -5429126182389050058L;

    private String password;

    private String login;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (Strings.isNullOrEmpty(login)) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_FILL_REQUIRED_VALUES.getKey()));
        }
        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        password = "";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
