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
package ru.runa.wf.web.tag;

import java.util.List;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "showSystemLogForm")
public class ShowSystemLogFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getBatchPresentation();
        int instanceCount = Delegates.getAuditService().getSystemLogsCount(getUser(), batchPresentation);
        // we must call getSystemLogs before obtaining current page number since
        // it can be changed after getSystemLogs call
        List<SystemLog> instances = Delegates.getAuditService().getSystemLogs(getUser(), batchPresentation);
        // batchPresentation must be recalculated since the current page number
        // might changed
        batchPresentation = getBatchPresentation();
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, instanceCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);

        TDBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, null);

        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 0, 0, getReturnAction(), pageContext);
        RowBuilder rowBuilder = new ReflectionRowBuilder(instances, batchPresentation, pageContext, null, getReturnAction(), "id", builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));

        navigation.addPagingNavigationTable(tdFormElement);
    }

    protected Permission getPermission() {
        return SystemPermission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesOther.TITLE_SYSTEM_HISTORY.message(pageContext);
    }

    @Override
    public String getAction() {
        return "";
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }
}
