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

import java.util.Arrays;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.commons.bc.DurationEnum;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Created on 14.09.2005 TODO only dates supported now
 */
public class DurationFilterTDFormatter extends FilterTDFormatter {

    @Override
    public void formatTd(TD filterInputTd, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        int inputsCount = 2;
        String[] stringConditions = filterCriteria.getFilterTemplates();
        for (int j = 0; j < inputsCount * 2; j += 2) {
            filterInputTd.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
            if (j != 0) {
                filterInputTd.addElement(Entities.NBSP);
            }
            final Input input = new Input(Input.TEXT, TableViewSetupForm.FILTER_CRITERIA, stringConditions[j]);
            input.setStyle("width: 30px;");
            filterInputTd.addElement(input);
            filterInputTd.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
            final String selectedDuration = stringConditions[j + 1];
            Select select = new Select(TableViewSetupForm.FILTER_CRITERIA);
            DurationEnum[] filterValues = Arrays.copyOfRange(DurationEnum.values(), DurationEnum.minutes.ordinal(), DurationEnum.values().length);
            for (DurationEnum value : filterValues) {
                String name = value.name();
                select.addElement(HTMLUtils.createOption(name, Messages.getMessage(value.getMessageKey(), pageContext), name.equals(selectedDuration)));
            }
            filterInputTd.addElement(select);
            filterInputTd.addElement(Entities.NBSP);
        }
    }
}
