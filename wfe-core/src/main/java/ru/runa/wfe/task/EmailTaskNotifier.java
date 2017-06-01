package ru.runa.wfe.task;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.task.logic.ITaskNotifier;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.ScriptingVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class EmailTaskNotifier implements ITaskNotifier {
    private static final Log log = LogFactory.getLog(EmailTaskNotifier.class);
    private boolean enabled = true;
    private boolean onlyIfTaskActorEmailDefined = false;
    private byte[] configBytes;
    
    /**
     * TODO: marked for removal
     */
    private List<Long> excludedProcessIds;

    private EmailUtils.EmailsFilter includeEmailsFilter;
    private EmailUtils.EmailsFilter excludeEmailsFilter;
    
    private EmailUtils.ProcessNameFilter includeProcessNameFilter;
    private EmailUtils.ProcessNameFilter excludeProcessNameFilter;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setOnlyIfTaskActorEmailDefined(boolean onlyIfTaskActorEmailDefined) {
        this.onlyIfTaskActorEmailDefined = onlyIfTaskActorEmailDefined;
    }

    @Required
    public void setConfigLocation(String path) {
        try {
            InputStream configInputStream = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
            configBytes = ByteStreams.toByteArray(configInputStream);
            InputStream excludesInputStream = ClassLoaderUtil.getAsStream(path + ".excludes", getClass());
            if (excludesInputStream != null) {
                String excludes = new String(ByteStreams.toByteArray(excludesInputStream), Charsets.UTF_8);
                excludedProcessIds = Lists.transform(Splitter.on("\n").omitEmptyStrings().trimResults().splitToList(excludes),
                        new Function<String, Long>() {

                            @Override
                            public Long apply(String input) {
                                return Long.valueOf(input.replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", ""));
                            }
                        });
            } else {
                excludedProcessIds = Lists.newArrayList();
            }
            log.debug("Excluded process ids: " + excludedProcessIds);
        } catch (Exception e) {
            log.error("Configuration error: " + e);
        }
    }

    @Override
    public void onTaskAssigned(ProcessDefinition processDefinition, IVariableProvider variableProvider, Task task, Executor previousExecutor) {
        if (!enabled || configBytes == null) {
            return;
        }
        try {
            log.debug("About " + task + " assigned to " + task.getExecutor() + ", previous: " + previousExecutor);
            Long rootProcessId = ProcessHierarchyUtils.getProcessIds(task.getProcess().getHierarchyIds()).get(0);
            if (excludedProcessIds.contains(rootProcessId)) {
                log.debug("Ignored due to excluded process id " + rootProcessId);
                return;
            }
            
            final String processName = task.getProcess().getDeployment().getName();
            if (! EmailUtils.isProcessNameMatching(processName,
                    includeProcessNameFilter, excludeProcessNameFilter)) {
                log.debug("Ignored due to excluded process name " + processName);
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
            Interaction interaction = processDefinition.getInteractionNotNull(task.getNodeId());
            Map<String, Object> map = Maps.newHashMap();
            map.put("interaction", interaction);
            map.put("task", task);
            map.put("emails", emails);
            ScriptingVariableProvider scriptingVariableProvider = new ScriptingVariableProvider(variableProvider);
            IVariableProvider emailVariableProvider = new MapDelegableVariableProvider(map, scriptingVariableProvider);
            EmailUtils.prepareMessage(UserHolder.get(), config, interaction, emailVariableProvider);
            EmailUtils.sendMessageRequest(config);
        } catch (Exception e) {
            log.warn("", e);
        }
    }

	public void setIncludeEmailsFilter(String includeEmailsFilter) {
		this.includeEmailsFilter = EmailUtils.validateAndCreateEmailsFilter(includeEmailsFilter);
	}

	public void setExcludeEmailsFilter(String excludeEmailsFilter) {
		this.excludeEmailsFilter = EmailUtils.validateAndCreateEmailsFilter(excludeEmailsFilter);
	}
	
    public void setIncludeProcessNameFilter(List<String> includeProcessNameFilter) {
        this.includeProcessNameFilter = EmailUtils.validateAndCreateProcessNameFilter(includeProcessNameFilter);
    }

    public void setExcludeProcessNameFilter(List<String> excludeProcessNameFilter) {
        this.excludeProcessNameFilter = EmailUtils.validateAndCreateProcessNameFilter(excludeProcessNameFilter);
    }

}
