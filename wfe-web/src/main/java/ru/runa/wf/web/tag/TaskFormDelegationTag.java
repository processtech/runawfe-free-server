package ru.runa.wf.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.MessagesProcesses;

/**
 * Created on 27.03.2015
 * 
 * @author artmikheev
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "taskFormDelegationButton")
public class TaskFormDelegationTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private String tasksIds;

    @Override
    protected ConcreteElement getEndElement() {
        Table table = new Table();
        table.setClass("box");
        TR row = new TR();
        TD col = new TD();
        col.setAlign("right");

        Button button = new Button();
        button.addElement(new StringElement(MessagesProcesses.BUTTON_DELEGATE_TASK.message(pageContext)));
        button.addAttribute("data-taskid", taskId.intValue());
        button.addAttribute("data-tasksIds", tasksIds); // Add tasks IDs
        button.setOnClick("delegateTaskDialog(this)");

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
    
    public String getTasksIds() {
        return tasksIds;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setTasksIds(String tasksIds) {
        this.tasksIds = tasksIds;
    }
    
    
}
