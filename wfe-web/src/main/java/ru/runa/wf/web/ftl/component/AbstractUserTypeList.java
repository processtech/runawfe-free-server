package ru.runa.wf.web.ftl.component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.UserTypeFormat;

import com.google.common.base.Preconditions;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public abstract class AbstractUserTypeList extends FormComponent {
    private static final long serialVersionUID = 1L;
    protected static final BeansWrapper BEANS_WRAPPER;
    static {
        BeansWrapperBuilder builder = new BeansWrapperBuilder(Configuration.VERSION_2_3_23);
        builder.setUseModelCache(true);
        builder.setExposeFields(true);
        BEANS_WRAPPER = builder.build();
    }

    @Override
    protected Object renderRequest() throws Exception {
        UserTypeListModel model = parseParameters();
        String template = ClassLoaderUtil.getAsString("templates/" + getClass().getSimpleName() + ".ftl", getClass());
        SimpleHash map = new SimpleHash(BEANS_WRAPPER);
        map.put("model", model);
        return FreemarkerProcessor.process(template, map);
    }

    protected abstract UserTypeListModel parseParameters();

    public class UserTypeListModel {
        protected final WfVariable variable;
        protected final UserType userType;
        protected final List<String> attributeNames;
        protected final boolean componentView;

        public UserTypeListModel(WfVariable variable, List<String> attributeNames, boolean componentView) {
            this.variable = variable;
            this.userType = ((UserTypeFormat) FormatCommons.createComponent(variable, 0)).getUserType();
            this.attributeNames = attributeNames;
            this.componentView = componentView;
        }

        public WfVariable getVariable() {
            return variable;
        }

        public List<UserTypeMap> getVariableValue() {
            List<UserTypeMap> list = (List<UserTypeMap>) getVariable().getValue();
            if (list == null) {
                list = new ArrayList<>();
            }
            return list;
        }

        public String getUniqueName() {
            return variable.getDefinition().getScriptingNameWithoutDots();
        }

        public List<VariableDefinition> getAttributes() {
            final LinkedList<VariableDefinition> attributes = new LinkedList<VariableDefinition>();
            if (attributeNames.isEmpty()) {
                attributes.addAll(userType.getAttributes());
            } else {
                for (final String field : attributeNames) {
                    VariableDefinition expandedDefinition = userType.getAttributeExpanded(field);
                    Preconditions.checkNotNull(expandedDefinition, field);
                    attributes.add(expandedDefinition);
                }
            }
            return attributes;
        }

        public String getValue(TemplateModel arg0, TemplateModel arg1, Number index) throws TemplateModelException {
            UserTypeMap userTypeMap = (UserTypeMap) BEANS_WRAPPER.unwrap(arg0);
            VariableDefinition attributeDefinition = (VariableDefinition) BEANS_WRAPPER.unwrap(arg1);
            WfVariable variable = getAttributeVariable(userTypeMap, attributeDefinition, index);
            if (componentView) {
                return ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
            } else {
                return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
            }
        }

        protected WfVariable getAttributeVariable(UserTypeMap userTypeMap, VariableDefinition attributeDefinition, Number index) {
            WfVariable attributeVariable = userTypeMap.getAttributeValue(attributeDefinition.getName());
            return ViewUtil.createUserTypeListComponentVariable(variable, index.intValue(), attributeVariable);
        }
    }
}
