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

import java.util.List;
import java.util.Map;

import org.apache.ecs.html.Center;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.ProcessDefinitionGraphImageAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Maps;

/**
 * Created on 30.08.2004
 * 
 * @jsp.tag name = "definitionGraphForm" body-content = "empty"
 */
public class DefinitionGraphFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = 880745425325952663L;
    private String subprocessId;

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getSubprocessId() {
        return subprocessId;
    }
    
    public void setSubprocessId(String subprocessId) {
        this.subprocessId = subprocessId;
    }

    @Override
    protected void fillFormData(final TD tdFormElement) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, getIdentifiableId());
        params.put("name", subprocessId);
        String href = Commons.getActionUrl(ProcessDefinitionGraphImageAction.ACTION_PATH, params, pageContext,
                PortletUrlType.Resource);
        Center center = new Center();
        IMG processGraphImage = new IMG(href);
        processGraphImage.setBorder(0);
        List<NodeGraphElement> elements = Delegates.getDefinitionService().getProcessDefinitionGraphElements(getUser(), getIdentifiableId(), subprocessId);
        DefinitionNodeGraphElementVisitor visitor = new DefinitionNodeGraphElementVisitor(pageContext, subprocessId);
        visitor.visit(elements);
        if (!visitor.getPresentationHelper().getMap().isEmpty()) {
            tdFormElement.addElement(visitor.getPresentationHelper().getMap());
            processGraphImage.setUseMap("#" + visitor.getPresentationHelper().getMapName());
        }
        center.addElement(processGraphImage);
        tdFormElement.addElement(center);
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.READ;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        if (subprocessId != null) {
            return null;
        }
        return Messages.getMessage(Messages.TITLE_PROCESS_GRAPH, pageContext);
    }

}
