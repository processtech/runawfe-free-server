package ru.runa.af.web.tag;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "addSubstitutionLink")
public class AddSubstitutionLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;
    private String text;

    public String getText() {
        return text;
    }

    @Attribute(required = true)
    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredObjectType.EXECUTOR, getIdentifiableId());
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(text, pageContext);
    }
}
