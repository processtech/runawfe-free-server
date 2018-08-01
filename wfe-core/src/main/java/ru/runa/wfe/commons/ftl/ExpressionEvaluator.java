package ru.runa.wfe.commons.ftl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateModel;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarInterval;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

@CommonsLog
public class ExpressionEvaluator {
    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");
    private static final Map<String, TemplateModel> staticModels = Maps.newHashMap();
    static {
        BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(ClassLoaderUtil.getExtensionClassLoader());
            for (String className : SystemProperties.getFreemarkerStaticClassNames()) {
                try {
                    int li = className.lastIndexOf(".");
                    String simpleClassName = li != -1 ? className.substring(li + 1) : className;
                    staticModels.put(simpleClassName, wrapper.getStaticModels().get(className));
                } catch (Exception e) {
                    log.error("Unable to register statics from " + className, e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static Date evaluateDueDate(IVariableProvider variableProvider, String expression) {
        Date baseDate;
        String durationString = null;
        if (expression != null && expression.startsWith("#")) {
            String baseDateVariableName = expression.substring(2, expression.indexOf("}"));
            Object o = variableProvider.getValue(baseDateVariableName);
            baseDate = TypeConversionUtil.convertTo(Date.class, o);
            if (baseDate == null) {
                throw new InternalApplicationException("Invalid base date, not defined by name '" + baseDateVariableName + "'");
            }
            int endOfELIndex = expression.indexOf("}");
            if (endOfELIndex < expression.length() - 1) {
                String durationSeparator = expression.substring(endOfELIndex + 1).trim().substring(0, 1);
                if (!(durationSeparator.equals("+") || durationSeparator.equals("-"))) {
                    throw new InternalApplicationException("Invalid duedate, + or - missing after EL");
                }
                durationString = expression.substring(endOfELIndex + 1).trim();
            }
        } else {
            durationString = expression;
            baseDate = new Date();
        }
        if (Strings.isNullOrEmpty(durationString)) {
            return baseDate;
        } else {
            BusinessCalendar businessCalendar = ApplicationContextFactory.getBusinessCalendar();
            return businessCalendar.apply(baseDate, durationString);
        }
    }

    /**
     * Calculates duration between current moment and due date.
     *
     * @return duration in milliseconds, always >= 0
     */
    public static long evaluateDuration(IVariableProvider variableProvider, String expression) {
        Date dueDate = evaluateDueDate(variableProvider, expression);
        long duration = new CalendarInterval(new Date(), dueDate).getLengthInMillis();
        return duration > 0 ? duration : 0;
    }

    public static Object evaluateVariableNotNull(IVariableProvider variableProvider, String expression) {
        Preconditions.checkNotNull(expression);
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String variableName = expression.substring(2, expression.length() - 1);
            return variableProvider.getValueNotNull(variableName);
        }
        return expression;
    }

    public static Object evaluateVariable(IVariableProvider variableProvider, String expression) {
        Preconditions.checkNotNull(expression);
        if (expression.startsWith("${") && expression.endsWith("}")) {
            String variableName = expression.substring(2, expression.length() - 1);
            return variableProvider.getValue(variableName);
        }
        return expression;
    }

    public static String substitute(String value, Map<String, ?> variables) {
        Preconditions.checkNotNull(value, "invalid string to substitute");
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variable = variables.get(variableName);
            if (variable == null) {
                throw new NullPointerException("Variable '" + variableName + "' is not defined");
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(variable.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String substitute(String value, IVariableProvider variableProvider) {
        if (value == null) {
            return null;
        }
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object variable = variableProvider.getValueNotNull(variableName);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(variable.toString()));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String process(User user, String template, IVariableProvider variableProvider, WebHelper webHelper) {
        FormHashModel model = new FormHashModel(user, variableProvider, webHelper);
        model.putAll(staticModels);
        // if (staticModels.size() > 0) {
        // ClassLoader contextClassLoader =
        // Thread.currentThread().getContextClassLoader();
        // try {
        // Thread.currentThread().setContextClassLoader(ClassLoaderUtil.getExtensionClassLoader());
        // model.putAll(staticModels);
        // return FreemarkerProcessor.process(template, model);
        // } finally {
        // Thread.currentThread().setContextClassLoader(contextClassLoader);
        // }
        // } else {
        return FreemarkerProcessor.process(template, model);
        // }
    }

}
