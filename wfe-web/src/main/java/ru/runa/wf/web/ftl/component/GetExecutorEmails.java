package ru.runa.wf.web.ftl.component;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.Executor;

public class GetExecutorEmails extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        Object executorIdentity = getParameterAs(Object.class, 0);
        Executor executor = TypeConversionUtil.convertToExecutor(executorIdentity, new DelegateExecutorLoader(user));
        List<String> emails = EmailUtils.getEmails(executor);
        log.debug(emails);
        return EmailUtils.concatenateEmails(emails);
    }

}
