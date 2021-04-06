package ru.runa.wf.web.ftl.component;

import freemarker.template.TemplateModelException;
import java.util.List;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

public class GroupMembers extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        String variableName = getParameterAsString(0);
        Object groupIdentity = getRichComboParameterAs(Object.class, 1);
        Group group = TypeConversionUtil.convertToExecutor(groupIdentity, new DelegateExecutorLoader(user));
        String view = getParameterAsString(2);
        Boolean useCurrentUserAsDefault = getParameterAs(Boolean.class, 3);
        if (useCurrentUserAsDefault == null) {
            useCurrentUserAsDefault = Boolean.FALSE;
        }
        List<Actor> actors = Delegates.getExecutorService().getGroupActors(user, group);
        if ("all".equals(view)) {
            Actor actor = variableProvider.getValue(Actor.class, variableName);
            if (actor == null && useCurrentUserAsDefault) {
                actor = user.getActor();
            }
            return ViewUtil.createExecutorSelect(variableName, actors, actor, true, true);
        } else if ("raw".equals(view)) {
            return actors;
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }

}
