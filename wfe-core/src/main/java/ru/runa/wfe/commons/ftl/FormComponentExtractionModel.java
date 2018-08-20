package ru.runa.wfe.commons.ftl;

import java.util.HashMap;
import java.util.List;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.collect.Maps;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FormComponentExtractionModel extends SimpleHash {
    private static final long serialVersionUID = 1L;
    private static final String NO_OP_STRING = "noop";
    private static final NoOpModel NO_OP_MODEL = new NoOpModel();
    private final HashMap<String, FormComponentSubmissionHandler> submissionHandlers = Maps.newHashMap();
    private final HashMap<String, FormComponentSubmissionPostProcessor> submissionPostProcessors = Maps.newHashMap();
    private final VariableProvider variableProvider;
    private final User user;
    private final WebHelper webHelper;

    public FormComponentExtractionModel(VariableProvider variableProvider, User user, WebHelper webHelper) {
        this.variableProvider = variableProvider;
        this.user = user;
        this.webHelper = webHelper;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        final FormComponent component = FreemarkerConfiguration.getComponent(key);
        if (component != null) {
            if (component instanceof FormComponentSubmissionHandler) {
                return new TemplateMethodModelEx() {
                    @Override
                    public Object exec(List arguments) throws TemplateModelException {
                        component.init(user, webHelper, variableProvider, false);
                        component.setArguments(arguments);
                        submissionHandlers.put(component.getVariableNameForSubmissionProcessing(), (FormComponentSubmissionHandler) component);
                        return NO_OP_STRING;
                    }
                };
            }
            if (component instanceof FormComponentSubmissionPostProcessor) {
                return new TemplateMethodModelEx() {
                    @Override
                    public Object exec(List arguments) throws TemplateModelException {
                        component.init(user, webHelper, variableProvider, false);
                        component.setArguments(arguments);
                        submissionPostProcessors.put(component.getVariableNameForSubmissionProcessing(),
                                (FormComponentSubmissionPostProcessor) component);
                        return NO_OP_STRING;
                    }
                };
            }
        }
        return NO_OP_MODEL;
    }

    public HashMap<String, FormComponentSubmissionHandler> getSubmissionHandlers() {
        return submissionHandlers;
    }

    public HashMap<String, FormComponentSubmissionPostProcessor> getSubmissionPostProcessors() {
        return submissionPostProcessors;
    }

    private static class NoOpModel implements TemplateMethodModelEx {

        @Override
        public Object exec(List args) throws TemplateModelException {
            return NO_OP_STRING;
        }
    }
}
