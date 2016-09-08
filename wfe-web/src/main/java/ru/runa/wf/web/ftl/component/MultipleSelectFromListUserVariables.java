package ru.runa.wf.web.ftl.component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MultipleSelectFromListUserVariables extends AbstractListUserVariables implements FormComponentSubmissionHandler {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        initFields();
        return ViewUtil.getUserTypeListTable(user, webHelper, variableProvider.getVariableNotNull(variableName),
                variableProvider.getVariableNotNull(dectVariableName), variableProvider.getProcessId(), sortField,
                displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE);
    }

    @Override
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition,
            Map<String, ? extends Object> userInput, Map<String, String> formatErrors) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        // TODO use index for submission
        Object raw = userInput.get(getVariableNameForSubmissionProcessing());
        String json = null;
        VariableFormat format = FormatCommons.create(variableDefinition);
        if (!(raw instanceof String[])) {
            json = (String) raw;
        } else {
            json = Arrays.toString((String[]) raw);
        }
        if (list == null) {
            try {
                result.put(variableDefinition.getName(), format.parse(json));
            } catch (ClassCastException e) {
                log.error(String.format("%s", e));
                throw e;
            }
            return result;
        }
        try {
            List<UserTypeMap> selected = Lists.newArrayList();
            JSONArray input = (JSONArray) JSONValue.parse(json);
            for (Object o : input) {
                for (UserTypeMap userTypeMap : list) {
                    if (!compareByValue(userTypeMap, (JSONObject) o)) {
                        continue;
                    }
                    if (!selected.contains(userTypeMap)) {
                        selected.add(userTypeMap);
                    }
                    break;
                }
            }
            result.put(variableDefinition.getName(), selected);
        } catch (Exception e) {
            log.error(String.format("%s", e));
            throw e;
        }
        return result;
    }

    private final boolean compareByValue(UserTypeMap whoMap, JSONObject with) {
        boolean result = true;
        UserType type = whoMap.getUserType();
        for (Object keyName : with.keySet()) {
            VariableDefinition def = type.getAttribute((String) keyName);
            VariableFormat format = FormatCommons.create(def);
            String toCompare = null;
            if (format instanceof FileFormat) {
                IFileVariable file = (IFileVariable) whoMap.get(keyName);
                if (file != null) {
                    toCompare = file.getName();
                }
            } else {
                toCompare = format.format(whoMap.get(keyName));
            }
            if (toCompare == null) {
                toCompare = "";
            }
            if (format instanceof TextFormat) {
                String[] words1 = toCompare.split("\\s+");
                String[] words2 = ((String) with.get(keyName)).split("\\s+");
                if (Arrays.equals(words1, words2)) {
                    continue;
                }
            } else if (toCompare.equals(with.get(keyName))) {
                continue;
            }
            result = false;
            break;
        }
        return result;
    }
}
