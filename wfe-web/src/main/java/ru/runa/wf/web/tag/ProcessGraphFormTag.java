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

import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.ProcessGraphImageAction;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Maps;

/**
 * Created on 15.04.2004
 * 
 * @jsp.tag name = "processGraphForm" body-content = "empty"
 */
public class ProcessGraphFormTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = -2668305021294162818L;

    private Long taskId;
    private Long childProcessId;
    private String subprocessId;

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getChildProcessId() {
        return childProcessId;
    }

    public void setChildProcessId(Long childProcessId) {
        this.childProcessId = childProcessId;
    }

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
    protected void fillFormData(TD td) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, getProcess().getId());
        params.put("childProcessId", childProcessId);
        params.put("name", subprocessId);
        params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
        String href = Commons.getActionUrl(ProcessGraphImageAction.ACTION_PATH, params, pageContext, PortletUrlType.Resource);
        IMG img = new IMG();
        img.setID("graph");
        img.setSrc(href);
        img.setBorder(0);
        List<GraphElementPresentation> elements = Delegates.getExecutionService().getProcessDiagramElements(getUser(), getIdentifiableId(), subprocessId);
        ProcessGraphElementPresentationVisitor visitor = new ProcessGraphElementPresentationVisitor(getUser(), pageContext, td, subprocessId);
        visitor.visit(elements);
        if (!visitor.getPresentationHelper().getMap().isEmpty()) {
            td.addElement(visitor.getPresentationHelper().getMap());
            img.setUseMap("#" + visitor.getPresentationHelper().getMapName());
        }
        td.addElement(img);
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected boolean isVisible() {
        return true;
    }

    @Override
    protected String getTitle() {
        if (subprocessId != null) {
            return null;
        }
        return Messages.getMessage(Messages.TITLE_PROCESS_GRAPH, pageContext);
    }
}
