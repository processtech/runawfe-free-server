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
