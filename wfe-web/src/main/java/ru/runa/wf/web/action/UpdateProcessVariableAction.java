package ru.runa.wf.web.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Created on 24.06.2014
 *
 * @struts:action path="/updateProcessVariable" name="commonProcessForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/manage_process.do" redirect =
 *                        "true"
 * @struts.action-forward name="failure" path="/update_process_variables.do"
 *                        redirect = "false"
 */
public class UpdateProcessVariableAction extends ActionBase {
    public static final String ACTION_PATH = "/updateProcessVariable";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = Commons.getUser(request.getSession());
        Long processId = Long.valueOf(request.getParameter("id"));
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ProcessForm.ID_INPUT_NAME, processId);
        try {
            String variableName = request.getParameter("variableSelect");
            WfVariable variable = Delegates.getExecutionService().getVariable(user, processId, variableName);
            Preconditions.checkNotNull(variable, variableName);
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
                map = new HashMap<String, Object>();
                map.put(variableName, variableValue);
            }
            Delegates.getExecutionService().updateVariables(user, processId, map);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        }
        FormSubmissionUtils.getUploadedFilesMap(request).clear();
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }

    private Map<String, Object> getValues(String variableName, UserTypeMap existingUserTypeMap, UserTypeMap userTypeMap) {
        Map<String, Object> existingMap = existingUserTypeMap.expand(variableName);
        Map<String, Object> variableMap = userTypeMap.expand(variableName);
        for (Map.Entry<String, Object> entry : Sets.newHashSet(variableMap.entrySet())) {
            if (isDefaultValue(existingMap.get(entry.getKey())) && isDefaultValue(entry.getValue())) {
                variableMap.remove(entry.getKey());
            }
        }
        return variableMap;
    }

    private boolean isDefaultValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String && ((String) value).length() == 0) {
            return true;
        }
        if (value instanceof Boolean && (Boolean) value == Boolean.FALSE) {
            return true;
        }
        if (TypeConversionUtil.isList(value) && TypeConversionUtil.getListSize(value) == 0) {
            return true;
        }
        return false;
    }
}
