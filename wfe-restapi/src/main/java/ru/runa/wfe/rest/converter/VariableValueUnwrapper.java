package ru.runa.wfe.rest.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.rest.dto.WfeFileVariable;
import ru.runa.wfe.rest.dto.WfeGroup;
import ru.runa.wfe.rest.dto.WfeUser;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;

public class VariableValueUnwrapper {
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> process(ParsedProcessDefinition processDefinition, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String variableName = entry.getKey();
            if (WfProcess.TRANSIENT_VARIABLES.equals(variableName) || WfProcess.SELECTED_TRANSITION_KEY.equals(variableName)) {
                result.put(variableName, entry.getValue());
                continue;
            }
            VariableDefinition variableDefinition = processDefinition.getVariableNotNull(variableName, true);
            Object unwrapped = variableDefinition.getFormatNotNull().processBy(new UnwrapVariableFormatVisitor(),
                    new WfVariable(variableDefinition, entry.getValue()));
            result.put(variableName, unwrapped);
        }
        return result;
    }

    private class UnwrapVariableFormatVisitor extends BaseWfVariableFormatVisitor {

        @Override
        public Object onDate(DateFormat dateFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            return new Date(((Number) variable.getValue()).longValue());
        }

        @Override
        public Object onTime(TimeFormat timeFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            return new Date(((Number) variable.getValue()).longValue());
        }

        @Override
        public Object onDateTime(DateTimeFormat dateTimeFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            return new Date(((Number) variable.getValue()).longValue());
        }

        @Override
        public Object onExecutor(ExecutorFormat executorFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            if (variable.getValue() instanceof String) {
                // allow strings for simplicity?
                return variable.getValue();
            }
            Map<String, Object> map = (Map<String, Object>) variable.getValue();
            if (WfeExecutor.Type.USER.name().equals(map.get("type"))) {
                return fromMap(WfeUser.class, map);
            } else {
                return fromMap(WfeGroup.class, map);
            }
        }

        @Override
        public Object onFile(FileFormat fileFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            return fromMap(WfeFileVariable.class, (Map<String, Object>) variable.getValue());
        }

        @Override
        public List<Object> onList(List<Object> values, VariableDefinition definition) {
            for (int i = 0; i < values.size(); i++) {
                values.set(i, definition.getFormatNotNull().processBy(this, new WfVariable(definition, values.get(i))));
            }
            return values;
        }

        @Override
        public Map<String, Object> getCustomUserTypeFields(UserTypeFormat userTypeFormat) {
            return new UserTypeMap(userTypeFormat.getUserType());
        }

        // implicit parameters parsed as map...
        @SneakyThrows
        private <T> T fromMap(Class<T> resultClass, Map<String, Object> map) {
            String json = mapper.writeValueAsString(map);
            return mapper.readValue(json, resultClass);
        }
    }

}
