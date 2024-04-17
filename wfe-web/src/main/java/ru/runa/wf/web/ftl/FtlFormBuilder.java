package ru.runa.wf.web.ftl;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.VariableProvider;

public class FtlFormBuilder extends TaskFormBuilder {

    @Override
    protected String buildForm(VariableProvider variableProvider) {
        String template = new String(interaction.getFormData(), Charsets.UTF_8);
        return processFreemarkerTemplate(template, variableProvider, true);
    }

    protected String processFreemarkerTemplate(String template, VariableProvider variableProvider, boolean clearSession) {
        FormHashModel model = new FormHashModel(user, variableProvider, new StrutsWebHelper(pageContext));
        if (clearSession) {
            model.clearSession();
        }
        // #173
        model.put("context", new BeanModel(new Context(this), BeansWrapper.getDefaultInstance()));
        return FreemarkerProcessor.process(template, model);
    }

    public static class Context {
        private final FtlFormBuilder builder;

        public Context(FtlFormBuilder builder) {
            this.builder = builder;
        }

        public WfDefinition getDefinition() {
            return Delegates.getDefinitionService().getProcessDefinition(builder.user, builder.definitionId);
        }

        public WfProcess getProcess() {
            return Delegates.getExecutionService().getProcess(builder.user, getTask().getProcessId());
        }

        public WfTask getTask() {
            Preconditions.checkNotNull(builder.task, "Be sure you are not using this in start form");
            return builder.task;
        }

        public Interaction getInteraction() {
            return builder.interaction;
        }
    }
}
