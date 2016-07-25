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
package ru.runa.wf.web.action;

import java.util.List;

import ru.runa.common.web.action.AbstractExportExcelAction;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * @since 4.3.0
 */
public class ExportExcelProcessesAction extends AbstractExportExcelAction<WfProcess> {

    @Override
    protected String getBatchPresentationId() {
        return "listProcessesForm";
    }

    @Override
    protected List<WfProcess> getData(User user, BatchPresentation batchPresentation) {
        int oldPageNumber = batchPresentation.getPageNumber();
        int oldRangeSize = batchPresentation.getRangeSize();
        try {
            batchPresentation.setPageNumber(1);
            batchPresentation.setRangeSize(Integer.MAX_VALUE);
            return Delegates.getExecutionService().getProcesses(user, batchPresentation);
        } finally {
            batchPresentation.setPageNumber(oldPageNumber);
            batchPresentation.setRangeSize(oldRangeSize);
        }
    }

    @Override
    protected String getFileNamePrefix() {
        return "processes";
    }
}
