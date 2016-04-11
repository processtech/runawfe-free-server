package ru.runa.wf.web.tag;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.IdLinkBaseTag;

/**
 * @jsp.tag name = "showTasksHistoryLink" body-content = "empty"
 */
public class ShowTasksHistoryLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.LABEL_SHOW_TASKS_HISTORY, pageContext);
    }
}
