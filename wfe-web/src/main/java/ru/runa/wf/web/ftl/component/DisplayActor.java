package ru.runa.wf.web.ftl.component;

import freemarker.template.TemplateModelException;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/**
 * @deprecated code moved to {@link InputVariable}.
 * 
 * @author dofs
 * @since 4.0
 */
@Deprecated
@CommonsLog
public class DisplayActor extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        String actorVarName = getParameterAsString(0);
        Object actorIdentity = variableProvider.getValue(actorVarName);
        if (actorIdentity == null) {
            log.error("No variable " + actorVarName + " in " + variableProvider.getProcessId());
            return "<p style='color: red;'> --- </p>";
        }
        Executor executor = TypeConversionUtil.convertToExecutor(actorIdentity, new DelegateExecutorLoader(user));
        if (executor instanceof Group) {
            return "<p style='color: blue;'>" + executor.getName() + "</p>";
        }
        Actor actor = (Actor) executor;
        String view = getParameterAsString(1);
        if ("fullname".equals(view)) {
            return actor.getFullName();
        } else if ("shortname".equals(view)) {
            return actor.getName();
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }

}
