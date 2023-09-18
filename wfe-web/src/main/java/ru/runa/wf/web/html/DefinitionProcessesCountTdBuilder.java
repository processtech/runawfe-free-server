package ru.runa.wf.web.html;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;

public class DefinitionProcessesCountTdBuilder extends BaseTdBuilder {

    public DefinitionProcessesCountTdBuilder() {
        super(null);
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        A loadProcessesCountLink = new A("javascript: void(0)",
                Messages.getMessage("label.definition_history.show_processes_count", env.getPageContext()));
        loadProcessesCountLink.setID("definition-processes-link-" + definition.getVersion());
        loadProcessesCountLink.setOnClick("getProcessesCount('" + definition.getName() + "'," + definition.getVersion() + ")");
        TD td = new TD().addElement(loadProcessesCountLink);
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return null;
    }
}
