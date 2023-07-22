package ru.runa.alfresco.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;

import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.AlfObject;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * Represents search condition.
 * 
 * @author dofs
 */
public class Expr {
    private static final DateFormat DATE_QUERY_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000Z");
    private N operand;
    private Op operator;
    private Object[] params;

    public Expr() {
    }

    public Expr(N operand, Op operator, Object... params) {
        this.operand = operand;
        this.operator = operator;
        this.params = params;
        if (this.params == null) {
            this.params = new Object[0];
        }
    }

    public Object[] getParams() {
        return params;
    }

    @Override
    public String toString() {
        String result = operator.getRegexp();
        result = result.replaceAll("\\$operand", Matcher.quoteReplacement(operand.toString()));
        for (int i = 0; i < params.length; i++) {
            String p;
            if (params[i] != null) {
                p = formatParam(params[i]);
            } else {
                p = "NULL";
                LogFactory.getLog(getClass()).warn("Null param [" + i + "] in " + result);
            }
            result = result.replaceAll("\\$" + i, Matcher.quoteReplacement(p));
        }
        return result;
    }

    private String formatParam(Object param) {
        if (param == null) {
            throw new NullPointerException(operator.getRegexp());
        }
        if (param instanceof Calendar) {
            param = ((Calendar) param).getTime();
        }
        if (param instanceof Date) {
            return CalendarUtil.format((Date) param, DATE_QUERY_FORMAT);
        }
        if (param instanceof AlfObject) {
            return ((AlfObject) param).getUuidRef();
        }
        return param.toString();
    }

}
