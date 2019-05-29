package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wfe.commons.web.PortletUrlType;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "addSubstitutionCriteriaLink")
public class AddSubstitutionCriteriaLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = 1L;

    private static final String HREF = "/createSubstitutionCriteria.do";

    @Override
    protected boolean isLinkEnabled() {
        return true;
    }

    @Override
    protected String getHref() {
        return Commons.getActionUrl(HREF, pageContext, PortletUrlType.Action);
    }

    @Override
    protected String getLinkText() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }
}
