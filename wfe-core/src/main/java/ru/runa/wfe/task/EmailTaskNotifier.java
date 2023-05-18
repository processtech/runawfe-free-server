package ru.runa.wfe.task;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.task.logic.TaskNotifier;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

@CommonsLog
public class EmailTaskNotifier implements TaskNotifier {
    private boolean enabled = true;
    private boolean onlyIfTaskActorEmailDefined = false;
    private String configPath;
    private byte[] configBytes;

    private EmailUtils.EmailsFilter includeEmailsFilter;
    private EmailUtils.EmailsFilter excludeEmailsFilter;

    private EmailUtils.ProcessNameFilter includeProcessNameFilter;
    private EmailUtils.ProcessNameFilter excludeProcessNameFilter;

    private EmailUtils.SwimlaneNameFilter includeSwimlaneNameFilter;
    private EmailUtils.SwimlaneNameFilter excludeSwimlaneNameFilter;

    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnlyIfTaskActorEmailDefined(boolean onlyIfTaskActorEmailDefined) {
        this.onlyIfTaskActorEmailDefined = onlyIfTaskActorEmailDefined;
    }

    @Required
    public void setConfigLocation(String path) {
        try {
            this.configPath = path;
            InputStream configInputStream = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
            configBytes = ByteStreams.toByteArray(configInputStream);
        } catch (Exception e) {
            log.error("Configuration error: " + e);
        }
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

    public void setIncludeSwimlaneNameFilter(List<String> includeSwimlaneNameFilter) {
        this.includeSwimlaneNameFilter = EmailUtils.validateAndCreateSwimlaneNameFilter(includeSwimlaneNameFilter);
    }

    public void setExcludeSwimlaneNameFilter(List<String> excludeSwimlaneNameFilter) {
        this.excludeSwimlaneNameFilter = EmailUtils.validateAndCreateSwimlaneNameFilter(excludeSwimlaneNameFilter);
    }

    @PostConstruct
    public void printConfigInfo() {
        log.info("Configured " + this);
    }

    @Override
    public void onTaskAssigned(ParsedProcessDefinition parsedProcessDefinition, VariableProvider variableProvider, Task task, Executor previousExecutor) {
        if (!enabled || configBytes == null) {
            return;
        }
        try {
            log.debug("About " + task + " assigned to " + task.getExecutor() + ", previous: " + previousExecutor);
            final String processName = processDefinitionLoader.getDefinition(task.getProcess()).getName();
            if (!EmailUtils.isProcessNameMatching(processName, includeProcessNameFilter, excludeProcessNameFilter)) {
                log.debug("Ignored due to excluded process name " + processName);
                return;
            }
            final String swimlaneName = task.getSwimlaneName();
            if (!EmailUtils.isSwimlaneNameMatching(swimlaneName, includeSwimlaneNameFilter, excludeSwimlaneNameFilter)) {
                log.debug("Ignored due to excluded swimlane name " + swimlaneName);
                return;
            }

            EmailConfig config = EmailConfigParser.parse(configBytes);
            List<String> emailsToSend = EmailUtils.getEmails(task.getExecutor());
            List<String> emailsWereSent = EmailUtils.getEmails(previousExecutor);
            emailsToSend.removeAll(emailsWereSent);
            if (onlyIfTaskActorEmailDefined && emailsToSend.isEmpty()) {
                log.debug("Ignored due to empty emails, previously emails were sent: " + emailsWereSent);
                return;
            }
            emailsToSend = EmailUtils.filterEmails(emailsToSend, includeEmailsFilter, excludeEmailsFilter);
            if (emailsToSend.isEmpty()) {
                log.debug("Ignored due to empty emails after email filter has been applied");
                return;
            }
            String emails = EmailUtils.concatenateEmails(emailsToSend);
            Interaction interaction = parsedProcessDefinition.getInteractionNotNull(task.getNodeId());
            Map<String, Object> map = Maps.newHashMap();
            map.put("interaction", interaction);
            map.put("task", task);
            map.put("emails", emails);
            VariableProvider emailVariableProvider = new MapDelegableVariableProvider(map, variableProvider);
            EmailUtils.prepareMessage(UserHolder.get(), config, interaction, emailVariableProvider);
            EmailUtils.sendMessageRequest(config);
        } catch (Exception e) {
            log.warn("", e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("enabled", enabled).add("onlyIfTaskActorEmailDefined", onlyIfTaskActorEmailDefined)
                .add("configPath", configPath).add("includeEmailsFilter", includeEmailsFilter).add("excludeEmailsFilter", excludeEmailsFilter)
                .add("includeProcessNameFilter", includeProcessNameFilter).add("excludeProcessNameFilter", excludeProcessNameFilter)
                .add("includeSwimlaneNameFilter", includeSwimlaneNameFilter).add("excludeSwimlaneNameFilter", excludeSwimlaneNameFilter).toString();
    }
}
