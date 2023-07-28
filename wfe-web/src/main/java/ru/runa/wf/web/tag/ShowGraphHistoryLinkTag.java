package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showGraphHistoryLink")
public class ShowGraphHistoryLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 8986818529014107234L;

    @Override
    protected String getLinkText() {
        return MessagesProcesses.LABEL_SHOW_GRAPH_HISTORY.message(pageContext);
    }
}
