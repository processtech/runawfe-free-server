package ru.runa.wfe.extension.handler;

import com.google.common.collect.Maps;
import java.util.Map;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

/**
 * Send email.
 *
 * @author dofs[197@gmail.com]
 */
public class SendEmailActionHandler extends ActionHandlerBase {

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        try {
            // we parse config here because it changes its state during execution
            if (!EmailConfigParser.canParse(configuration)) {
                throw new Exception("Invalid configuration " + configuration);
            }
            EmailConfig config = EmailConfigParser.parse(configuration);
            try {
                Task task = executionContext.getTask();
                Interaction interaction = task != null ? executionContext.getParsedProcessDefinition().getInteractionNotNull(task.getNodeId()) : null;
                Map<String, Object> map = Maps.newHashMap();
                map.put("task", task);
                map.put("interaction", interaction);
                map.put("process", executionContext.getProcess());
                VariableProvider emailVariableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
                EmailUtils.prepareMessage(UserHolder.get(), config, interaction, emailVariableProvider);
                EmailUtils.sendMessageRequest(config);
            } catch (Exception e) {
                if (config.isThrowErrorOnFailure()) {
                    throw e;
                }
                log.error("unable to send email", e);
            }
        } catch (Exception e) {
            if (SystemProperties.ignoreErrorsInSendEmailActionHandler()) {
                log.warn("unable to send email: " + e);
                return;
            }
        }
    }
}
