package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createRootDigitalSignatureLink")
public class CreateRootDigitalSignatureLinkTag extends LinkTag {
    @Override
    protected boolean isLinkEnabled() {
        return  Delegates.getAuthorizationService().isAllowed(getUser(), Permission.CREATE_DIGITAL_SIGNATURE,
                SecuredObjectType.DIGITAL_SIGNATURE, getUser().getActor().getId()) &&
                !Delegates.getDigitalSignatureService().doesRootDigitalSignatureExist(getUser());
    }
    @Override
    protected String getLinkText() {
        return MessagesProcesses.LABEL_CREATE.message(pageContext);
    }
}
