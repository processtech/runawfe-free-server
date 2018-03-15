/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.extension.handler.var;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.extension.function.Function;
import ru.runa.wfe.extension.function.Functions;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.IFileVariable;

//TODO introduce strict mode and throw exceptions there
public class FormulaActionHandler extends ActionHandlerBase {
    private ExecutionContext context;
    private final FormulaActionHandlerOperations actions = new FormulaActionHandlerOperations();
    private char[] formula = null;
    private int nowPosition = 0;
    private static final String oneSymbolTokens = "=()+-*/!<>&|^'\",\n;";
    private static final String[] operations = { "&|^", // priority 0
            "<!=>", // priority 1
            "+-", // priority 2
            "*/" // priority 3
    };
    private boolean stringVariableToken = false;
    private boolean quo = false;
    private String nextToken = null;

    private String nextStringToken(char limitingSymbol) {
        if (formula[nowPosition] != limitingSymbol) {
            return null;
        }
        nowPosition++;
        StringBuilder answer = new StringBuilder();
        boolean escapeCharacter = false;
        while (nowPosition < formula.length) {
            if (escapeCharacter) {
                escapeCharacter = false;
                answer.append(formula[nowPosition]);
            } else {
                if (formula[nowPosition] == '\\') {
                    escapeCharacter = true;
                } else {
                    if (formula[nowPosition] == limitingSymbol) {
                        break;
                    } else {
                        answer.append(formula[nowPosition]);
                    }
                }
            }
            nowPosition++;
        }
        if (nowPosition == formula.length) {
            return null;
        }
        nowPosition++;
        return answer.toString();
    }

    private String nextToken() {
        quo = false;
        if (nextToken != null) {
            String ans = nextToken;
            nextToken = null;
            return ans;
        }
        if (stringVariableToken) {
            stringVariableToken = false;
            return nextStringToken('"');
        }
        while (nowPosition < formula.length && formula[nowPosition] == ' ') {
            nowPosition++;
        }
        if (nowPosition == formula.length) {
            return null;
        }
        if (formula[nowPosition] == '"') {
            stringVariableToken = true;
            return ":";
        }
        if (formula[nowPosition] == '\'') {
            quo = true;
            return nextStringToken('\'');
        }
        if (oneSymbolTokens.contains("" + formula[nowPosition])) {
            nowPosition++;
            return "" + formula[nowPosition - 1];
        }
        StringBuilder answer = new StringBuilder();
        while (nowPosition < formula.length && formula[nowPosition] != ' ') {
            if (oneSymbolTokens.contains("" + formula[nowPosition])) {
                break;
            }
            answer.append(formula[nowPosition++]);
        }
        return answer.toString();
    }

    @Override
    public void execute(ExecutionContext context) {
        this.context = context;
        if (configuration == null) {
            log.error("Configuration not found in " + context);
            return;
        }
        log.debug(configuration);
        formula = configuration.toCharArray();
        nowPosition = 0;
        stringVariableToken = false;
        nextToken = null;
        String nf = "";
        String s;
        while ((s = nextToken()) != null) {
            if (!quo && (s.equals(";") || s.equals("\n"))) {
                if (nf.length() > 0) {
                    formula = nf.toCharArray();
                    int ip = nowPosition;
                    String nt = nextToken;
                    boolean b = stringVariableToken;
                    parseFormula();
                    nowPosition = ip;
                    stringVariableToken = b;
                    nextToken = nt;
                    formula = configuration.toCharArray();
                    nf = "";
                }
            } else {
                if (stringVariableToken) {
                    nf += '"' + nextToken().replaceAll("\"", "\\\\\"") + '"';
                } else {
                    s = s.replaceAll("'", "\\\\'");
                    boolean contains = false;
                    for (char c : (oneSymbolTokens + " ").toCharArray()) {
                        contains |= s.contains("" + c);
                    }
                    if (s.length() > 1 && contains || quo) {
                        nf += '\'' + s + '\'';
                    } else {
                        nf += s;
                    }
                }
            }
        }
        if (nf.length() > 0) {
            formula = nf.toCharArray();
            parseFormula();
        }
    }

    private void parseFormula() {
        nowPosition = 0;
        String variableName = nextToken();
        if (stringVariableToken) {
            error("Incorrect variable name: use ' instead \"");
            return;
        }
        if (variableName == null) {
            error("Variable name expected");
            return;
        }
        if (variableName.length() == 1 && oneSymbolTokens.contains(variableName)) {
            error("Incorrect variable name: " + variableName);
            return;
        }
        String equal = nextToken();
        if (equal == null || !equal.equals("=")) {
            error("'=' expected");
            return;
        }
        Object value = parsePriority0();
        WfVariable variable = context.getVariableProvider().getVariable(variableName);
        if (variable != null) {
            Class<?> definedClass = variable.getDefinition().getFormatNotNull().getJavaClass();
            boolean appropriateType = value == null || definedClass.isAssignableFrom(value.getClass());
            if (!appropriateType) {
                appropriateType = variable.getValue() != null && variable.getValue().getClass() == value.getClass();
            }
            if (!appropriateType) {
                value = TypeConversionUtil.convertTo(definedClass, value);
            }
        }
        if (IFileVariable.class.isInstance(value)) {
            IFileVariable fileVariable = (IFileVariable) value;
            value = new FileVariable(fileVariable);
        }
        context.setVariableValue(variableName, value);
    }

    private Object parsePriority0() {
        Object answer = parsePriority1();
        while (true) {
            if (answer == null) {
                return null;
            }
            String s = nextToken();
            if (s == null) {
                return answer;
            }
            if (s.equals(")") || s.equals(",")) {
                nowPosition--;
                return answer;
            }
            if (s.equals("&")) {
                Object operand = parsePriority1();
                answer = actions.and(answer, operand);
                continue;
            }
            if (s.equals("|")) {
                Object operand = parsePriority1();
                answer = actions.or(answer, operand);
                continue;
            }
            if (s.equals("^")) {
                Object operand = parsePriority1();
                answer = actions.xor(answer, operand);
                continue;
            }
            error("Operator expected, but '" + s + "' found at position " + nowPosition);
            return null;
        }
    }

    private Object parsePriority1() {
        Object o1 = parsePriority2();
        if (o1 == null) {
            return null;
        }
        String s = nextToken();
        if (s == null) {
            return o1;
        }
        if (s.equals(")") || s.equals(",") || operations[0].contains(s)) {
            nowPosition--;
            return o1;
        }
        if (s.equals("<")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.lessOrEqual(o1, o2);
            } else {
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.less(o1, o2);
            }
        }
        if (s.equals(">")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.biggerOrEqual(o1, o2);
            } else {
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.bigger(o1, o2);
            }
        }
        if (s.equals("=")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.equal(o1, o2);
            }
        }
        if (s.equals("!")) {
            if (nowPosition < formula.length && formula[nowPosition] == '=') {
                nowPosition++;
                Object o2 = parsePriority2();
                if (o2 == null) {
                    return null;
                }
                return actions.not(actions.equal(o1, o2));
            }
        }
        error("Operator expected, but '" + s + "' found at position " + nowPosition);
        return null;
    }

    private Object parsePriority2() {
        Object answer = parsePriority3();
        while (true) {
            if (answer == null) {
                return null;
            }
            String s = nextToken();
            if (s == null) {
                return answer;
            }
            if (s.equals(")") || s.equals(",") || operations[0].contains(s) || operations[1].contains(s)) {
                nowPosition--;
                return answer;
            }
            if (s.equals("+")) {
                Object operand = parsePriority3();
                answer = actions.sum(answer, operand);
                continue;
            }
            if (s.equals("-")) {
                Object operand = parsePriority3();
                answer = actions.sub(answer, operand);
                continue;
            }
            error("Operator expected, but '" + s + "' found at position " + nowPosition);
            return null;
        }
    }

    private Object parsePriority3() {
        Object answer = parseSimple();
        while (true) {
            if (answer == null) {
                return null;
            }
            String s = nextToken();
            if (s == null) {
                return answer;
            }
            if (s.equals(")") || s.equals(",") || operations[0].contains(s) || operations[1].contains(s) || operations[2].contains(s)) {
                nowPosition--;
                return answer;
            }
            if (s.equals("*")) {
                Object operand = parseSimple();
                answer = actions.mul(answer, operand);
                continue;
            }
            if (s.equals("/")) {
                Object operand = parseSimple();
                answer = actions.div(answer, operand);
                continue;
            }
            error("Operator expected, but '" + s + "' found at position " + nowPosition);
            return null;
        }
    }

    private Object parseSimple() {
        String s = nextToken();
        if (s == null) {
            error("Incorrect token at position " + nowPosition);
            return null;
        }
        if (s.equals("-")) {
            return actions.changeSign(parseSimple());
        }
        if (s.equals("!")) {
            return actions.not(parseSimple());
        }
        if (s.equals("(")) {
            Object answer = parsePriority0();
            nextToken = nextToken();
            if (nextToken == null || !nextToken.equals(")")) {
                error("')' expected at position " + nowPosition);
                return null;
            }
            nextToken = null;
            return answer;
        }
        if (oneSymbolTokens.contains(s)) {
            return null;
        }
        nextToken = nextToken();
        if ("(".equals(nextToken)) {
            return tryParseFunction(s);
        }
        Object answer = tryParseNumericalValue(s);
        if (answer != null) {
            return answer;
        }
        WfVariable variable = context.getVariableProvider().getVariable(s);
        if (variable != null) {
            if (variable.getValue() == null) {
                log.warn("Null value will be returned for variable '" + s + "'");
            }
            return variable.getValue();
        }
        error("Cannot parse '" + s + "' at position " + (nowPosition - s.length() + 1));
        return null;
    }

    private Object tryParseFunction(String s) {
        nextToken();
        if (s.equals("get_instance_id") || s.equals("get_process_id")) {
            return simpleValue(s, context.getProcess().getId());
        }
        if (s.equals("current_date_time")) {
            return simpleValue(s, new Date());
        }
        if (s.equals("random")) {
            return simpleValue(s, Math.random());
        }
        if (s.equalsIgnoreCase("BigDecimal")) {
            return bigDecimalValue(s);
        }
        if (s.equalsIgnoreCase("float")) {
            return floatValue(s);
        }
        Function<? extends Object> function = Functions.getFunction(s);
        if (function != null) {
            List<Object> parameters = Lists.newArrayList();
            String token;
            do {
                Object param = parsePriority0();
                parameters.add(param);
                token = nextToken();
                if (token == null) {
                    throw new InternalApplicationException(
                            "Unable to parse function " + function + " parameters from configuration: " + configuration);
                }
            } while (!token.equals(")"));
            return function.execute(parameters.toArray(new Object[parameters.size()]));
        }
        return null;
    }

    private Object bigDecimalValue(String s) {
        Object param = parsePriority0();
        if (param == null || !nextToken().equals(")")) {
            incorrectParameters(s);
            return null;
        }
        return new BigDecimal(param.toString());
    }

    private Object floatValue(String s) {
        Object param = parsePriority0();
        if (param == null || !nextToken().equals(")")) {
            incorrectParameters(s);
            return null;
        }
        return new Float(param.toString());
    }

    private Object simpleValue(String s, Object value) {
        if (!nextToken().equals(")")) {
            incorrectParameters(s);
            return null;
        }
        return value;
    }

    private void incorrectParameters(String function) {
        error("Incorrect parameters for " + function + " function at position " + nowPosition);
    }

    private Object tryParseNumericalValue(String s) {
        if (s.equals(":")) {
            return nextToken();
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
        }
        try {
            return new Double(Double.parseDouble(s));
        } catch (NumberFormatException e) {
        }
        if (s.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }
        if (s.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);
        } catch (Exception e) {
        }
        try {
            return CalendarUtil.convertToDate(s, CalendarUtil.HOURS_MINUTES_FORMAT);
        } catch (Exception e) {
        }
        return null;
    }

    private void error(String message) {
        String details = "Incorrect formula in " + context.getProcess().toString() + " -> " + new String(formula);
        if (message != null) {
            details += "\n - " + message;
        }
        if (SystemProperties.isFormulaHandlerInStrictMode()) {
            throw new RuntimeException(details);
        } else {
            log.warn(details);
        }
    }

}
