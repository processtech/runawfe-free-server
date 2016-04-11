package ru.runa.wf.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.VisibleTag;

/**
 * Created on 27.03.2015
 * 
 * @author artmikheev
 * @jsp.tag name = "taskFormDelegationButton" body-content = "JSP"
 */
public class TaskFormDelegationTag extends VisibleTag {
	private static final long serialVersionUID = 1L;
	
	// required
	private Long taskId;
	
	@Override
	protected ConcreteElement getEndElement() {
		Table table = new Table();
		table.setClass("box");
		TR row = new TR();
		TD col = new TD();
		col.setAlign("right");
		
		Button button = new Button();
		button.addElement(new StringElement(Messages.getMessage(Messages.BUTTON_DELEGATE_TASK, pageContext)));
		button.addAttribute("data-taskid", taskId.intValue());
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

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

}
