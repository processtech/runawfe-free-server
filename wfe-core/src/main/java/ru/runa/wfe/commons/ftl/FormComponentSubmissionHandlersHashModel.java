package ru.runa.wfe.commons.ftl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FormComponentSubmissionHandlersHashModel extends FormHashModel {
    private static final long serialVersionUID = 1L;
    private final HashMap<String, FormComponentSubmissionHandler> componentsSubmissionHandlers = new HashMap<String, FormComponentSubmissionHandler>();

    public FormComponentSubmissionHandlersHashModel(User user, IVariableProvider variableProvider, WebHelper webHelper) {
        super(user, variableProvider, webHelper);
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        final TemplateModel model = super.get(key);
        if (model instanceof FormComponentSubmissionHandler && model instanceof FormComponent) {
            return new TemplateMethodModelEx() {
                @Override
                public Object exec(List arguments) throws TemplateModelException {
                    FormComponent formComponent = (FormComponent) model;
                    Object result = formComponent.exec(arguments);
                    componentsSubmissionHandlers.put(formComponent.getVariableName(), (FormComponentSubmissionHandler) formComponent);
                    return result;
                }
            };
        }
        return model;
    }

    public Map<String, FormComponentSubmissionHandler> getComponentsSubmissionHandlers() {
        return componentsSubmissionHandlers;
    }
}
