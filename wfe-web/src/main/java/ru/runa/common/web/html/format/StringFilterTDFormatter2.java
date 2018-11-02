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

import java.util.UUID;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.Entities;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Powered by Dofs
 */
public class StringFilterTDFormatter2 extends FilterTDFormatter {

    @Override
    public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        String[] stringConditions = filterCriteria.getFilterTemplates();
        Input filterInput = new Input(Input.TEXT, TableViewSetupForm.FILTER_CRITERIA, stringConditions[0]);
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(filterInput);
        String filterInputId = UUID.randomUUID().toString();
        filterInput.setID(filterInputId);
        IMG imgList = new IMG(Commons.getUrl(Resources.V_MORE_ICON, pageContext, PortletUrlType.Resource), 0);
        imgList.setClass("button-more");
        imgList.setTitle(Messages.getMessage("label.filter_criteria.list", pageContext));
        imgList.setOnClick("javascript:editListFilterCriteria(\"" + filterInputId + "\", \""
                + Messages.getMessage("label.filter_criteria.list", pageContext) + "\", \"" + MessagesCommon.BUTTON_SAVE.message(pageContext)
                + "\", \"" + MessagesCommon.BUTTON_CANCEL.message(pageContext) + "\");");
        td.addElement(imgList);
        IMG imgRange = new IMG(Commons.getUrl(Resources.H_MORE_ICON, pageContext, PortletUrlType.Resource), 0);
        imgRange.setClass("button-more");
        imgRange.setTitle(Messages.getMessage("label.filter_criteria.range", pageContext));
        imgRange.setOnClick("javascript:editRangeFilterCriteria(\"" + filterInputId + "\", \""
                + Messages.getMessage("label.filter_criteria.range", pageContext) + "\", \"" + MessagesCommon.BUTTON_SAVE.message(pageContext)
                + "\", \"" + MessagesCommon.BUTTON_CANCEL.message(pageContext) + "\");");
        td.addElement(imgRange);
        td.addElement(Entities.NBSP);
    }
}
