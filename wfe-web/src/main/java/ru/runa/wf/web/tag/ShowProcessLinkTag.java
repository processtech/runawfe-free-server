package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "showProcessLink")
public class ShowProcessLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 3114678609093894112L;

    @Override
    protected String getHref() {
        return Commons.getActionUrl("/manage_process.do?" + IdForm.ID_INPUT_NAME + "=" + getIdentifiableId(), pageContext, PortletUrlType.Render);
    }

    @Override
    protected String getLinkText() {
        return MessagesProcesses.TITLE_PROCESS.message(pageContext);
    }
}
