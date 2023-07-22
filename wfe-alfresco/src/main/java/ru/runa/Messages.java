package ru.runa;

import java.util.ResourceBundle;

import org.apache.commons.logging.LogFactory;

/**
 * I18N
 * @author dofs
 */
public class Messages {

    public static String getMessage(String key) {
        try {
            return ResourceBundle.getBundle("messages").getString(key);
        } catch (Exception e) {
            LogFactory.getLog(Messages.class).error(key, e);
            return "!" + key + "!";
        }
    }
}
