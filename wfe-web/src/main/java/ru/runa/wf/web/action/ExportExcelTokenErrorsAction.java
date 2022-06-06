package ru.runa.wf.web.action;

import java.util.List;
import ru.runa.common.web.action.AbstractExportExcelAction;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class ExportExcelTokenErrorsAction extends AbstractExportExcelAction<WfTokenError> {

    @Override
    protected List<WfTokenError> getData(User user, BatchPresentation batchPresentation) {
        batchPresentation.setPageNumber(1);
        batchPresentation.setRangeSize(Integer.MAX_VALUE);
        return Delegates.getSystemService().getTokenErrors(user, batchPresentation);
    }

    @Override
    protected String getFileNamePrefix() {
        return "tokenErrors";
    }
}
