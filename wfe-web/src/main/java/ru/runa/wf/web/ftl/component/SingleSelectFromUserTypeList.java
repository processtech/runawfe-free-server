package ru.runa.wf.web.ftl.component;

import com.google.common.collect.Maps;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.VariableDefinition;

import java.util.Map;

public class SingleSelectFromUserTypeList extends MultipleSelectFromUserTypeList{

    @Override
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition, Map<String, ?> userInput, Map<String, String> formatErrors) throws Exception {
        final Map<String, Object> result = Maps.newHashMap();
        result.put(getVariableNameForSubmissionProcessing(), userInput.get(getVariableNameForSubmissionProcessing()));
        return result;
    }

}
