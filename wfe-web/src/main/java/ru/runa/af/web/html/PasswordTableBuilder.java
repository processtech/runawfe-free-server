package ru.runa.af.web.html;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.Table;

import ru.runa.af.web.form.UpdatePasswordForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;

public class PasswordTableBuilder {
    private final boolean enabled;
    private final PageContext pageContext;

    public PasswordTableBuilder(boolean disabled, PageContext pageContext) {
        enabled = !disabled;
        this.pageContext = pageContext;
    }

    public Table build() {
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        Input passwordInput = HTMLUtils.createInput(Input.PASSWORD, UpdatePasswordForm.PASSWORD_INPUT_NAME, "", enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesCommon.PASSWORD.message(pageContext), passwordInput));
        Input passwordConfirmInput = HTMLUtils.createInput(Input.PASSWORD, UpdatePasswordForm.PASSWORD_CONFIRM_INPUT_NAME, "", enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesCommon.PASSWORD_CONFIRM.message(pageContext), passwordConfirmInput));
        return table;
    }
}
