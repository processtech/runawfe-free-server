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
import ru.runa.wf.web.action.HistoryGraphImageAction;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Maps;

/**
 * 
 * @jsp.tag name = "showGraphHistoryForm" body-content = "JSP"
 */
public class ShowGraphHistoryFormTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long childProcessId;
    private String subprocessId;

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getTaskId() {
        return taskId;
    }

    public void setChildProcessId(Long childProcessId) {
        this.childProcessId = childProcessId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getChildProcessId() {
        return childProcessId;
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
    protected void fillFormData(final TD formDataTD) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, getProcess().getId());
        params.put("childProcessId", childProcessId);
        params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
        params.put("name", subprocessId);
        String href = Commons.getActionUrl(HistoryGraphImageAction.ACTION_PATH, params, pageContext, PortletUrlType.Resource);
        List<NodeGraphElement> elements = Delegates.getAuditService().getProcessHistoryDiagramElements(getUser(), getIdentifiableId(),
                taskId, subprocessId);
        GraphHistoryElementPresentationVisitor visitor = new GraphHistoryElementPresentationVisitor(pageContext, formDataTD, subprocessId);
        visitor.visit(elements);
        IMG img = new IMG();
        img.setSrc(href);
        img.setBorder(1);
        formDataTD.addElement(img);
        if (!visitor.getResultMap().isEmpty()) {
            formDataTD.addElement(visitor.getResultMap());
            img.setUseMap("#" + GraphElementPresentationHelper.MAP_NAME + (subprocessId != null ? subprocessId : ""));
        }

        /*
         * if (logElements.size() > 0) {
         * 
         * for (LogElementPresentation logElement : logElements) {
         * 
         * if (logElement.isSubProcessElementLog()) {
         * List<ProcessElementPresentation> subprocesses =
         * logElement.getProcessElements();
         * 
         * if (subprocesses.size() > 0) { org.apache.ecs.html.Map map = new
         * org.apache.ecs.html.Map(); map.setName("processMap");
         * 
         * for (ProcessElementPresentation elementPresentation : subprocesses) {
         * 
         * if (elementPresentation.isReadPermission()) {
         * 
         * int mlSize = 17; int maxItemsPerLine = 10; int additionalHeight = 0;
         * int mainDivSize = mlSize * elementPresentation.getIds().size();
         * 
         * if (mainDivSize > (maxItemsPerLine * mlSize)) { additionalHeight =
         * (int) Math.ceil(mainDivSize / (maxItemsPerLine * mlSize)) * mlSize;
         * mainDivSize = maxItemsPerLine * mlSize; }
         * 
         * int[] ltCoords = new int[] {
         * elementPresentation.getGraphConstraints()[2] - (mainDivSize / 2),
         * elementPresentation.getGraphConstraints()[3] + (mlSize / 2) +
         * additionalHeight }; StringBuffer buf = new StringBuffer();
         * buf.append(
         * "<div class=\"multiInstanceContainer\" style=\" position: absolute; "
         * ); buf.append("width: ").append(mainDivSize).append("px;");
         * buf.append("left: ").append(ltCoords[0] + 270).append("px;");
         * buf.append("top: ").append(ltCoords[1] +
         * 150).append("px; z-index:20;\">");
         * 
         * for (int i = 0; i < elementPresentation.raphCongetIds().size(); i++)
         * { Long subprocessId = elementPresentation.getIds().get(i);
         * buf.append("<div class=\"multiInstanceBox\" style=\"width: "); buf
         * .append(mlSize).append("px; height: ").append(mlSize).append("px;\" "
         * ); buf.append(
         * "onmouseover=\"this.style.backgroundColor='gray';\" onmouseout=\"this.style.backgroundColor='white';\">"
         * ); buf.append("<a href=\"").append(getSubprocessHistoryInstanceUrl
         * (subprocessId)).append("\" style>&nbsp;").append(i +
         * 1).append("&nbsp;</a>"); buf.append("</div>");
         * 
         * if (((i + 1) % maxItemsPerLine) == 0) { buf.append("\n"); } }
         * 
         * buf.append("</div>"); formDataTD.addElement(buf.toString()); } }
         * 
         * formDataTD.addElement(map);
         * 
         * img.setUseMap("#processMap"); } } else { String toolTipId = "log_" +
         * logElements.indexOf(logElement) + "_tt"; StringBuffer buf = new
         * StringBuffer();buf.append(
         * "<div class=\"multiInstanceContainer\" style=\" position: absolute; z-index:10; opacity: 0.1; filter:progid:DXImageTransform.Microsoft.Alpha(opacity=10); "
         * ); buf.append("width: ").append(logElement.getGraphCoordinates()[2
         * ]).append("px;");
         * buf.append("height: ").append(logElement.getGraphCoordinates
         * ()[3]).append("px;");
         * buf.append("left: ").append(logElement.getGraphCoordinates()[0] +
         * 270).append("px;");
         * buf.append("top: ").append(logElement.getGraphCoordinates()[1] + 150
         * ).append("px;\" onmouseover=\"showTip(event, '").append(toolTipId
         * ).append ("')\" onmouseout=\" hideTip('").append(toolTipId).append
         * ("')\" >");
         * 
         * buf.append("</div>");
         * 
         * buf.append("<span id=\""); buf.append(toolTipId);
         * buf.append("\" class=\"field-hint\" style=\"display: none;\">");
         * buf.append(logElement.getLog()); buf.append("</span>");
         * 
         * formDataTD.addElement(buf.toString()); } } }
         */
    }

    // private String getSubprocessHistoryInstanceUrl(Long id) {
    // Map<String, String> params = new HashMap<String, String>();
    // params.put(IdForm.ID_INPUT_NAME, String.valueOf(id));
    // params.put(TaskIdForm.TASK_ID_INPUT_NAME, String.valueOf(taskId));
    // return Commons.getActionUrl(Resources.ACTION_SHOW_GRAPH_HISTORY, params,
    // pageContext, PortletUrl.Render);
    // }

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
        return Messages.getMessage(Messages.TITLE_PROCESS_GRAPH, pageContext);
    }

}
