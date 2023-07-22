package ru.runa.wf.web.form;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.struts.action.ActionForm;

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

}
