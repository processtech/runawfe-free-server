package ru.runa.wfe.office.storage;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableProvider;

@CommonsLog
public class ConditionProcessor {

    public static final char UNICODE_CHARACTER_OVERLINE = '\u203E';

    private static final String LIKE_EXPR_END = ") != null";

    private static final String LIKE_EXPR_START = ".match(";

    private static final String LIKE_LITERAL = "like";

    private static final String OR_EXPR = "||";

    private static final String OR_LITERAL = "OR";

    private static final String AND_EXPR = "&&";

    private static final String SPACE = " ";

    private static final String AND_LITERAL = "AND";

    private static Set<String> operators = Sets.newHashSet(">", ">=", "<", "<=", "!=");

    private static Object previousAttributeValue;
    private static String previousOperator = "";

    private static ScriptEngine engine;

    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("JavaScript");
    }

    public static synchronized boolean filter(String condition, Map<String, Object> attributes, VariableProvider variableProvider) {
        try {
            clear();
            String query = parse(condition, attributes, variableProvider);
            return (Boolean) engine.eval(query);
        } catch (Exception e) {
            log.error("error parse condition \"" + condition + "\"", e);
            throw Throwables.propagate(e);
        }
    }

    private static void clear() {
        previousAttributeValue = null;
        previousOperator = "";
    }

    private static String parse(String condition, Map<String, Object> attributes, VariableProvider variableProvider) {
        condition = hideSpacesInAttributeNames(condition);
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(condition);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equalsIgnoreCase(AND_LITERAL)) {
                previousOperator = AND_LITERAL;
                sb.append(SPACE);
                sb.append(AND_EXPR);
            } else if (token.equalsIgnoreCase(OR_LITERAL)) {
                previousOperator = OR_LITERAL;
                sb.append(SPACE);
                sb.append(OR_EXPR);
            } else if (token.startsWith("[") && token.endsWith("]")) {
                sb.append(SPACE);
                sb = appendAttribute(sb, attributes, token.replace(UNICODE_CHARACTER_OVERLINE, ' '));
            } else if (token.equalsIgnoreCase(LIKE_LITERAL)) {
                previousOperator = LIKE_LITERAL;
                sb.append(LIKE_EXPR_START).append("/");
                token = st.nextToken();
                if (token.startsWith("@")) {
                    sb.append(extractVariableValue(token, variableProvider, false).replace("%", ".*"));
                } else {
                    sb.append(token.replace("%", ".*"));
                }
                sb.append("/g").append(LIKE_EXPR_END);
            } else if (token.startsWith("@")) {
                sb.append(SPACE).append(extractVariableValue(token, variableProvider, true));
            } else {
                sb.append(SPACE);
                if (previousAttributeValue != null && previousAttributeValue instanceof Date && operators.contains(previousOperator)) {
                    // handle date string value. For example: [startDate] > '16.05.2015'
                    sb.append(getTime(token));
                } else {
                    sb.append(token);
                }
                if (operators.contains(token)) {
                    previousOperator = token;
                }
            }
        }
        return sb.toString();
    }

    private static String extractVariableValue(String token, VariableProvider variableProvider, boolean adjustValue) {
        final String variableName = token.substring(1);
        final Object value = variableProvider.getValue(variableName);
        return formatParameterValue(value, adjustValue);
    }

    private static long getTime(String source) {
        source = source.replaceAll("'", "");
        try {
            return CalendarUtil.convertToDate(source, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT).getTime();
        } catch (InternalApplicationException e) {
            return CalendarUtil.convertToDate(source, CalendarUtil.DATE_WITHOUT_TIME_FORMAT).getTime();
        } catch (Exception e) {
            throw new InternalApplicationException("Unable parse date '" + source + "'", e);
        }
    }

    private static StringBuilder appendAttribute(StringBuilder sb, Map<String, Object> variables, String token) {
        String var = token.substring(1, token.length() - 1);
        if (variables.keySet().contains(var)) {
            Object obj = variables.get(var);
            previousAttributeValue = obj;
            sb.append(formatParameterValue(obj));
        }
        return sb;
    }

    private static String formatParameterValue(Object value) {
        return formatParameterValue(value, true);
    }

    private static String formatParameterValue(Object value, boolean adjustValue) {
        if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Date) {
            return String.valueOf(((Date) value).getTime());
        } else if (value instanceof Executor) {
            return adjustValue ? "'" + ((Executor) value).getName() + "'" : ((Executor) value).getName();
        }
        return adjustValue ? "'" + value + "'" : value.toString();
    }

    public static String hideSpacesInAttributeNames(String condition) {
        char[] conditionChars = condition.toCharArray();
        boolean attributeName = false;
        for (int i = 0; i < conditionChars.length; i++) {
            char c = conditionChars[i];
            if (c == '[') {
                attributeName = true;
            } else if (c == ']') {
                attributeName = false;
            } else if (c == ' ') {
                if (attributeName) {
                    conditionChars[i] = UNICODE_CHARACTER_OVERLINE;
                }
            }
        }
        return new String(conditionChars);
    }
}
