package ru.runa.af.web.form;

import com.google.common.base.Strings;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import ru.runa.common.web.MessagesException;

@Getter
@Setter
public class SaveJobForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    private String jobId;
    private String dueDate;
    private String processId;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (Strings.isNullOrEmpty(getJobId()) //
                || Strings.isNullOrEmpty(getDueDate()) //
                || Strings.isNullOrEmpty(getProcessId()) //
        ) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_FILL_REQUIRED_VALUES.getKey()));
        }
        return errors;
    }
}
