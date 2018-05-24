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
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listProcessDefinitionChangesForm")
public class ListProcessDefinitionChangesFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 7128850164438509265L;

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        final String VERSION = "process_definition_changes.version";
        final String DATE = "process_definition_changes.date";
        final String AUTHOR = "process_definition_changes.author";
        final String COMMENT = "process_definition_changes.comment";
        List<ProcessDefinitionChange> changes = Delegates.getDefinitionService().getLastChanges(getIdentifiableId(), 5L);
        if (!changes.isEmpty()) {
            Table table = new Table();
            tdFormElement.addElement(table);
            table.setClass(Resources.CLASS_LIST_TABLE);
            table.setID("processDefinitionChanges");
            TR headerTR = new TR();
            table.addElement(headerTR);
            headerTR.addElement(new TH(Messages.getMessage(VERSION, pageContext)).setWidth("15%").setClass(Resources.CLASS_LIST_TABLE_TH));
            headerTR.addElement(new TH(Messages.getMessage(DATE, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
            headerTR.addElement(new TH(Messages.getMessage(AUTHOR, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
            headerTR.addElement(new TH(Messages.getMessage(COMMENT, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));

            long currentVersion = 0;
            for (int i = changes.size() - 1; i >= 0; i--) {
                ProcessDefinitionChange change = changes.get(i);
                if (change.getVersion() <= Delegates.getDefinitionService().getProcessDefinition(getUser(), getIdentifiableId()).getVersion()
                        && !change.getComment().isEmpty()) {
                    TR row = new TR();
                    table.addElement(row);
                    TD versionTD = new TD();
                    versionTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    if (currentVersion == change.getVersion()) {
                        versionTD.setStyle("border-top-style:hidden;");
                    } else {
                        versionTD.setTagText(change.getVersion().toString());
                    }
                    row.addElement(versionTD);

                    TD dateTimeTD = new TD(CalendarUtil.formatDateTime(change.getDate()));
                    dateTimeTD.setClass(Resources.CLASS_LIST_TABLE_TD);

                    TD authorTD = new TD(change.getAuthor());
                    authorTD.setClass(Resources.CLASS_LIST_TABLE_TD);

                    TD commentTD = new TD(change.getComment());
                    commentTD.setClass(Resources.CLASS_LIST_TABLE_TD);

                    row.addElement(dateTimeTD);
                    row.addElement(authorTD);
                    row.addElement(commentTD);
                    currentVersion = change.getVersion();
                }
            }

            A link = new A();
            link.setHref("/wfe/processDefinitionChanges?action=loadAllChanges&id=" + getIdentifiableId());
            link.setID("showAllProcessDefinitionChanges");
            link.addElement(Messages.getMessage("process_definition_changes.showAllChanges", pageContext));
            tdFormElement.addElement(link);
        }
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION_CHANGES.message(pageContext);
    }
}