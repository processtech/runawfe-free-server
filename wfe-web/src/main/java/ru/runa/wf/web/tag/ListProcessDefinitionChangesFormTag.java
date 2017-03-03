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
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listProcessDefinitionChangesForm")
public class ListProcessDefinitionChangesFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 7128850164438509265L;

    private Long processDefinitionId;

    @Attribute(required = true, rtexprvalue = true)
    public void setProcessDefinitionId(Long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    protected boolean isVisible() {
        return true;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        final String VERSION = "process_definition_changes.version";
        final String DATE = "process_definition_changes.date";
        final String AUTHOR = "process_definition_changes.author";
        final String COMMENT = "process_definition_changes.comment";

        List<ProcessDefinitionChange> changes = Delegates.getDefinitionService().getLastChanges(getProcessDefinitionId(), new Long(10));
        if (!changes.isEmpty()) {
            Table table = new Table();
            tdFormElement.addElement(table);
            table.setClass(Resources.CLASS_LIST_TABLE);
            table.setID("versionChangesTable");
            table.setStyle("border-style : hidden;");
            TR headerTR = new TR();
            table.addElement(headerTR);
            headerTR.setStyle("border-style: solid; border-width : 1px;");
            headerTR.addElement(new TH(Messages.getMessage(VERSION, pageContext)).setWidth("15%").setClass(Resources.CLASS_LIST_TABLE_TH));
            headerTR.addElement(new TH(Messages.getMessage(DATE, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
            headerTR.addElement(new TH(Messages.getMessage(AUTHOR, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
            headerTR.addElement(new TH(Messages.getMessage(COMMENT, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));

            long currentVersion = 0;
            long rowCount = 0;
            for (int i = changes.size() - 1; i >= 0; i--) {
                ProcessDefinitionChange change = changes.get(i);
                if (change.getVersion() <= Delegates.getDefinitionService().getProcessDefinition(getUser(), getProcessDefinitionId()).getVersion()
                        && change.getComment().isEmpty() != true) {
                    TR row = new TR();
                    table.addElement(row);
                    row.setStyle("border-top-style:hidden;" + "border-left-style:hidden;" + "border-right-style:hidden;");
                    TD versionTD = new TD();
                    versionTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    if (currentVersion == change.getVersion()) {
                        versionTD.setStyle("border-top-style:hidden;" + "border-left-style:hidden;" + "border-right-style:hidden;");
                    } else {
                        versionTD.setTagText(change.getVersion().toString());
                        row.setStyle(row.getAttribute("style") + "border-top-style: solid; border-width: 1px;");
                        versionTD.setStyle("border-top-style: solid; border-width: 1px;");

                    }
                    row.addElement(versionTD);

                    TD dateTimeTD = new TD(CalendarUtil.formatDateTime(change.getDate()));
                    dateTimeTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    dateTimeTD.setStyle("font-style : italic; " + "border-top-style:hidden;" + "border-left-style:hidden;"
                            + "border-right-style:hidden;");

                    TD authorTD = new TD(change.getAuthor());
                    authorTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    authorTD.setStyle("font-style : italic; " + "border-top-style:hidden;" + "border-left-style:hidden;"
                            + "border-right-style:hidden;");

                    TD commentTD = new TD(change.getComment());
                    commentTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                    commentTD.setStyle("border-top-style:hidden;" + "border-left-style:hidden;" + "border-right-style:hidden;");

                    if (currentVersion != change.getVersion()) {
                        dateTimeTD.setStyle(dateTimeTD.getAttribute("style") + "border-top-style:solid; border-width:1px;");
                        authorTD.setStyle(authorTD.getAttribute("style") + "border-top-style:solid; border-width:1px;");
                        commentTD.setStyle("border-top-style:solid; border-width:1px;");
                        currentVersion = change.getVersion();
                    }

                    row.addElement(dateTimeTD);
                    row.addElement(authorTD);
                    row.addElement(commentTD);

                    rowCount++;
                }
            }

            if (changes.size() > 10) {
                Table tableShowAllChanges = new Table();
                tdFormElement.addElement(tableShowAllChanges);
                TR tr = new TR();
                tableShowAllChanges.addElement(tr);
                A link = new A();
                link.setHref("#showAllChanges");
                link.setName("showAllChanges");
                String script = "jQuery.ajax({" +
                        "type: 'GET'," +
                        "url: '/wfe/versionChanges'," +
                        "data: {" +
                        "id: "+getProcessDefinitionId()+", " +
                        "untilVersion: "+Delegates.getDefinitionService().getProcessDefinition(getUser(), getProcessDefinitionId()).getVersion()+" , "+
                        "action: 'loadAllChanges', " +
                        "}," +
                        "dataType: 'html'," +
                        "success: function (data) { " +
                        "jQuery('#versionChangesTable').find('tr:gt(0)').remove();" +
                        "window.scrollTo(0, 0); " +
                        "jQuery('#versionChangesTable').append(data);" +
                        "window.scrollTo(0, 0);" +
                        "jQuery('a[name=showAllChanges]').closest('table').remove();" +
                        "window.scrollTo(0, 0);" +
                        "}" +
                        "});";
                link.setOnClick(script);
                link.addElement(Messages.getMessage("process_definition_changes.showAllChanges", pageContext));
                TD showAllChangesTd = new TD().addElement(link);
                tr.addElement(showAllChangesTd);
            }
        }
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION_CHANGES.message(pageContext);
    }
}