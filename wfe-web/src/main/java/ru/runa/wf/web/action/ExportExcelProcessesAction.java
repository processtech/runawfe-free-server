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
