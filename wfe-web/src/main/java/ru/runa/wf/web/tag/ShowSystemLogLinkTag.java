package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.tag.LinkTag;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showSystemLogLink")
public class ShowSystemLogLinkTag extends LinkTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getLinkText() {
        return MessagesOther.LABEL_SHOW_HISTORY.message(pageContext);
    }
}
