package ru.runa.wf.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.SignalUtils;
import ru.runa.wf.web.VariablesFormatException;
import ru.runa.wf.web.form.SendProcessSignalForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * Created on 10.04.2019
 *
 * @struts:action path="/sendProcessSignal" name="sendProcessSignalForm" validate="true" input = "/WEB-INF/wf/send_process_signal.jsp"
 * @struts.action-forward name="success" path="/manage_processes.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_processes.do" redirect = "true"
 */
public class SendProcessSignalAction extends ActionBase {

    public static final String ACTION_PATH = "/sendProcessSignal";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        SendProcessSignalForm sendForm = (SendProcessSignalForm) form;
        Map<String, String> routingData = initRoutingMap(sendForm.getRoutingParam(), sendForm.getRoutingValue());
        Map<String, Object> payloadData = initPayloadMap(sendForm, request);

        ActionMessages errors = getActionMessages(request, Globals.ERROR_KEY);

        if (!errors.isEmpty()) {
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), new HashMap<>());
        }
        try {
            Delegates.getExecutionService().sendSignal(getLoggedUser(request), routingData, payloadData, 1);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), new HashMap<>());
        }
        addMessage(request, new ActionMessage(MessagesProcesses.SIGNAL_MESSAGE_IS_SENT.getKey()));
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    private ActionMessages getActionMessages(HttpServletRequest request, String key) {
        ActionMessages errors = (ActionMessages) request.getAttribute(key);
        if (errors == null) {
            errors = (ActionMessages) request.getSession().getAttribute(key);
        }
        if (errors == null) {
            errors = new ActionMessages();
        }
        return errors;
    }

    private Map<String, Object> initPayloadMap(SendProcessSignalForm form, HttpServletRequest request) {
        Map<Integer, String[]> paramMap = form.getPayloadParam();
        Map<Integer, String[]> valueMap = form.getPayloadValue();
        Map<Integer, String[]> payloadType = form.getPayloadType();

        Map<String, String> payloadData = new HashMap<>();
        Map<String, VariableFormat> varToFormatMap = new HashMap<>();
        varToFormatMap.put("String", new StringFormat());

        for (Map.Entry<Integer, String[]> entry : paramMap.entrySet()) {
            String param = entry.getValue()[0];
            String[] valueArray = valueMap.get(entry.getKey());
            String[] typeArray = payloadType.get(entry.getKey());
            if (valueArray != null && typeArray != null && valueArray.length > 0 && typeArray.length > 0) {
                String type = typeArray[0];
                VariableFormat variableFormat = SignalUtils.getFormatMap().get(type);
                String value = valueArray[0];
                payloadData.put(param, value);
                varToFormatMap.put(param, variableFormat);
            }
        }
        Map<String, Object> payloadResult = new HashMap<>();

        for (Map.Entry<String, String> entry : payloadData.entrySet()) {
            try {
                Map<String, String> errors = new HashMap<>();
                String variableName = entry.getKey();
                VariableDefinition variableDefinition = new VariableDefinition(variableName, null, varToFormatMap.get(variableName));
                Object value = FormSubmissionUtils.extractVariable(request, payloadData, variableDefinition, errors);
                payloadResult.put(variableName, value);
                if (errors.size() > 0) {
                    List<String> errorValues = new ArrayList<>();
                    for (Map.Entry<String, String> errorEntry : errors.entrySet()) {
                        errorValues.add(errorEntry.getKey() + " ->" + errorEntry.getValue());
                    }
                    throw new VariablesFormatException(errorValues);
                }
            } catch (Exception e) {
                addError(request, e);
            }
        }
        return payloadResult;
    }

    private Map<String, String> initRoutingMap(Map<Integer, String[]> paramMap, Map<Integer, String[]> valueMap) {
        Map<String, String> payloadData = new HashMap<>();
        for (Map.Entry<Integer, String[]> entry : paramMap.entrySet()) {
            String param = entry.getValue()[0];
            String value = valueMap.get(entry.getKey())[0];
            if (value != null) {
                payloadData.put(param, value);
            }
        }
        return payloadData;
    }

}
