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

import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Powered by Dofs
 */
public class StringFilterTDFormatter extends FilterTDFormatter {

    @Override
    public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        String[] stringConditions = filterCriteria.getFilterTemplates();
        Input filterInput = new Input(Input.TEXT, TableViewSetupForm.FILTER_CRITERIA, stringConditions[0]);
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(filterInput);
        td.addElement(Entities.NBSP);
    }
}
