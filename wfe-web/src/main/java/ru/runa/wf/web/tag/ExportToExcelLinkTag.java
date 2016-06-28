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
package ru.runa.wf.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Button;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * Class for displaying the "Export to Excel" button
 * 
 * @author Vladimir Shevtsov
 *
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "exportTaskListLink")
public class ExportToExcelLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getEndElement() {
        ConcreteElement concreteElement;
        try {
            if (isLinkEnabled()) {

                Button button = new Button();
                button.setClass(Resources.CLASS_BUTTON);
                button.addElement(new StringElement(getLinkText()));
                button.setOnClick("window.location.href='" + getHref() + "'");
                concreteElement = button;
            } else {
                concreteElement = new StringElement();
            }
        } catch (Exception e) {
            log.debug("link.isEnabled", e);
            concreteElement = new StringElement();
        }
        concreteElement.setClass(Resources.CLASS_BUTTON);

        Table table = new Table();
        table.setClass("box");
        TR row = new TR();
        TD col = new TD();
        col.setAlign("right");

        col.addElement(concreteElement);

        row.addElement(col);
        table.addElement(row);

        return table;
    }

    @Override
    protected String getHref() {
        return Commons.getActionUrl(WebResources.ACTION_MAPPING_EXPORT_TASK_LIST, pageContext, PortletUrlType.Render);
    }

    @Override
    protected String getLinkText() {
        return MessagesProcesses.BUTTON_EXPORT_EXCEL.message(pageContext);
    }

}
