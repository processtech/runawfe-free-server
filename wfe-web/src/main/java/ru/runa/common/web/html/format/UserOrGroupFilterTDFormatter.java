package ru.runa.common.web.html.format;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
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
        int selected = 0;
        if (!stringConditions[1].isEmpty()) {
            selected = Integer.parseInt(stringConditions[1]);
        }
        Select select = new Select(TableViewSetupForm.FILTER_CRITERIA);
        Option falseOption = new Option();
        falseOption.setValue(0);
        falseOption.addElement(Messages.getMessage("label.no_include_group", pageContext));
        falseOption.setSelected(0 == selected);
        select.addElement(falseOption);
        Option trueOption = new Option();
        trueOption.setValue(1);
        trueOption.addElement(Messages.getMessage("label.include_group", pageContext));
        trueOption.setSelected(1 == selected);
        select.addElement(trueOption);
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }
}
