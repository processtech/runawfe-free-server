package ru.runa.common.web.tag;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.PermissionWebUtils;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "managePermissionsLink")
public class ManagePermissionsLinkTag extends BaseLinkTag {

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
        if (securedObjectType == SecuredObjectType.DEFINITION) {
            // ********************************************************************************************************************************
            // ***** !!!!! DON'T MERGE THIS INTO develop !!!!! This is temporary solution, before table BPM_PROCESS_DEFINITION_VER was created.
            // ********************************************************************************************************************************
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getUser(), identifiableId);
            return Delegates.getAuthorizationService().isAllowed(
                    getUser(), Permission.READ_PERMISSIONS, securedObjectType, definition.getIdentifiableId()
            );
        }
        return Delegates.getAuthorizationService().isAllowed(
                getUser(), Permission.READ_PERMISSIONS, securedObjectType, identifiableId != null ? identifiableId : 0
        );
    }
    
    @Override
    protected boolean isVisible() {
        return isLinkEnabled();
    }    

    @Override
    protected String getLinkText() {
        return MessagesCommon.TITLE_PERMISSION_OWNERS.message(pageContext);
    }

    @Override
    protected String getHref() {
        return Commons.getActionUrl("/managePermissionsForm", PermissionWebUtils.getLinkHRefParams(pageContext, securedObjectType, identifiableId),
                pageContext, PortletUrlType.Action);
    }
}
