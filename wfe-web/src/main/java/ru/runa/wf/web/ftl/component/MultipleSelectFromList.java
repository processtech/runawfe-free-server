package ru.runa.wf.web.ftl.component;

import java.util.List;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionPostProcessor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.UserTypeFormat;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class MultipleSelectFromList extends FormComponent implements FormComponentSubmissionPostProcessor {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        List<Object> list = getParameterVariableValue(List.class, 1, null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        List<Object> selectedValues = variableProvider.getValue(List.class, variableName);
        StringBuffer html = new StringBuffer();
        html.append("<span class=\"multipleSelectFromList\">");
        for (Object option : list) {
            String optionValue;
            String optionLabel;
            if (option instanceof ISelectable) {
                ISelectable selectable = (ISelectable) option;
                optionValue = selectable.getValue();
                optionLabel = selectable.getLabel();
            } else if (option instanceof Executor) {
                Executor executor = (Executor) option;
                optionValue = "ID" + executor.getId();
                optionLabel = executor.getLabel();
            } else if (option instanceof UserTypeMap) {
                UserTypeMap userTypeMap = (UserTypeMap) option;
                UserTypeFormat userTypeFormat = new UserTypeFormat(userTypeMap.getUserType());
                optionValue = userTypeFormat.formatJSON(userTypeMap);
                optionValue = optionValue.replaceAll("\"", "&quot;");
                WfVariable variable = ViewUtil.createVariable(variableName, userTypeMap.getUserType().getName(), userTypeFormat, userTypeMap);
                String hid = userTypeMap.getUserType().getAttributes().get(0) != null ? userTypeMap.getUserType().getAttributes().get(0).getName()
                        + " " + userTypeMap.get(userTypeMap.getUserType().getAttributes().get(0).getName()) : userTypeMap.getUserType().getName();
                optionLabel = variableName + " " + hid + "<br>"
                        + ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
            } else if (option instanceof IFileVariable) {
                FileFormat fileFormat = new FileFormat();
                IFileVariable file = (IFileVariable) option;
                optionValue = fileFormat.formatJSON(file);
                optionValue = optionValue.replaceAll("\"", "&quot;");
                WfVariable variable = ViewUtil.createVariable(file.getName(), variableName, fileFormat, file);
                optionLabel = variableName + ": " + file.getName() + "<br>"
                        + ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
            } else {
                optionValue = String.valueOf(option);
                optionLabel = String.valueOf(option);
            }
            String id = variableName + "_" + optionValue;
            html.append("<input id=\"").append(id).append("\"");
            html.append(" type=\"checkbox\" value=\"").append(optionValue).append("\"");
            html.append(" name=\"").append(variableName).append("\"");
            if (selectedValues != null && selectedValues.contains(option)) {
                html.append(" checked=\"true\"");
            }
            html.append("style=\"width: 30px;\">");
            html.append("<label for=\"").append(id).append("\">");
            html.append(optionLabel);
            html.append("</label><br>");
        }
        html.append("</span>");
        return html;
    }

    @Override
    public Object postProcessValue(Object source) {
        if (source instanceof List) {
            List<String> valuesList = (List<String>) source;
            List<ISelectable> list = getParameterVariableValueNotNull(List.class, 1);
            List<ISelectable> selectedOptions = Lists.newArrayListWithExpectedSize(valuesList.size());
            for (String selectedValue : valuesList) {
                for (ISelectable option : list) {
                    if (selectedValue.equals(option.getValue())) {
                        selectedOptions.add(option);
                        break;
                    }
                }
            }
            return selectedOptions;
        }
        return source;
    }

}
