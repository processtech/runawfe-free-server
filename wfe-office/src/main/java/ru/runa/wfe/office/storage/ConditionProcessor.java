package ru.runa.wfe.office.storage;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.ParamBasedVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

public class ConditionProcessor {

    private static final String LIKE_EXPR_END = ".toLowerCase()) >= 0";

    private static final String LIKE_EXPR_START = ".toLowerCase().indexOf(";

    private static final String LIKE_LITERAL = "like";

    private static final String OR_EXPR = "||";

    private static final String OR_LITERAL = "OR";

    private static final String AND_EXPR = "&&";

    private static final String SPACE = " ";

    private static final String AND_LITERAL = "AND";

    private static final Log log = LogFactory.getLog(ConditionProcessor.class);

    private static Set<String> operators = Sets.newHashSet(">", ">=", "<", "<=", "!=");

    private static Object previousAttributeValue;
    private static String previousOperator = "";

    private static ScriptEngine engine;
    static {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        engine = engineManager.getEngineByName("JavaScript");
    }

    public static synchronized boolean filter(String condition, Map<String, Object> attributes, IVariableProvider variableProvider) {
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

    private static String parse(String condition, Map<String, Object> attributes, IVariableProvider variableProvider) {
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
                sb = appendAttribute(sb, attributes, token);
            } else if (token.equalsIgnoreCase(LIKE_LITERAL)) {
                previousOperator = LIKE_LITERAL;
                sb.append(LIKE_EXPR_START);
                token = st.nextToken();
                sb.append(token);
                sb.append(LIKE_EXPR_END);
            } else if (token.startsWith("@")) {
                String variableName = token.substring(1);
                String toAppend = "";
                if (variableProvider instanceof ParamBasedVariableProvider) {
                    ParamsDef paramsDef = ((ParamBasedVariableProvider) variableProvider).getParamsDef();
                    if (paramsDef != null) {
                        if (paramsDef.getInputParam(variableName) != null) {
                            Object inputParamValue = paramsDef.getInputParamValue(variableName, variableProvider);
                            if (inputParamValue instanceof Number) {
                                toAppend = inputParamValue.toString();
                            } else if (inputParamValue instanceof Date) {
                                toAppend = ((Date) inputParamValue).getTime() + "";
                            } else {
                                toAppend = "'" + inputParamValue + "'";
                            }
                        } else {
                            WfVariable wfVariable = variableProvider.getVariableNotNull(variableName);
                            VariableFormat format = wfVariable.getDefinition().getFormatNotNull();
                            if (format instanceof LongFormat) {
                                toAppend = wfVariable.getStringValue();
                            } else if (format instanceof DateTimeFormat || format instanceof DateFormat) {
                                Date date = (Date) wfVariable.getValue();
                                toAppend = String.valueOf(date.getTime());
                            } else {
                                toAppend = "'" + wfVariable.getStringValue() + "'";
                            }
                        }
                    }
                    sb.append(SPACE);
                    sb.append(toAppend);
                }
            } else {
                sb.append(SPACE);
                if (previousAttributeValue != null && (previousAttributeValue instanceof Date) && operators.contains(previousOperator)) {
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

    private static long getTime(String source) {
        source = source.replaceAll("'", "");
        Date date = null;
        try {
            date = CalendarUtil.convertToDate(source, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        } catch (InternalApplicationException e) {
            date = CalendarUtil.convertToDate(source, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
        } catch (Exception e) {
            log.error(String.format("error parse date [%s]", source));
            throw Throwables.propagate(e);
        }
        return date == null ? 0 : date.getTime();
    }

    private static StringBuilder appendAttribute(StringBuilder sb, Map<String, Object> variables, String token) {
        String var = token.substring(1, token.length() - 1);
        if (variables.keySet().contains(var)) {
            String toAppend = "";
            Object obj = variables.get(var);
            previousAttributeValue = obj;
            if (obj instanceof String) {
                toAppend = "'" + obj + "'";
            } else if (obj instanceof Date) {
                toAppend = String.valueOf(((Date) obj).getTime());
            } else {
                toAppend = obj + "";
            }
            sb.append(toAppend);
        }
        return sb;
    }

}
