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

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Powered by Dofs
 */
public abstract class FilterTDFormatter {

    public TD format(PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex, boolean isFilterEnabled) {
        TD td = createFormattedTd(fieldIndex, isFilterEnabled);
        formatTd(td, pageContext, filterCriteria, fieldIndex);
        return td;
    }

    public abstract void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex);

    protected TD createFormattedTd(int fieldIndex, boolean isFilterEnabled) {
        Input selectFieldToFilterInput = new Input(Input.CHECKBOX, TableViewSetupForm.FILTER_CRITERIA_ID, fieldIndex);
        TD filterInputTd = new TD();
        filterInputTd.setClass(Resources.CLASS_VIEW_SETUP_TD);
        filterInputTd.addElement(selectFieldToFilterInput);
        filterInputTd.addElement(Entities.NBSP);
        if (isFilterEnabled) {
            selectFieldToFilterInput.setChecked(true);
        }
        return filterInputTd;
    }
}
