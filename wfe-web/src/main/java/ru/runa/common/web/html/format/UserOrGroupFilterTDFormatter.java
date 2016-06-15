package ru.runa.common.web.html.format;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Messages;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.presentation.filter.FilterCriteria;

public class UserOrGroupFilterTDFormatter extends FilterTDFormatter {

    @Override
    public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        String[] stringConditions = filterCriteria.getFilterTemplates();
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(new Input(Input.TEXT, TableViewSetupForm.FILTER_CRITERIA, stringConditions[0]));
        td.addElement(Entities.NBSP);
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        final Input check = new Input(Input.CHECKBOX, TableViewSetupForm.FILTER_CRITERIA, "true");
        check.setChecked(!stringConditions[1].isEmpty());
        td.addElement(check);
        td.addElement(Messages.getMessage("label.include_group", pageContext));
        td.addElement(Entities.NBSP);
    }
}
