package ru.runa.wfe.extension.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.extension.Configurable;
import ru.runa.wfe.extension.TaskHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Charsets;

public abstract class TaskHandlerBase implements TaskHandler, Configurable {
    protected Log log = LogFactory.getLog(getClass());
    protected byte[] embeddedFile;
    protected String configuration;

    @Override
    public final void setConfiguration(byte[] config, byte[] embeddedFile) throws Exception {
        this.embeddedFile = embeddedFile;
        if (config != null) {
            setConfiguration(new String(config, Charsets.UTF_8));
        }
    }

    @Override
    public String getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(String configuration) throws Exception {
        this.configuration = configuration;
    }

    @Override
    public void onRollback(User user, VariableProvider variableProvider, WfTask task) throws Exception {
    }
}
