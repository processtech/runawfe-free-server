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
package ru.runa.wf.web;

import javax.servlet.jsp.PageContext;

import ru.runa.common.WebResources;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.user.User;

/**
 * Created on 17.11.2004
 * 
 */
public class TaskFormBuilderFactory {

    /**
     * @return implementation of TaskFormBuilder defined in wf.web.property file
     */
    public static TaskFormBuilder createTaskFormBuilder(User user, PageContext pageContext, Interaction interaction) {
        String taskFormBuilderClassName = WebResources.getTaskFormBuilderClassName(interaction.getType());
        TaskFormBuilder taskFormBuilder = ClassLoaderUtil.instantiate(taskFormBuilderClassName);
        taskFormBuilder.setUser(user);
        taskFormBuilder.setPageContext(pageContext);
        taskFormBuilder.setInteraction(interaction);
        return taskFormBuilder;
    }

}
