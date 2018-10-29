package ru.runa.af.web.orgfunction;

import java.util.List;

import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.user.User;

public class StringRenderer implements ParamRenderer {

    @Override
    public boolean hasJSEditor() {
        return false;
    }

    @Override
    public List<String[]> loadJSEditorData(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayLabel(User user, String value) {
        return value;
    }

    @Override
    public boolean isValueValid(User user, String value) {
        return value.trim().length() > 0;
    }

}
