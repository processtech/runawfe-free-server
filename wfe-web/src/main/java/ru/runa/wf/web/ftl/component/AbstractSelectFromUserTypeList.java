package ru.runa.wf.web.ftl.component;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public abstract class AbstractSelectFromUserTypeList extends AbstractUserTypeList implements FormComponentSubmissionHandler {
    private static final long serialVersionUID = 1L;

    protected abstract boolean isMultiple();

    @Override
    protected UserTypeListModel parseParameters() {
        String inputVariableName = getParameterAsString(0);
        WfVariable inputVariable = variableProvider.getVariableNotNull(inputVariableName);
        String displayVariableName = getParameterAsString(1);
        boolean componentView = getParameterAs(boolean.class, 2);
        WfVariable displayVariable = variableProvider.getVariableNotNull(displayVariableName);
        List<String> displayFields = getMultipleParameter(3);
        return new SelectUserTypeListModel(displayVariable, displayFields, componentView, inputVariable);
    }

    @Override
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition,
            Map<String, ? extends Object> userInput, Map<String, String> formatErrors) throws Exception {
        Object selected = null;
        String[] indexes = (String[]) userInput.get(getVariableNameForSubmissionProcessing());
        if (indexes != null) {
            List<?> list = getParameterVariableValueNotNull(List.class, 1);
            if (isMultiple()) {
                selected = new ArrayList<>(indexes.length);
                for (String index : indexes) {
                    ((List<Object>) selected).add(list.get(TypeConversionUtil.convertTo(int.class, index)));
                }
            } else {
                selected = list.get(TypeConversionUtil.convertTo(int.class, indexes[0]));
            }
        }
        final Map<String, Object> result = new HashMap<>(1);
        result.put(getVariableNameForSubmissionProcessing(), selected);
        return result;
    }

    public class SelectUserTypeListModel extends UserTypeListModel {
        private final WfVariable inputVariable;

        public SelectUserTypeListModel(WfVariable variable, List<String> attributeNames, boolean componentView, WfVariable inputVariable) {
            super(variable, attributeNames, componentView);
            this.inputVariable = inputVariable;
        }

        public WfVariable getInputVariable() {
            return inputVariable;
        }

        public String getChecked(TemplateModel arg0) throws TemplateModelException {
            UserTypeMap userTypeMap = (UserTypeMap) BEANS_WRAPPER.unwrap(arg0);
            List<UserTypeMap> selectedList = (List<UserTypeMap>) getInputVariable().getValue();
            boolean checked = selectedList != null && selectedList.contains(userTypeMap);
            if (checked) {
                return "checked='true'";
            }
            return "";
        }

    }

}
