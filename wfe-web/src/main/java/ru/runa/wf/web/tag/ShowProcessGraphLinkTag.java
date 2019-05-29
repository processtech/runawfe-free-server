package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showProcessGraphLink")
public class ShowProcessGraphLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getLinkText() {
        return MessagesProcesses.TITLE_PROCESS_GRAPH.message(pageContext);
    }
}
