package ru.runa.wf.web.ftl.component;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

public class EditAndDisplayUserTypeList extends EditUserTypeList {
    private static final long serialVersionUID = 1L;
        
    @Override
    protected UserTypeListModel parseParameters() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        boolean allowToAddElements = getParameterAs(boolean.class, 1);
        boolean allowToDeleteElements = getParameterAs(boolean.class, 2);
        int firstReadOnlyIndex = 0;
        List<String> attributeNames = new ArrayList<>();

        String attributeString = getParameterAsString(3);
        if (!attributeString.isEmpty()) {
            attributeNames.addAll(Arrays.asList(attributeString.split(",")));
            firstReadOnlyIndex = attributeNames.size();
        }
        attributeString = getParameterAsString(4);
        if (!attributeString.isEmpty()) {
            for (String readOnlyAttribute : Arrays.asList(attributeString.split(","))) {
                if (!attributeNames.contains(readOnlyAttribute)) {
                    attributeNames.add(readOnlyAttribute);
                }
            }
        }

        return new EditAndDisplayUserTypeListModel(variable, attributeNames, allowToAddElements,
            allowToDeleteElements, firstReadOnlyIndex);
    }

    @Override
    protected List<String> getEditableAttributeNames() {
        return Arrays.asList(getParameterAsString(3).split(","));
    }

    public class EditAndDisplayUserTypeListModel extends UserTypeListModel {
        private final boolean allowToAddElements;
        private final boolean allowToDeleteElements;
        private final int firstReadOnlyIndex;

        public EditAndDisplayUserTypeListModel(
            WfVariable variable, List<String> attributeNames, boolean allowToAddElements,
            boolean allowToDeleteElements, int firstReadOnlyIndex) {
            super(variable, attributeNames, true);
            this.allowToAddElements = allowToAddElements;
            this.allowToDeleteElements = allowToDeleteElements;
            this.firstReadOnlyIndex = firstReadOnlyIndex;
        }

        public boolean isAllowToAddElements() {
            return allowToAddElements;
        }

        public boolean isAllowToDeleteElements() {
            return allowToDeleteElements;
        }

        @Override
        public String getValue(TemplateModel arg0, TemplateModel arg1, Number index) throws TemplateModelException {
            UserTypeMap userTypeMap = (UserTypeMap) BEANS_WRAPPER.unwrap(arg0);
            VariableDefinition attributeDefinition = (VariableDefinition) BEANS_WRAPPER.unwrap(arg1);

            if (attributeNames.indexOf(attributeDefinition.getName()) < firstReadOnlyIndex) {
                WfVariable variable = getAttributeVariable(userTypeMap, attributeDefinition, index);
                return ViewUtil.getComponentInput(user, webHelper, variable);
            }
            return super.getValue(arg0, arg1, index);
        }

        public String getTemplateValue(TemplateModel arg0) throws TemplateModelException {
            VariableDefinition definition = (VariableDefinition) BEANS_WRAPPER.unwrap(arg0);
            String suffix = VariableFormatContainer.COMPONENT_QUALIFIER_START + VariableFormatContainer.COMPONENT_QUALIFIER_END + "."
                    + definition.getName();
            WfVariable templateComponentVariable = ViewUtil.createComponentVariable(variable, suffix, definition.getFormatNotNull(), null);
            String componentHtml;
            if (attributeNames.indexOf(definition.getName()) < firstReadOnlyIndex) {
                componentHtml = ViewUtil.getComponentInput(user, webHelper, templateComponentVariable);
            } else {
                componentHtml = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), templateComponentVariable);
            }
            return componentHtml.replaceAll("\"", "'").replaceAll("\n", "").replace("[]", "{}");
        }

    }
}