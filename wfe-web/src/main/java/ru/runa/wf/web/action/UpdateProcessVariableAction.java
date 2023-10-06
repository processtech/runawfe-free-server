package ru.runa.wf.web.action;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.BooleanFormat;

/**
 * Created on 24.06.2014
 * 
 * @struts:action path="/updateProcessVariable" name="commonProcessForm" validate="false"
 * @struts.action-forward name="success" path="/update_process_variables.do" redirect = "false"
 * @struts.action-forward name="failure" path="/update_process_variables.do" redirect = "false"
 */
public class UpdateProcessVariableAction extends ActionBase {
    public static final String ACTION_PATH = "/updateProcessVariable";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = Commons.getUser(request.getSession());
        Long processId = Long.valueOf(request.getParameter("id"));
        Map<String, Object> params = new HashMap<>();
        params.put(ProcessForm.ID_INPUT_NAME, processId);
        try {
            String variableName = request.getParameter("variableName");
            if (variableName == null || variableName.isEmpty()) {
                log.warn("No variableName has been provided, seems like a user copied URL for page");
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
            }
            WfVariable variable = Delegates.getExecutionService().getVariable(user, processId, variableName);
            if (variable == null) {
                throw new InternalApplicationException("Variable \"" + variableName + "\" is not found");
            }
            Object variableValue;
            if ("on".equals(request.getParameter("isNullValue"))) {
                variableValue = null;
            } else {
                variableValue = FormSubmissionUtils.extractVariable(request, form, variable.getDefinition());
            }
            Map<String, Object> map;
            if (variableValue instanceof UserTypeMap && variable.getValue() instanceof UserTypeMap) {
                map = getValues(variableName, (UserTypeMap) variable.getValue(), (UserTypeMap) variableValue);
            } else {
                map = new HashMap<>();
                map.put(variableName, variableValue);
            }
            Delegates.getExecutionService().updateVariables(user, processId, map);
            addMessage(request, new ActionMessage(MessagesProcesses.VARIABLE_WAS_UPDATED.getKey()));
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        }
        FormSubmissionUtils.clearUserInputFiles(request);
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }

    private Map<String, Object> getValues(String variableName, UserTypeMap existingUserTypeMap, UserTypeMap userTypeMap) {
        Map<String, Object> existingMap = existingUserTypeMap.expand(variableName);
        Map<VariableDefinition, Object> variableMap = userTypeMap.expandAttributes(variableName);
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<VariableDefinition, Object> entry : variableMap.entrySet()) {
            Object existingVariableValue = existingMap.get(entry.getKey().getName());
            if (isDefaultValue(entry.getKey().getFormat(), existingVariableValue) && isDefaultValue(entry.getKey().getFormat(), entry.getValue())) {
                continue;
            }
            result.put(entry.getKey().getName(), entry.getValue());
        }
        return result;
    }

    private boolean isDefaultValue(String format, Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String && String.valueOf(value).isEmpty()) {
            return true;
        }
        if (Boolean.FALSE.equals(value) && BooleanFormat.class.getName().equals(format)) {
            return true;
        }
        if (TypeConversionUtil.isList(value) && TypeConversionUtil.getListSize(value) == 0) {
            return true;
        }
        return false;
    }
}
