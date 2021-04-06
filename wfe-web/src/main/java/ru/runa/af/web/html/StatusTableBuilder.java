package ru.runa.af.web.html;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Table;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.form.UpdateStatusForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.user.Actor;

public class StatusTableBuilder {
    private final boolean enabled;
    private final PageContext pageContext;
    private final Actor actor;

    public StatusTableBuilder(Actor actor, boolean disabled, PageContext pageContext) {
        this.actor = actor;
        enabled = !disabled;
        this.pageContext = pageContext;
    }

    public Table build() {
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        table.addElement(HTMLUtils.createCheckboxRow(MessagesExecutor.ACTOR_IS_ACTIVE.message(pageContext),
                UpdateStatusForm.IS_ACTIVE_INPUT_NAME, actor.isActive(), enabled, false));
        return table;
    }
}
