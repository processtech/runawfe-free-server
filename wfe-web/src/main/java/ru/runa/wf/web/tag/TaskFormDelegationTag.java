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
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.ExecutorServiceDelegate;

/**
 * Created on 27.03.2015
 * 
 * @author artmikheev
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "taskFormDelegationButton")
public class TaskFormDelegationTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    private Long taskId;

    @Override
    protected ConcreteElement getEndElement() {
        Table table = new Table();
        table.setClass("box");
        TR row = new TR();
        TD col = new TD();
        col.setAlign("right");

        Button button = new Button();
        button.addAttribute("data-taskid", taskId.intValue());
        button.setOnClick("delegateTaskDialog(this)");

        String returnAction = (String) pageContext.getAttribute("returnAction", PageContext.REQUEST_SCOPE);
        if (returnAction == null) {
            returnAction = "/wfe/manage_tasks.do";
        }
        button.addAttribute("returnAction", returnAction);

        String tasksIds = (String) pageContext.getAttribute("tasksIds", PageContext.REQUEST_SCOPE);
        ExecutorService executorService = new ExecutorServiceDelegate();
        // taskId == -1 below - means multiple tasks delegation
        if (tasksIds != null && taskId == -1L && executorService.isAdministrator(Commons.getUser(pageContext.getSession()))) {
            button.addAttribute("data-tasksIds", tasksIds);
            button.addElement(new StringElement(MessagesProcesses.BUTTON_DELEGATE_TASKS.message(pageContext)));
        } else if (taskId != -1L) {
            button.addElement(new StringElement(MessagesProcesses.BUTTON_DELEGATE_TASK.message(pageContext)));
        } else {
            return table; // Empty table - means no vizible button in this case
        }

        col.addElement(button);
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
