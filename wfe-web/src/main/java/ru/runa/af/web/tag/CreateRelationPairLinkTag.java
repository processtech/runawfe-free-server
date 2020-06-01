/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web.tag;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.CreateRelationPairAction;
import ru.runa.af.web.form.RelationPairForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createRelationPairLink")
public class CreateRelationPairLinkTag extends LinkTag {
    private static final long serialVersionUID = 1L;
    private Long relationId;

    public Long getRelationId() {
        return relationId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    @Override
    protected String getHref() {
        return Commons.getActionUrl(CreateRelationPairAction.VIEW_PATH, RelationPairForm.RELATION_ID, relationId, pageContext, PortletUrlType.Action);
    }

    @Override
    protected String getLinkText() {
        return MessagesExecutor.LINK_CREATE_RELATION_PAIR.message(pageContext);
    }

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredObjectType.RELATION, getRelationId());
    }
}
