package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showTasksHistoryLink")
public class ShowTasksHistoryLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getLinkText() {
        return MessagesProcesses.LABEL_SHOW_TASKS_HISTORY.message(pageContext);
    }
}
