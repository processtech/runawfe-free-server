package ru.runa.wfe.commons.ftl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.commons.web.WebUtils;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public abstract class FormComponent implements TemplateMethodModelEx, Serializable {
    public static final String TARGET_PROCESS_PREFIX = "TargetProcess";
    private static final long serialVersionUID = 1L;
    private static final String RICH_COMBO_VALUE_PREFIX = "value@";
    protected final Log log = LogFactory.getLog(getClass());
    protected User user;
    protected IVariableProvider variableProvider;
    protected WebHelper webHelper;
    private List<TemplateModel> arguments;
    private boolean targetProcess;

    public void init(User user, WebHelper webHelper, IVariableProvider variableProvider, boolean targetProcess) {
        this.user = user;
        this.webHelper = webHelper;
        this.variableProvider = variableProvider;
        this.targetProcess = targetProcess;
    }

    public void initChained(FormComponent parent) {
        init(parent.user, parent.webHelper, parent.variableProvider, false);
        arguments = parent.arguments;
    }

    public void setArguments(List arguments) {
        this.arguments = arguments;
        if (targetProcess) {
            Long targetProcessId = getParameterVariableValueNotNull(Long.class, 0);
            this.arguments.remove(0);
            this.variableProvider = ((AbstractVariableProvider) variableProvider).getSameProvider(targetProcessId);
        }
    }

    @Override
    public final Object exec(List arguments) {
        try {
            setArguments(arguments);
            return renderRequest();
        } catch (Throwable th) {
            log.error(String.format("Process %s:%s", variableProvider.getProcessId(), arguments.toString()), th);
            return "<div style=\"background-color: #ffb0b0; border: 1px solid red; padding: 3px;\">" + th.getMessage() + "</div>";
        }
    }

    /**
     * @return component name
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Optionally used in {@link FormComponentSubmissionHandler}, {@link FormComponentSubmissionPostProcessor} and ajax processing.
     */
    public String getVariableNameForSubmissionProcessing() {
        return getParameterAsString(0);
    }

    /**
     * Invoked on page rendering
     * 
     * @return component html
     */
    protected abstract Object renderRequest() throws Exception;

    protected String getParameterAsString(int i) {
        return getParameterAs(String.class, i);
    }

    protected <T> T getParameterAs(Class<T> clazz, int i) {
        Object paramValue = null;
        if (i < arguments.size()) {
            try {
                paramValue = BeansWrapper.getDefaultInstance().unwrap(arguments.get(i));
            } catch (TemplateModelException e) {
                Throwables.propagate(e);
            }
        }
        return TypeConversionUtil.convertTo(clazz, paramValue);
    }

    protected <T> T getRichComboParameterAs(Class<T> clazz, int i) {
        Object paramValue = null;
        if (i < arguments.size()) {
            try {
                paramValue = BeansWrapper.getDefaultInstance().unwrap(arguments.get(i));
            } catch (TemplateModelException e) {
                Throwables.propagate(e);
            }
        }
        if (paramValue instanceof String) {
            String value = (String) paramValue;
            if (value.startsWith(RICH_COMBO_VALUE_PREFIX)) {
                value = value.substring(RICH_COMBO_VALUE_PREFIX.length()).trim();
                return TypeConversionUtil.convertTo(clazz, value);
            }
            WfVariable variable = variableProvider.getVariable(value);
            if (variable != null) {
                return TypeConversionUtil.convertTo(clazz, variable.getValue());
            }
        }
        // back compatibility (pre 4.2.0)
        return TypeConversionUtil.convertTo(clazz, paramValue);
    }

    protected <T> T getParameterVariableValueNotNull(Class<T> clazz, int i) {
        String variableName = getParameterAsString(i);
        return variableProvider.getValueNotNull(clazz, variableName);
    }

    protected <T> T getParameterVariableValue(Class<T> clazz, int i, T defaultValue) {
        String variableName = getParameterAsString(i);
        T value = variableProvider.getValue(clazz, variableName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    protected List<String> getMultipleParameter(int i) {
        List<String> list = Lists.newArrayList();
        while (true) {
            String option = getParameterAsString(i);
            if (option == null) {
                break;
            }
            list.add(option);
            i++;
        }
        return list;
    }

    protected String exportScript(Map<String, String> substitutions, boolean globalScope) {
        return exportScript(substitutions, globalScope, getClass().getSimpleName());
    }

    protected String exportScript(Map<String, String> substitutions, boolean globalScope, String name) {
        String path = "scripts/" + name + ".js";
        try {
            if (webHelper == null) {
                return "";
            }
            if (globalScope) {
                if (webHelper.getRequest().getAttribute(path) != null) {
                    return "";
                }
                webHelper.getRequest().setAttribute(path, Boolean.TRUE);
            }
            InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
            return WebUtils.getFormComponentScript(javascriptStream, substitutions);
        } catch (Exception e) {
            log.error("Tag execution error", e);
            return "<p style='color: red;'>Unable to export script <b>" + path + "</b> to page</p>";
        }
    }

}
