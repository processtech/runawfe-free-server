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
package ru.runa.common.web.html.format;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;

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
            Option option = new Option();
            option.setValue(entry.getKey());
            option.addElement(Messages.getMessage(entry.getValue(), pageContext));
            option.setSelected(entry.getKey().equals(filterValue));
            select.addElement(option);
        }
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }
}
