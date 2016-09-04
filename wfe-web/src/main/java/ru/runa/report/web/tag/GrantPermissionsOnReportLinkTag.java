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
package ru.runa.report.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wfe.report.dto.ReportDto;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "grantPermissionsOnReportLink")
public class GrantPermissionsOnReportLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() {
        ReportDto report = Delegates.getReportService().getReportDefinition(getUser(), getIdentifiableId());
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE_PERMISSIONS, report);
    }

    @Override
    protected String getLinkText() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

}
