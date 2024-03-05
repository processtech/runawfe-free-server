package ru.runa.wf.web.ftl.component;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionPostProcessor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.SelectableOption;

public class OptionSelectFromVariableList extends FormComponent implements FormComponentSubmissionPostProcessor {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        List<Object> list = getParameterVariableValue(List.class, 1, null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        Object selectedValue = variableProvider.getValue(Object.class, variableName);
        StringBuffer html = new StringBuffer();
        html.append("<select name=\"").append(variableName).append("\">");
        html.append("<option value=\"\"> ------------------------- </option>");
        for (Object option : list) {
            String optionValue;
            String optionLabel;
            if (option instanceof SelectableOption) {
                SelectableOption selectable = (SelectableOption) option;
                optionValue = selectable.getValue();
                optionLabel = selectable.getLabel();
            } else if (option instanceof Executor) {
                Executor executor = (Executor) option;
                optionValue = "ID" + executor.getId();
                optionLabel = executor.getFullName();
            } else {
                optionValue = String.valueOf(option);
                optionLabel = String.valueOf(option);
            }
            html.append("<option value=\"").append(optionValue).append("\"");
            if (Objects.equal(selectedValue, option)) {
                html.append(" selected=\"true\"");
            }
            html.append(">").append(optionLabel).append("</option>");
        }
        html.append("</select>");
        return html;
    }

    @Override
    public Object postProcessValue(Object source) {
        if (source instanceof String) {
            String value = (String) source;
            List<?> list = getParameterVariableValue(List.class, 1, null);
            if (TypeConversionUtil.getListFirstValueOrNull(list) instanceof SelectableOption) {
                for (SelectableOption option : (List<SelectableOption>) list) {
                    if (Objects.equal(value, option.getValue())) {
                        return option;
                    }
                }
            }
        }
        return source;
    }

}
