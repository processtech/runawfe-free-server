package ru.runa.wf.web.tag;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.service.delegate.ExecutorServiceDelegate;

/**
 * Created on 27.03.2015
 * 
 * @author artmikheev
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "taskFormDelegationButton")
public class TaskFormDelegationTag extends VisibleTag {
    @Override
    protected boolean isVisible() {
        return Delegates.getAuthorizationService().isAllowedForAny(getUser(), Permission.DELEGATE_TASKS, SecuredObjectType.EXECUTOR); 
    }

    private static final long serialVersionUID = 1L;

    private Long taskId;

    @Override
    protected ConcreteElement getEndElement() {
        Table table = new Table();
        table.setClass("box");
        TR row = new TR();
        TD col = new TD();
        col.setAlign("right");
        String tasksIds = (String) pageContext.getAttribute("tasksIds", PageContext.REQUEST_SCOPE);
        ExecutorService executorService = new ExecutorServiceDelegate();
        // taskId == -1 below - means multiple tasks delegation
        if (taskId == -1L && tasksIds != null && executorService.isAdministrator(Commons.getUser(pageContext.getSession()))) {
            Button button = new Button();
            button.addAttribute("data-taskIds", tasksIds);
            button.addElement(new StringElement(MessagesProcesses.BUTTON_DELEGATE_TASKS.message(pageContext)));
            button.setOnClick("delegateTaskDialog(this)");
            col.addElement(button);
        } else if (taskId != -1L && !Delegates.getTaskService().getTask(getUser(), taskId).isReadOnly()) {
            Button button = new Button();
            button.addElement(new StringElement(MessagesProcesses.BUTTON_DELEGATE_TASK.message(pageContext)));
            button.addAttribute("data-taskId", taskId.intValue());
            button.setOnClick("delegateTaskDialog(this)");
            col.addElement(button);
        } else {
            return table; // Empty table - means no visible button in this case
        }
        row.addElement(col);
        table.addElement(row);
        return table;
    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }

    public Long getTaskId() {
        return taskId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
