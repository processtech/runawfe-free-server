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

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

public class ProcessVariablesRowBuilder implements RowBuilder {
    private int index = 0;
    private final List<WfVariable> variables;
    private final PageContext pageContext;
    private final Long processId;
    private boolean massBool;

    public ProcessVariablesRowBuilder(Long processId, List<WfVariable> variables, PageContext pageContext) {
        this.variables = variables;
        this.processId = processId;
        this.pageContext = pageContext;
    }
    
    public void setMassBool(boolean massBool) {
    	this.massBool=massBool;
    }

    @Override
    public boolean hasNext() {
        return index < variables.size();
    }

    @Override
    public TR buildNext() {
        WfVariable variable = variables.get(index);
        Object value = variable.getValue();
        TR tr = new TR();
        TD nameTd = new TD(variable.getDefinition().getName());
        if (variable.getDefinition().isSynthetic()) {
            nameTd.setStyle("color: #aaaaaa;");
        }
        tr.addElement(nameTd.setClass(Resources.CLASS_LIST_TABLE_TD));
        String fl = variable.getDefinition() != null ? variable.getDefinition().getFormatLabel() : "-";
        tr.addElement(new TD(fl).setClass(Resources.CLASS_LIST_TABLE_TD));
        if (WebResources.isDisplayVariablesJavaType()) {
            String className = value != null ? value.getClass().getName() : "";
            tr.addElement(new TD(className).setClass(Resources.CLASS_LIST_TABLE_TD));
        }
        String formattedValue;
        if (value == null) {
            formattedValue = MessagesOther.LABEL_UNSET_EMPTY_VALUE.message(pageContext);
        } else {
            User user = Commons.getUser(pageContext.getSession());
            	formattedValue = ViewUtil.getOutput(user, new StrutsWebHelper(pageContext), processId, variable,massBool);	
        }
        tr.addElement(new TD(formattedValue).setClass(Resources.CLASS_LIST_TABLE_TD));
        index++;
        return tr;
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }

}
