package ru.runa.report.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.LinkTag;
import ru.runa.report.web.MessagesReport;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deployReportLink")
public class DeployReportLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredSingleton.REPORTS);
    }

    @Override
    protected String getLinkText() {
        return MessagesReport.BUTTON_DEPLOY_REPORT.message(pageContext);
    }

}
