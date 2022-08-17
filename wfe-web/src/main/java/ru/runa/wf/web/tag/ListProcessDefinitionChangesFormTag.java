package ru.runa.wf.web.tag;

import java.util.Collections;
import java.util.List;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

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

        User user = Commons.getUser(pageContext.getSession());
        WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(user, getIdentifiableId());
        long definitionVersion = definition.getVersion();
        List<ProcessDefinitionChange> changes = Delegates.getDefinitionService().findChanges(definition.getName(), definitionVersion,
                definitionVersion);
        if (definitionVersion > 1 || !changes.isEmpty()) {
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
            Collections.reverse(changes);
            boolean first = true;
            for (ProcessDefinitionChange change : changes) {
                TR row = new TR();
                TD versionTD = new TD();
                versionTD.setClass(Resources.CLASS_LIST_TABLE_TD);
                if (first) {
                    versionTD.setTagText(change.getVersion().toString());
                    first = false;
                } else {
                    versionTD.setStyle("border-top-style:hidden;");
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
                table.addElement(row);
            }
            table.addAttribute("lastLoadedVersion", definitionVersion);
            if (definitionVersion != 1) {
                A link = new A();
                link.setHref("/wfe/processDefinitionChanges?action=load&id=" + getIdentifiableId());
                link.setID("showChangesFromNext5Versions");
                link.addElement(Messages.getMessage("process_definition_changes.showFromNext5Versions", pageContext));
                tdFormElement.addElement(link);
            }
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