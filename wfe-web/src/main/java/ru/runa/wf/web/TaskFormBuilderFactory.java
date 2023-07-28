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
