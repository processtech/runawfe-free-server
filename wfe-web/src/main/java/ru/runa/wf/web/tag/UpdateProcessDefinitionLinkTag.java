package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updateProcessDefinitionLink")
public class UpdateProcessDefinitionLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = 8907909173614656228L;

    @Override
    protected String getLinkText() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION.message(pageContext);
    }
}
