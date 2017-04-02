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
package ru.runa.report.web.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 06.10.2004
 * 
 * @struts:action path="/redeployReport" name="fileForm" validate="false"
 * @struts.action-forward name="success" path="/manage_report.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_report.do" redirect = "false"
 * @struts.action-forward name="failure_report_does_not_exist" path="/manage_reports.do" redirect = "true"
 */
public class RedeployReportAction extends BaseDeployReportAction {
    public static final String ACTION_PATH = "/redeployReport";

    private Long reportId;

    @Override
    protected void doAction(User user, WfReport report, byte[] file) throws Exception {
        reportId = report.getId();
        Delegates.getReportService().redeployReport(user, report, file);
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, reportId);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, reportId);
    }
}
