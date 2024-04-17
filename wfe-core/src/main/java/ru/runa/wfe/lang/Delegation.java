package ru.runa.wfe.lang;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.extension.Configurable;

@XmlAccessorType(XmlAccessType.FIELD)
public class Delegation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String className;
    private String configuration;

    public Delegation() {
    }

    public Delegation(String className, String configuration) {
        this.className = null == className ? null : className.intern();
        this.configuration = null == configuration ? null : configuration.intern();
    }

    /**
     * Checks all prerequisites needed for execution.
     */
    public void validate() {
        Preconditions.checkNotNull(className, "className in " + this);
    }

    public <T extends Configurable> T getInstance() throws Exception {
        Configurable configurable = ApplicationContextFactory.createAutowiredBean(className);
        configurable.setConfiguration(configuration);
        return (T) configurable;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("class", className).add("configuration", configuration).toString();
    }

}
