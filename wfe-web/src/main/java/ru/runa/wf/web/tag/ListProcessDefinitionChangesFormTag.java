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

import org.apache.ecs.html.*;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.*;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

import java.util.List;

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

        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);
        List<ProcessDefinitionChange> changes = Delegates.getDefinitionService().getChanges(getProcessDefinitionId());
        TR headerTR = new TR();
        table.addElement(headerTR);
        headerTR.addElement(new TH(Messages.getMessage(VERSION, pageContext)).setWidth("15%").setClass(Resources.CLASS_LIST_TABLE_TH));
        headerTR.addElement(new TH(Messages.getMessage(DATE, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
        headerTR.addElement(new TH(Messages.getMessage(AUTHOR, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
        headerTR.addElement(new TH(Messages.getMessage(COMMENT, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));

        long curVersion = 0;
        for (ProcessDefinitionChange change : changes){
            TR row = new TR();
            table.addElement(row);
            if (curVersion == change.getVersion()){
                row.addElement(new TD().setClass(Resources.CLASS_LIST_TABLE_TD));
            }else {
                row.addElement(new TD(change.getVersion().toString()).setClass(Resources.CLASS_LIST_TABLE_TD));
                curVersion = change.getVersion();
            }
            row.addElement(new TD(CalendarUtil.formatDateTime(change.getDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
            row.addElement(new TD(change.getAuthor()).setClass(Resources.CLASS_LIST_TABLE_TD));
            row.addElement(new TD(change.getComment()).setNoWrap(false).setClass(Resources.CLASS_LIST_TABLE_TD));
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