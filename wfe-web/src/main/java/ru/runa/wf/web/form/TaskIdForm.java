package ru.runa.wf.web.form;

import ru.runa.common.web.form.IdNameForm;

/**
 * Created on 12.05.2006
 * 
 * @struts:form name = "taskIdForm"
 */
public class TaskIdForm extends IdNameForm {

    private static final long serialVersionUID = 23542935792835791L;

    public static final String TASK_ID_INPUT_NAME = "taskId";
    public static final String SELECTED_TASK_PROCESS_ID_NAME = "selectedTaskProcessId";

    private Long taskId;
    private Long childProcessId;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getChildProcessId() {
        return childProcessId;
    }

    public void setChildProcessId(Long childProcessId) {
        this.childProcessId = childProcessId;
    }
}
