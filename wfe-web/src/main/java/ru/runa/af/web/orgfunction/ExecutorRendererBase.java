package ru.runa.af.web.orgfunction;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public abstract class ExecutorRendererBase implements ParamRenderer {

    @Override
    public boolean hasJSEditor() {
        return true;
    }

    @Override
    public List<String[]> loadJSEditorData(User user) throws Exception {
        List<String[]> result = new ArrayList<String[]>();
        List<? extends Executor> executors = loadExecutors(user);
        if (executors.size() == 0) {
            result.add(new String[] { "0", "No executors found" });
        } else {
            for (Executor executor : executors) {
                result.add(new String[] { getValue(executor), executor.getName() });
            }
        }
        return result;
    }

    protected abstract List<? extends Executor> loadExecutors(User user) throws Exception;

    protected abstract String getValue(Executor executor);

    @Override
    public String getDisplayLabel(User user, String value) {
        try {
            return getExecutor(user, value).getName();
        } catch (Exception e) {
            return "<span class='error'>" + e.getMessage() + "</span>";
        }
    }

    @Override
    public boolean isValueValid(User user, String value) {
        try {
            getExecutor(user, value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected abstract Executor getExecutor(User user, String value) throws Exception;
}
