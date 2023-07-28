package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.service.client.DelegateTaskVariableProvider;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.VariableProvider;

public class TaskDescriptionTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD(getValue(object, env));
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTask task = (WfTask) object;
        String description = task.getDescription();
        if (description != null && description.contains("${")) {
            VariableProvider variableProvider = new DelegateTaskVariableProvider(env.getUser(), task);
            FormHashModel model = new FormHashModel(env.getUser(), variableProvider, new StrutsWebHelper(env.getPageContext()));
            description = FreemarkerProcessor.process(description, model);
        }
        return description;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
