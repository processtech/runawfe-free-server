package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.user.User;

/**
 * Used in substitutions (web-interface). Since 3.2.1
 */
public interface ParamRenderer {

    /**
     * Returns user-friendly display label for parameter value
     */
    public String getDisplayLabel(User user, String value);

    /**
     * Whether parameter has its own javascript editor
     */
    public boolean hasJSEditor();

    /**
     * Called from ajax serlvlet during JS editor initialization
     */
    public List<String[]> loadJSEditorData(User user) throws Exception;

    /**
     * Validates parameter value
     */
    public boolean isValueValid(User user, String value);
}
