package ru.runa.wfe.script.common;

import com.google.common.collect.Lists;
import java.util.List;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@XmlTransient()
public abstract class ScriptOperation {
    protected Log log = LogFactory.getLog(getClass());

    public List<String> getExternalResources() {
        return Lists.newArrayList();
    }

    public abstract void validate(ScriptExecutionContext context);

    public abstract void execute(ScriptExecutionContext context);
}
