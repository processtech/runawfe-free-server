package ru.runa.wf.web.ftl.component;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultipleSelectFromUserTypeList extends AbstractUserTypeList implements FormComponentSubmissionHandler {
    private static final long serialVersionUID = 1L;

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
        final Map<String, Object> result = Maps.newHashMap();
        String[] indexes = (String[]) userInput.get(getVariableNameForSubmissionProcessing());
        if (indexes == null || indexes.length == 0) {
            return result;
        }
        List<?> list = getParameterVariableValue(List.class, 1, null);
        List<Object> selected = Lists.newArrayListWithExpectedSize(indexes.length);
        for (String index : indexes) {
            selected.add(list.get(TypeConversionUtil.convertTo(int.class, index)));
        }
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
    }

}
