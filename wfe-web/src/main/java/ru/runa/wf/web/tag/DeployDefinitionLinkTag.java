package ru.runa.wf.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployDefinitionLink")
public class DeployDefinitionLinkTag extends LinkTag {

    private static final long serialVersionUID = 984615575450934781L;

    @Override
    protected boolean isLinkEnabled() {
        return !WebResources.isBulkDeploymentElements()
                && Delegates.getAuthorizationService().isAllowed(getUser(), Permission.CREATE, SecuredSingleton.DEFINITIONS);
    }

    @Override
    protected String getLinkText() {
        return MessagesProcesses.BUTTON_DEPLOY_DEFINITION.message(pageContext);
    }

}
