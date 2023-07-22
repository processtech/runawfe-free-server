package ru.runa.common.web.tag;

import java.util.LinkedHashMap;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.PermissionWebUtils;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "grantPermissionsLink")
public class GrantPermissionsLinkTag extends BaseLinkTag {
    private static final long serialVersionUID = 1L;

    private SecuredObjectType securedObjectType;
    private Long identifiableId;

    @Attribute(required = true)
    public void setSecuredObjectType(String type) {
        this.securedObjectType = SecuredObjectType.valueOf(type);
    }

    @Attribute
    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(
                getUser(), Permission.UPDATE_PERMISSIONS, securedObjectType, identifiableId != null ? identifiableId : 0
        );
    }

    @Override
    protected String getLinkText() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

    @Override
    protected String getHref() {
        return Commons.getActionUrl("/grantPermissionsForm", PermissionWebUtils.getLinkHRefParams(pageContext, securedObjectType, identifiableId),
                pageContext, PortletUrlType.Action);
    }
}
