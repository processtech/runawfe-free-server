package ru.runa.wf.logic.bot;

import java.util.Map;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.collect.Maps;

/**
 * Created on 04.07.2005
 *
 */
public class EmailTaskHandler extends TaskHandlerBase {
    protected EmailConfig config;

    @Override
    public void setConfiguration(String configuration) {
        if (!EmailConfigParser.canParse(configuration)) {
            throw new InternalApplicationException("Format of email configuration has been changed in 4.x");
        }
        config = EmailConfigParser.parse(configuration);
    }

    @Override
    public Map<String, Object> handle(final User user, VariableProvider variableProvider, final WfTask task) throws Exception {
        try {
            Interaction interaction = Delegates.getDefinitionService().getTaskNodeInteraction(user, task.getDefinitionVersionId(), task.getNodeId());
            Map<String, Object> map = Maps.newHashMap();
            map.put("interaction", interaction);
            map.put("task", task);
            VariableProvider emailVariableProvider = new MapDelegableVariableProvider(map, variableProvider);
            EmailUtils.prepareMessage(user, config, interaction, emailVariableProvider);
            EmailUtils.sendMessage(config);
        } catch (Exception e) {
            if (config.isThrowErrorOnFailure()) {
                throw e;
            }
            log.error("unable to send email", e);
        }
        return null;
    }
}
