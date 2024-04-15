package ru.runa.wfe.rest.converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.mapstruct.factory.Mappers;
import ru.runa.wfe.rest.dto.WfeFileVariable;
import ru.runa.wfe.rest.dto.WfeVariable;
import ru.runa.wfe.rest.dto.WfeVariableType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.UserTypeFormat;

public class VariableValueWrapper {

    public void process(VariableDefinition variableDefinition, WfeVariable variable) {
        if (variable == null) {
            return;
        }
        Object wrapped = variableDefinition.getFormatNotNull().processBy(new WrapVariableFormatVisitor(),
                new WfVariable(variableDefinition, variable.getValue()));
        variable.setValue(wrapped);
    }

    private static class WrapVariableFormatVisitor extends BaseWfVariableFormatVisitor {

        @Override
        public Object onExecutor(ExecutorFormat executorFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            return Mappers.getMapper(WfeExecutorMapper.class).map((Executor) variable.getValue());
        }

        @Override
        public Object onFile(FileFormat fileFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            FileVariable fileVariable = (FileVariable) variable.getValue();
            // data should be obtained by another api call
            return new WfeFileVariable(fileVariable.getName(), fileVariable.getContentType(), null, fileVariable.getStringValue());
        }

        @Override
        public List<Object> onList(List<Object> values, VariableDefinition definition) {
            List<Object> result = new ArrayList<>(values.size());
            for (Object value : values) {
                result.add(definition.getFormatNotNull().processBy(this, new WfVariable(definition, value)));
            }
            return result;
        }

        @Override
        public Map<String, Object> getCustomUserTypeFields(UserTypeFormat userTypeFormat) {
            return new LinkedHashMap<>();
        }

        @Override
        public Object onUserType(UserTypeFormat userTypeFormat, WfVariable variable) {
            if (variable.getValue() == null) {
                return null;
            }
            UserTypeMap value = (UserTypeMap) variable.getValue();
            String namePrefix = variable.getDefinition().getName() + UserType.DELIM;
            return userTypeFormat.getUserType().getAttributes().stream()
                    .map(attributeDefinition -> {
                        WfeVariable result = new WfeVariable();
                        result.setName(attributeDefinition.getName());
                        result.setType(WfeVariableType.findByJavaClass(attributeDefinition.getFormatNotNull().getJavaClass()));
                        result.setFormat(attributeDefinition.getFormatNotNull().getName());
                        Object attributeValue = value.get(attributeDefinition.getName());
                        VariableDefinition attributeVariable = new VariableDefinition(namePrefix + attributeDefinition.getName(), null, attributeDefinition);
                        result.setValue(attributeVariable.getFormatNotNull().processBy(this, new WfVariable(attributeVariable, attributeValue)));
                        return result;
                    })
                    .collect(Collectors.toList());
        }
    }
}
