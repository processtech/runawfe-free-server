package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.tag.IdLinkBaseTag;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showHistoryLink")
public class ShowHistoryLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 8986818529014107234L;

    @Override
    protected String getLinkText() {
        return MessagesOther.LABEL_SHOW_HISTORY.message(pageContext);
    }
}
