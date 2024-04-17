package ru.runa.wf.web.action;

import com.google.common.base.Objects;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
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
import ru.runa.wf.web.form.VariableForm;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ConvertToSimpleVariables;
import ru.runa.wfe.execution.ConvertToSimpleVariablesContext;
import ru.runa.wfe.execution.ConvertToSimpleVariablesResult;
import ru.runa.wfe.execution.ConvertToSimpleVariablesUnrollContext;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Created on 24.06.2014
 * 
 * @struts:action path="/updateProcessVariable" name="variableForm" validate="false"
 * @struts.action-forward name="success" path="/update_process_variables.do" redirect = "false"
 * @struts.action-forward name="failure" path="/update_process_variables.do" redirect = "false"
 */
public class UpdateProcessVariableAction extends ActionBase {
    public static final String ACTION_PATH = "/updateProcessVariable";

    private static final String REDIRECT_OPTION_PARAM = "redirectOption";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        User user = Commons.getUser(request.getSession());
        VariableForm variableForm = (VariableForm) form;
        Long processId = variableForm.getId();
        Map<String, Object> params = new HashMap<>();
        params.put(ProcessForm.ID_INPUT_NAME, processId);
        try {
            String variableName = variableForm.getVariableName();
            if (variableName == null || variableName.isEmpty()) {
                return Commons.forward(mapping.findForward(request.getParameter(REDIRECT_OPTION_PARAM)), params);
            }
            WfVariable variable = Delegates.getExecutionService().getVariable(user, processId, variableName);
            Object variableValue;
            if ("on".equals(request.getParameter("isNullValue"))) {
                variableValue = null;
            } else {
                variableValue = FormSubmissionUtils.extractVariable(request, form, variable);
            }
            if (!Objects.equal(FormSubmissionUtils.IGNORED_VALUE, variableValue)) {
                MapVariableProvider variableProvider = new MapVariableProvider(new HashMap<>());
                ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesUnrollContext(variable.getDefinition(), variable.getValue());
                for (ConvertToSimpleVariablesResult unrolled : variable.getDefinition().getFormatNotNull().processBy(new ConvertToSimpleVariables(),
                        context)) {
                    variableProvider.add(new WfVariable(unrolled.variableDefinition, unrolled.value));
                }
                variableValue = clearInsignificantData(variableProvider, variable.getDefinition(), variableValue);
            }
            if (isIgnoredValue(variableValue)) {
                addMessage(request, new ActionMessage(MessagesProcesses.VARIABLE_HAS_NOT_CHANGES.getKey()));
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(variableName, variableValue);
                Delegates.getExecutionService().updateVariables(user, processId, map);
                addMessage(request, new ActionMessage(MessagesProcesses.VARIABLE_WAS_UPDATED.getKey()));
            }
            FormSubmissionUtils.clearUserInputFiles(request);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), params);
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), params);
    }

    private boolean isIgnoredValue(Object value) {
        if (Objects.equal(FormSubmissionUtils.IGNORED_VALUE, value)) {
            return true;
        }
        if (value instanceof UserTypeMap && ((UserTypeMap) value).size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Clears values that did not changed. It is needed to prevent fake changes (null -> "", etc)
     */
    private Object clearInsignificantData(VariableProvider variableProvider, VariableDefinition variableDefinition, Object variableValue) {
        if (variableValue instanceof UserTypeMap) {
            clearInsignificantDataInUserTypeMap(variableProvider, variableDefinition, (UserTypeMap) variableValue);
        } else if (variableValue instanceof List) {
            return clearInsignificantDataInList(variableProvider, variableDefinition, (List) variableValue);
        } else if (variableValue instanceof Map) {
            // partial support
            Object oldValue = variableProvider.getValue(variableDefinition.getName());
            if (TypeConversionUtil.getMapSize(variableValue) == 0 && TypeConversionUtil.getMapSize(oldValue) == 0) {
                return FormSubmissionUtils.IGNORED_VALUE;
            }
        } else if (isInsignificantData(variableProvider, variableDefinition, variableValue)) {
            return FormSubmissionUtils.IGNORED_VALUE;
        }
        return variableValue;
    }

    /**
     * @return empty user type map if no changes detected
     */
    private Object clearInsignificantDataInUserTypeMap(VariableProvider variableProvider, VariableDefinition variableDefinition,
            UserTypeMap newValue) {
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            Object componentValue = newValue.get(attributeDefinition.getName());
            if (componentValue == null) {
                newValue.remove(attributeDefinition.getName());
                continue;
            }
            String name = variableDefinition.getName() + UserType.DELIM + attributeDefinition.getName();
            String scriptingName = variableDefinition.getScriptingName() + UserType.DELIM + attributeDefinition.getScriptingName();
            VariableDefinition componentDefinition = new VariableDefinition(name, scriptingName, attributeDefinition);
            Object clearedValue = clearInsignificantData(variableProvider, componentDefinition, componentValue);
            if (isIgnoredValue(clearedValue)) {
                newValue.remove(attributeDefinition.getName());
            }
        }
        return newValue;
    }

    /**
     * @return ignored value if all items contains any changes; otherwise list with real size
     */
    private Object clearInsignificantDataInList(VariableProvider variableProvider, VariableDefinition variableDefinition, List<Object> newValue) {
        List<Object> oldValue = (List<Object>) variableProvider.getValue(variableDefinition.getName());
        boolean allComponentsAreIgnored = true;
        if (newValue.size() != TypeConversionUtil.getListSize(oldValue)) {
            allComponentsAreIgnored = false;
        }
        VariableFormat componentFormat = FormatCommons.createComponent(variableDefinition, 0);
        for (int index = 0; index < newValue.size(); index++) {
            String name = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END;
            String scriptingName = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END;
            VariableDefinition componentDefinition = new VariableDefinition(name, scriptingName, componentFormat);
            if (componentFormat instanceof UserTypeFormat) {
                Object clearedValue = clearInsignificantDataInUserTypeMap(variableProvider, componentDefinition, (UserTypeMap) newValue.get(index));
                if (!isIgnoredValue(clearedValue)) {
                    allComponentsAreIgnored = false;
                }
            } else {
                if (!isInsignificantData(variableProvider, componentDefinition, newValue.get(index))) {
                    allComponentsAreIgnored = false;
                    break;
                }
            }
        }
        if (allComponentsAreIgnored) {
            return FormSubmissionUtils.IGNORED_VALUE;
        }
        return newValue;
    }

    private boolean isInsignificantData(VariableProvider variableProvider, VariableDefinition variableDefinition, Object newValue) {
        Object oldValue = variableProvider.getValue(variableDefinition.getName());
        if (Objects.equal(newValue, oldValue)) {
            return true;
        }
        if (newValue instanceof Boolean) {
            if (Boolean.FALSE.equals(newValue)) {
                Object value = variableProvider.getValue(variableDefinition.getName());
                if (!Boolean.TRUE.equals(value)) {
                    return true;
                }
            }
        }
        if (newValue instanceof String) {
            if (StringUtils.isBlank((String) newValue) && StringUtils.isBlank((String) oldValue)) {
                // do not change "" -> " ", null -> "", etc
                return true;
            }
            if (oldValue instanceof String
                    && ((String) newValue).trim().replaceAll("\r", "").equals(((String) oldValue).trim().replaceAll("\r", ""))) {
                // do not change " something" -> "something", " some\r\nthing" -> "some\nthing"
                return true;
            }
        }
        if (newValue instanceof Date && oldValue instanceof Date) {
            VariableFormat format = FormatCommons.create(variableDefinition);
            if (format.format(newValue).equals(format.format(oldValue))) {
                // ignore non-significant changes
                return true;
            }
        }
        return false;
    }

}
