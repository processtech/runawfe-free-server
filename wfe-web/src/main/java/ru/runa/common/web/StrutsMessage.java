package ru.runa.common.web;

import java.util.Set;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Sets;

/**
 * Used to get localized messages.
 */
public class StrutsMessage {

    private static final Log log = LogFactory.getLog(StrutsMessage.class);

    private static final Set<String> declaredProperties = Sets.newConcurrentHashSet();

    private final String strutsPropertyName;

    public StrutsMessage(String strutsPropertyName) {
        if (!declaredProperties.add(strutsPropertyName)) {
            log.error("Property " + strutsPropertyName + " is already declared. Refer to existing property if property is the same.");
        }
        this.strutsPropertyName = strutsPropertyName;
    }

    /**
     * Get struts message, translated according to page locale.
     * 
     * @param pageContext
     *            Processing page context.
     * @return Return message text.
     */
    public String message(PageContext pageContext) {
        return Messages.getMessage(getKey(), pageContext);
    }

    /**
     * Get struts key for message.
     * 
     * @return Return struts key for message.
     */
    public String getKey() {
        return strutsPropertyName;
    }
}
