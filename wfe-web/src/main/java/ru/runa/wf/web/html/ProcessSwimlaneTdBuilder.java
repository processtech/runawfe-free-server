package ru.runa.wf.web.html;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.ecs.html.TD;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ActorFormat;

public class ProcessSwimlaneTdBuilder implements TdBuilder {
    private final String swimlaneName;

    public ProcessSwimlaneTdBuilder(String swimlaneName) {
        this.swimlaneName = swimlaneName;
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getDisplayValue(object, env));
        return td;
    }

    // now used for excel export only
    @Override
    public String getValue(Object object, Env env) {
        List<Actor> actors = getActors(object, env);
        ActorFormat format = new ActorFormat();
        return actors.stream().map(a -> format.formatExcelCell(a)).collect(Collectors.joining(", "));
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getDisplayValue(object, env) };
    }
    
    private String getDisplayValue(Object object, Env env) {
        Long processId = ((WfProcess) object).getId();
        List<Actor> actors = getActors(object, env);
        ActorFormat format = new ActorFormat();
        WebHelper webHelper = new StrutsWebHelper(env.getPageContext());
        return actors.stream().map(a -> format.formatHtml(env.getUser(), webHelper, processId, null, a)).collect(Collectors.joining(", "));
    }

    private List<Actor> getActors(Object object, Env env) {
        WfVariable variable = ((WfProcess) object).getVariable(swimlaneName);
        if (variable == null || variable.getValue() == null) {
            return new ArrayList<>();
        }
        if (variable.getValue() instanceof Actor) {
            return Lists.newArrayList((Actor) variable.getValue());
        }
        Group group = (Group) variable.getValue();
        return Delegates.getExecutorService().getGroupActors(env.getUser(), group);
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

}
