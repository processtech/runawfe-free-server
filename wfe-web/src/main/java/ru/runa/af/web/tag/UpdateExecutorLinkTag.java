package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.tag.IdLinkBaseTag;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "updateExecutorLink")
public class UpdateExecutorLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = 8986818529014107234L;

    @Override
    protected String getLinkText() {
        return MessagesExecutor.TITLE_EXECUTOR_DETAILS.message(pageContext);
    }
}
