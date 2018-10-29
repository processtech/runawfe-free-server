package ru.runa.common.web.html.format;

import java.util.Map;

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
 * {@link FilterTDFormatter}, which creates option to select filter value from predefined {@link String}'s.
 */
public class StringEnumerationFilterTDFormatter extends FilterTDFormatter {

    /**
     * {@link Map} from enumerated value to property display name (struts property).
     */
    private final Map<String, String> enumerationValues;

    /**
     * Creates instance with specified allowed filter values.
     *
     * @param enumerationValues
     *            {@link Map} from enumerated value to property display name (struts property).
     */
    public StringEnumerationFilterTDFormatter(Map<String, String> enumerationValues) {
        super();
        this.enumerationValues = enumerationValues;
    }

    @Override
    public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        String filterValue = filterCriteria.getFilterTemplates()[0];
        Select select = new Select(TableViewSetupForm.FILTER_CRITERIA);
        for (Map.Entry<String, String> entry : enumerationValues.entrySet()) {
            String value = entry.getKey();
            select.addElement(HTMLUtils.createOption(value, Messages.getMessage(entry.getValue(), pageContext), value.equals(filterValue)));
        }
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }
}
