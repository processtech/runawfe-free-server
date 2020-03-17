package ru.runa.wf.web.form;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @struts:form name = "sendProcessSignalForm"
 */
@Data
public class SendProcessSignalForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    private Map<Integer, String[]> routingParam = new HashMap<>();
    private Map<Integer, String[]> routingValue = new HashMap<>();
    private Map<Integer, String[]> payloadParam = new HashMap<>();
    private Map<Integer, String[]> payloadValue = new HashMap<>();
    private Map<Integer, String[]> payloadType = new HashMap<>();


    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        return errors;
    }

}
