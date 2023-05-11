package ru.runa.af.web.form;
/*
 * Created on 25.08.2022
 */

import com.google.common.base.Strings;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesException;
import ru.runa.common.web.form.IdForm;
import javax.servlet.http.HttpServletRequest;


/**
 * @struts:form name = "updateDigitalSignatureDetailsForm"
 */
@Getter
@Setter
public class UpdateDigitalSignatureDetailsForm extends IdForm {
    private static final long serialVersionUID = 4887945348189814199L;

    public static final String EXECUTOR_ID_INPUT_NAME = "executorId";
    public static final String COMMON_NAME_INPUT_NAME = "commonName";
    public static final String EMAIL_INPUT_NAME = "email";
    public static final String DEPARTMENT_INPUT_NAME = "department";
    public static final String ORGANIZATION_INPUT_NAME = "organization";
    public static final String CITY_INPUT_NAME = "city";
    public static final String STATE_INPUT_NAME = "state";
    public static final String COUNTRY_INPUT_NAME = "country";
    public static final String VALIDITY_INPUT_NAME = "validity";
    public static final String DATE_OF_ISSUE_OUTPUT_NAME = "dateOfIssue";
    public static final String DATE_OF_EXPIRY_OUTPUT_NAME = "dateOfExpiry";

    private String commonName;
    private String email;
    private String department;
    private String organization;
    private String city;
    private String state;
    private String country;
    private String validity;
    private String dateOfIssue;
    private String dateOfExpiry;
    private String executorId;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (Strings.isNullOrEmpty(getCommonName())) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_FILL_REQUIRED_VALUES.getKey()));
        } else if (getCommonName().length() > WebResources.VALIDATOR_STRING_64) {
            ActionMessage message = new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey());

            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        }
        if (getDepartment() == null) {
            setDepartment("");
        } else if (getDepartment().length() > WebResources.VALIDATOR_STRING_64) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        }
        if (getOrganization() == null) {
            setOrganization("");
        } else if (getOrganization().length() > WebResources.VALIDATOR_STRING_64) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        }

        if (getEmail() == null) {
            setEmail("");
        } else if (getEmail().length() > WebResources.VALIDATOR_STRING_64) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        } else if (!getEmail().isEmpty()) {
            try {
                InternetAddress email = new InternetAddress(getEmail());
                email.validate();
            } catch (AddressException ex) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_EMAIL_NOT_VALID.getKey()));
            }
        }
        if (getCountry() == null) {
            setCountry("RU");
        } else if (getCountry().length() > WebResources.VALIDATOR_STRING_2) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        } else if (getCountry().length() < WebResources.VALIDATOR_STRING_2) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_LESS.getKey()));
        } else if (!getCountry().matches("[A-Z][A-Z]")) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_NOT_CAPITAL_LETTER.getKey()));
        }
        if (getState() == null) {
            setState("");
        } else if (getState().length() > WebResources.VALIDATOR_STRING_64) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        }
        if (getCity() == null) {
            setCity("");
        } else if (getCity().length() > WebResources.VALIDATOR_STRING_64) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        }

        if (getValidity() == null) {
            setValidity("12");
        } else if (getValidity().length() > WebResources.VALIDATOR_STRING_3) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_ERROR_STRING_LENGTH_MORE.getKey()));
        }
        else  {
            try  {
                int validity =  Integer.parseInt(getValidity());
                if (validity < 0) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_VALIDITY_CANT_BE_NEGATIVE.getKey()));
                } else if (validity == 0) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_VALIDITY_CANT_BE_ZERO.getKey()));
                }
            } catch (NumberFormatException e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.DS_VALIDITY_MUST_BE_NUMBER.getKey()));
            }
        }

        return errors;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        setCommonName("");
        setDepartment("");
        setOrganization("");
        setEmail("");
        setCountry("");
        setState("");
        setCity("");
        setValidity("");
    }
}
