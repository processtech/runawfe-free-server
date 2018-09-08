package ru.runa.wfe.job.impl;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;

public class ExpiredTasksNotifier {
    protected final Log log = LogFactory.getLog(getClass());
    private boolean enabled = true;
    private boolean onlyIfTaskActorEmailDefined = false;
    private byte[] configBytes;

    private EmailUtils.EmailsFilter includeEmailsFilter;
    private EmailUtils.EmailsFilter excludeEmailsFilter;

    private EmailUtils.ProcessNameFilter includeProcessNameFilter;
    private EmailUtils.ProcessNameFilter excludeProcessNameFilter;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private long scheduledTimerTaskPeriod;

    @Required
    public void setConfigLocation(String configLocation) {
        InputStream in = ClassLoaderUtil.getAsStreamNotNull(configLocation, getClass());
        try {
            configBytes = ByteStreams.toByteArray(in);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Required
    public void setScheduledTimerTaskPeriod(long scheduledTimerTaskPeriod) {
        this.scheduledTimerTaskPeriod = scheduledTimerTaskPeriod;
    }

    public void setOnlyIfTaskActorEmailDefined(boolean onlyIfTaskActorEmailDefined) {
        this.onlyIfTaskActorEmailDefined = onlyIfTaskActorEmailDefined;
    }

    public void setIncludeEmailsFilter(List<String> includeEmailsFilter) {
        this.includeEmailsFilter = EmailUtils.validateAndCreateEmailsFilter(includeEmailsFilter);
    }

    public void setExcludeEmailsFilter(List<String> excludeEmailsFilter) {
        this.excludeEmailsFilter = EmailUtils.validateAndCreateEmailsFilter(excludeEmailsFilter);
    }

    public void setIncludeProcessNameFilter(List<String> includeProcessNameFilter) {
        this.includeProcessNameFilter = EmailUtils.validateAndCreateProcessNameFilter(includeProcessNameFilter);
    }

    public void setExcludeProcessNameFilter(List<String> excludeProcessNameFilter) {
        this.excludeProcessNameFilter = EmailUtils.validateAndCreateProcessNameFilter(excludeProcessNameFilter);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Transactional
    public void execute() {
        if (!enabled || configBytes == null) {
            return;
        }
        Date curDate = Calendar.getInstance().getTime();
        List<Task> tasks = taskDao.getAllExpiredTasks(curDate);
        for (Task task : tasks) {
            if ((task.getDeadlineDate().getTime() + scheduledTimerTaskPeriod) < curDate.getTime()) {
                continue;
            }
            final String processName = task.getProcess().getDeployment().getName();
            if (!EmailUtils.isProcessNameMatching(processName, includeProcessNameFilter, excludeProcessNameFilter)) {
                log.debug("Ignored due to excluded process name " + processName);
                return;
            }
            List<String> emailsToSend = EmailUtils.getEmails(task.getExecutor());
            if (onlyIfTaskActorEmailDefined && emailsToSend.isEmpty()) {
                log.debug("Ignored due to empty email of executor: " + task.getExecutor().getName());
                return;
            }
            emailsToSend = EmailUtils.filterEmails(emailsToSend, includeEmailsFilter, excludeEmailsFilter);
            if (emailsToSend.isEmpty()) {
                log.debug("Ignored due to empty emails after email filter has been applied");
                return;
            }
            String emails = EmailUtils.concatenateEmails(emailsToSend);
            EmailConfig config = EmailConfigParser.parse(configBytes);
            config.getHeaderProperties().put(EmailConfig.HEADER_TO, emails);
            config.setMessage(config.getMessage() + " - " + task.getName());
            EmailUtils.sendMessageRequest(config);
        }
    }

}
