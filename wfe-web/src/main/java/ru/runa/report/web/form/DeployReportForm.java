package ru.runa.report.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;
import ru.runa.common.web.form.IdForm;

import com.google.common.base.Strings;

/**
 * Created on 06.10.2004
 * 
 * @struts:form name = "fileForm"
 */
public class DeployReportForm extends IdForm {

    private static final long serialVersionUID = 7850320221673917388L;

    public static final String VAR_USER_NAMES = "varUserName";
    public static final String VAR_DESCRIPTION = "varDescription";
    public static final String VAR_INTERNAL_NAMES = "varInternalName";
    public static final String VAR_POSITION = "varPosition";
    public static final String VAR_TYPE = "varType";
    public static final String VAR_REQUIRED = "varRequired";

    private String[] varUserName;
    private String[] varDescription;
    private String[] varInternalName;
    private String[] varPosition;
    private String[] varType;
    private String[] varRequired;

    public String[] getVarUserName() {
        return varUserName;
    }

    public void setVarUserName(String[] varUserName) {
        this.varUserName = varUserName;
    }

    public String[] getVarDescription() {
        return varDescription;
    }

    public void setVarDescription(String[] varDescription) {
        this.varDescription = varDescription;
    }

    public String[] getVarInternalName() {
        return varInternalName;
    }

    public void setVarInternalName(String[] varInternalName) {
        this.varInternalName = varInternalName;
    }

    public String[] getVarPosition() {
        return varPosition;
    }

    public void setVarPosition(String[] varPosition) {
        this.varPosition = varPosition;
    }

    public String[] getVarType() {
        return varType;
    }

    public void setVarType(String[] varType) {
        this.varType = varType;
    }

    public String[] getVarRequired() {
        return varRequired == null ? new String[0] : varRequired;
    }

    public void setVarRequired(String[] varRequired) {
        this.varRequired = varRequired;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        for (String userName : varUserName) {
            if (Strings.isNullOrEmpty(userName)) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
            }
        }
        return errors;
    }
}
