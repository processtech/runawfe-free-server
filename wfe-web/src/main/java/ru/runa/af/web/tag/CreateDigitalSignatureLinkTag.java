package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Link to creating digital signature list form
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createDigitalSignatureLink")
public class CreateDigitalSignatureLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() {
        return (getUser().getActor().getId().equals(getIdentifiableId())
                || Delegates.getAuthorizationService().isAllowed(getUser(), Permission.CREATE_DIGITAL_SIGNATURE,
                SecuredObjectType.DIGITAL_SIGNATURE, getIdentifiableId())) &&
                !Delegates.getDigitalSignatureService().isDigitalSignatureExist(getUser(), getIdentifiableId());
    }

    @Override
    protected String getLinkText() {
        return MessagesProcesses.LABEL_CREATE.message(pageContext);
    }
}
