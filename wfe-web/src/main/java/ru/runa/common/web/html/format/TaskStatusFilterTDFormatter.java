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

/**
 * 
 * @author estet90
 * @since 4.3.0
 * 
 */
public class TaskStatusFilterTDFormatter extends FilterTDFormatter {

	@Override
	public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
		String[] stringConditions = filterCriteria.getFilterTemplates();
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(new Input(Input.TEXT, TableViewSetupForm.FILTER_CRITERIA, stringConditions[0]));
        td.addElement(Entities.NBSP);
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        int selected = 0;
        Select select = new Select(TableViewSetupForm.FILTER_CRITERIA);
        select.addElement(HTMLUtils.createOption(0, Messages.getMessage("label.task_status.task_is_active", pageContext), 0 == selected));
        select.addElement(HTMLUtils.createOption(1, Messages.getMessage("label.task_status.task_has_assigned", pageContext), 1 == selected));
        select.addElement(HTMLUtils.createOption(2, Messages.getMessage("label.task_status.task_has_not_started", pageContext), 2 == selected));
        td.addElement(select);
        td.addElement(Entities.NBSP);
	}

}
