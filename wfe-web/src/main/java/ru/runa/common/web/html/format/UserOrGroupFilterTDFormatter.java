package ru.runa.common.web.html.format;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;

import ru.runa.common.web.HTMLUtils;
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
        int selected = 0;
        if (!stringConditions[1].isEmpty()) {
            selected = Integer.parseInt(stringConditions[1]);
        }
        Select select = new Select(TableViewSetupForm.FILTER_CRITERIA);
        // false select element
        select.addElement(HTMLUtils.createOption(0, Messages.getMessage("label.not_including_groups", pageContext), 0 == selected));
        // true select element
        select.addElement(HTMLUtils.createOption(1, Messages.getMessage("label.including_groups", pageContext), 1 == selected));
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }
}
