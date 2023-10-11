package ru.runa.wf.web.action;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.SignalUtils;
import ru.runa.wf.web.form.SendProcessSignalForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * Created on 10.04.2019
 *
 * @struts:action path="/sendProcessSignal" name="sendProcessSignalForm" validate="false"
 */
public class SendProcessSignalAction extends ActionBase {

    public static final String ACTION_PATH = "/sendProcessSignal";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        SendProcessSignalForm sendForm = (SendProcessSignalForm) form;
        try {
            Map<String, String> routingData = initRoutingMap(sendForm.getRoutingParam(), sendForm.getRoutingValue());
            Map<String, Object> payloadData = initPayloadMap(sendForm, request);
            Delegates.getExecutionService().sendSignal(getLoggedUser(request), routingData, payloadData, 1);
        } catch (Exception e) {
            writeResponse(response, e.getMessage() != null ? e.getMessage() : e.toString());
        }
        return null;
    }

    private void writeResponse(HttpServletResponse response, String data) {
        try {
            response.setContentType("text/html");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "max-age=0");
            OutputStream os = response.getOutputStream();
            os.write(data.getBytes(Charsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> initPayloadMap(SendProcessSignalForm form, HttpServletRequest request) throws Exception {
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
            Map<String, String> errors = new HashMap<>();
            String variableName = entry.getKey();
            VariableDefinition variableDefinition = new VariableDefinition(variableName, null, varToFormatMap.get(variableName));
            Object value = FormSubmissionUtils.extractVariable(request, payloadData, variableDefinition, errors);
            payloadResult.put(variableName, value);
            if (errors.size() > 0) {
                throw new Exception(errors.toString());
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
