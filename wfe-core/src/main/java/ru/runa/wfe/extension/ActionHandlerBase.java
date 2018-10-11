package ru.runa.wfe.extension;

import com.google.common.base.MoreObjects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ActionHandlerBase implements ActionHandler {
    protected final Log log = LogFactory.getLog(getClass());
    protected String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("configuration", configuration).toString();
    }
}
