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
package ru.runa.wf.web.html;

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import com.google.common.base.Strings;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wfe.execution.dto.WfSwimlane;

public class ProcessSwimlaneRowBuilder implements RowBuilder {
    private final PageContext pageContext;
    private final List<WfSwimlane> swimlanes;
    private int currentIndex = 0;

    public ProcessSwimlaneRowBuilder(List<WfSwimlane> swimlanes, PageContext pageContext) {
        this.swimlanes = swimlanes;
        this.pageContext = pageContext;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < swimlanes.size();
    }

    @Override
    public TR buildNext() {
        TR tr = new TR();
        WfSwimlane swimlane = swimlanes.get(currentIndex++);

        TD nameTD = new TD(swimlane.getDefinition().getName());
        tr.addElement(nameTD);
        nameTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD globalTD = new TD(swimlane.getDefinition().isGlobal() ? /*√*/"&#x221A;" : "");
        tr.addElement(globalTD);
        globalTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        TD assignedToActorTD = new TD();
        tr.addElement(assignedToActorTD);
        assignedToActorTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        assignedToActorTD.addElement(HTMLUtils.createExecutorElement(pageContext, swimlane.getExecutor()));

        TD organizationFunctionTD = new TD();
        tr.addElement(organizationFunctionTD);
        organizationFunctionTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        String swimlaneInitializer = swimlane.getDefinition().getOrgFunctionLabel();
        if (Strings.isNullOrEmpty(swimlaneInitializer)) {
            swimlaneInitializer = MessagesOther.LABEL_UNSET_EMPTY_VALUE.message(pageContext);
        }
        organizationFunctionTD.addElement(swimlaneInitializer);
        return tr;
    }

    public int getEnabledRowsCount() {
        return swimlanes.size();
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
