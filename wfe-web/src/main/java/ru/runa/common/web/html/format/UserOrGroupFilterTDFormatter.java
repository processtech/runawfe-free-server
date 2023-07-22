package ru.runa.common.web.html.format;

import com.google.common.base.Strings;
import java.util.UUID;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;

public class UserOrGroupFilterTDFormatter extends FilterTDFormatter {

    @Override
    public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        String[] stringConditions = filterCriteria.getFilterTemplates();
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        Input nameInput = new Input(Input.HIDDEN, TableViewSetupForm.FILTER_CRITERIA, stringConditions[0]);
        String nameInputId = UUID.randomUUID().toString();
        nameInput.setID(nameInputId);
        Input labelInput = new Input(Input.TEXT);
        labelInput.setReadOnly(true);
        String labelInputId = UUID.randomUUID().toString();
        labelInput.setID(labelInputId);
        if (!Strings.isNullOrEmpty(stringConditions[0])) {
            Actor actor = Delegates.getExecutorService().getExecutorByName(Commons.getUser(pageContext.getSession()), stringConditions[0]);
            labelInput.setValue(actor.getLabel());
        }
        td.addElement(nameInput);
        td.addElement(labelInput);
        td.addElement(Entities.NBSP);
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        int selected = 0;
        if (!stringConditions[1].isEmpty()) {
            selected = Integer.parseInt(stringConditions[1]);
        }
        Select select = new Select(TableViewSetupForm.FILTER_CRITERIA);
        select.addElement(HTMLUtils.createOption(0, Messages.getMessage("label.not_including_groups", pageContext), 0 == selected));
        select.addElement(HTMLUtils.createOption(1, Messages.getMessage("label.including_groups", pageContext), 1 == selected));
        td.addElement(select);
        td.addElement(Entities.NBSP);
        A selectLink = new A("javascript:void(0);");
        selectLink.addElement(MessagesExecutor.LABEL_SELECT.message(pageContext));
        selectLink.setOnClick("javascript:selectUser('" + nameInputId + "','" + labelInputId + "', \""
                + MessagesCommon.BUTTON_CANCEL.message(pageContext) + "\");");
        td.addElement(selectLink);
    }
}
