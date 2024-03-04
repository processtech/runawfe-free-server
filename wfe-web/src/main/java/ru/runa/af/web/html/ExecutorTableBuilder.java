package ru.runa.af.web.html;

import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/*
 * Created on 20.08.2004
 */
public class ExecutorTableBuilder {
    private final Executor executor;
    private final boolean enabled;
    private final PageContext pageContext;

    /**
     * Use for update
     * 
     * @param executor
     *            executor for update
     */
    public ExecutorTableBuilder(Executor executor, boolean areInputsDisabled, PageContext pageContext) {
        this.executor = executor;
        this.pageContext = pageContext;
        enabled = !areInputsDisabled;
    }

    /**
     * Use for create
     * 
     * @param isActor
     *            type of table
     */
    public ExecutorTableBuilder(boolean isActor, PageContext pageContext) {
        if (isActor) {
            executor = new Actor("", "", "", null, "", "", "", "");
        } else {
            executor = new Group("", "");
        }
        enabled = true;
        this.pageContext = pageContext;
    }

    public Table buildTable() {
        Actor actor = (Actor) (executor instanceof Actor ? executor : null);
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        Input nameInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.NEW_NAME_INPUT_NAME, executor.getName(), enabled, true);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.EXECUTOR_NAME.message(pageContext), nameInput));
        if (actor != null) {
            Input fullNameInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.FULL_NAME_INPUT_NAME, actor.getFullName(), enabled, false);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.ACTOR_FULL_NAME.message(pageContext), fullNameInput));
        }
        Input descriptionInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.DESCRIPTION_INPUT_NAME, executor.getDescription(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.EXECUTOR_DESCRIPTION.message(pageContext), descriptionInput));
        if (actor != null) {
            String code = actor.getCode() != null ? actor.getCode().toString() : "";
            Input codeInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.CODE_INPUT_NAME, code, enabled, false);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.ACTOR_CODE.message(pageContext), codeInput));

            Input emailInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.EMAIL_INPUT_NAME, actor.getEmail(), enabled, false);
            TD emailTableData = this.createEmailTableData(emailInput, actor);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.ACTOR_EMAIL.message(pageContext), emailTableData));

            Input phoneInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.PHONE_INPUT_NAME, actor.getPhone(), enabled, false);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.ACTOR_PHONE.message(pageContext), phoneInput));

            Input titleInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.TITLE_INPUT_NAME, actor.getTitle(), enabled, false);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.ACTOR_TITLE.message(pageContext), titleInput));

            Input departmentInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.DEPARTMENT_INPUT_NAME, actor.getDepartment(), enabled, false);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.ACTOR_DEPARTMENT.message(pageContext), departmentInput));
        } else {
            Group group = (Group) executor;
            Input adLdapGroupInput = HTMLUtils.createInput(UpdateExecutorDetailsForm.EMAIL_INPUT_NAME, group.getLdapGroupName(), enabled, false);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.GROUP_AD.message(pageContext), adLdapGroupInput));
        }
        return table;
    }

    private TD createEmailTableData(Input emailInput, Actor actor) {
        TD result = new TD();
        Div divForEmail = new Div();
        result.addElement(divForEmail);
        divForEmail.addElement(emailInput);
        Div divForCheckboxes = new Div();
        result.addElement(divForCheckboxes);
        Label sendMessagesAboutTasksLabel = new Label();
        divForCheckboxes.addElement(sendMessagesAboutTasksLabel);
        Input taskEmailNotificationsEnabledCheckbox = HTMLUtils.createCheckboxInput(UpdateExecutorDetailsForm.TASK_EMAIL_NOTIFICATIONS_ENABLED_INPUT_NAME,
                actor.getTaskEmailNotificationsEnabled(), enabled, false);
        sendMessagesAboutTasksLabel.addElement(taskEmailNotificationsEnabledCheckbox);
        sendMessagesAboutTasksLabel.addElement(MessagesExecutor.ACTOR_SEND_NOTIFICATIONS_ABOUT_TASKS.message(pageContext));
        Label sendMessagesAboutChatMessagesLabel = new Label();
        divForCheckboxes.addElement(sendMessagesAboutChatMessagesLabel);
        Input chatEmailNotificationsEnabledCheckbox = HTMLUtils.createCheckboxInput(
                UpdateExecutorDetailsForm.CHAT_EMAIL_NOTIFICATIONS_ENABLED_INPUT_NAME, actor.getChatEmailNotificationsEnabled(), enabled, false);
        sendMessagesAboutChatMessagesLabel.addElement(chatEmailNotificationsEnabledCheckbox);
        sendMessagesAboutChatMessagesLabel.addElement(MessagesExecutor.ACTOR_SEND_NOTIFICATIONS_ABOUT_CHAT_MESSAGES.message(pageContext));
        return result;
    }
}
