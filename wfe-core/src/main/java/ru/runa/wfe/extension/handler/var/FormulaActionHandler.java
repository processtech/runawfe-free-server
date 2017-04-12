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
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.extension.function.Function;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
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
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return context.getProcess().getId();
        }
        if (s.equals("current_date_time")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return new Date();
        }
        if (s.equals("current_date")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.dateFunction(new Date());
        }
        if (s.equals("current_time")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.timeFunction(new Date());
        }
        if (s.equals("random")) {
            if (!nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return Math.random();
        }
        if (s.equals("date")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.dateFunction(param1);
        }
        if (s.equals("time")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.timeFunction(param1);
        }
        if (s.equals("hours_round_up")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return actions.hoursRoundUpFunction(param1);
        }
        if (s.equals("round_up")) {
            Object param1 = parsePriority0();
            Double d = (Double) actions.translate(param1, Double.class);
            if (d == null) {
                incorrectParameters(s);
                return null;
            }
            Integer num = 0;
            String tok = nextToken();
            if (!tok.equals(")")) {
                if (!tok.equals(",")) {
                    incorrectParameters(s);
                    return null;
                }
                num = (Integer) actions.translate(parsePriority0(), Integer.class);
                if (num == null) {
                    incorrectParameters(s);
                    return null;
                }
                tok = nextToken();
            }
            if (!tok.equals(")")) {
                incorrectParameters(s);
                return null;
            }
            if (BigDecimal.class.isInstance(param1)) {
                return ((BigDecimal) param1).setScale(num, RoundingMode.HALF_UP);
            } else {
                if (num <= 0) {
                    return actions.roundUpFunction(d);
                }
                return actions.roundUpFunction(d, num);
            }
        }
        if (s.equals("round_down")) {
            Object param1 = parsePriority0();
            Double d = (Double) actions.translate(param1, Double.class);
            if (d == null) {
                incorrectParameters(s);
                return null;
            }
            Integer num = 0;
            String tok = nextToken();
            if (!tok.equals(")")) {
                if (!tok.equals(",")) {
                    incorrectParameters(s);
                    return null;
                }
                num = (Integer) actions.translate(parsePriority0(), Integer.class);
                if (num == null) {
                    incorrectParameters(s);
                    return null;
                }
                tok = nextToken();
            }
            if (!tok.equals(")")) {
                incorrectParameters(s);
                return null;
            }
            if (BigDecimal.class.isInstance(param1)) {
                return ((BigDecimal) param1).setScale(num, RoundingMode.HALF_DOWN);
            } else {
                if (num <= 0) {
                    return actions.roundDownFunction(d);
                }
                return actions.roundDownFunction(d, num);
            }
        }
        if (s.equals("round")) {
            Object param1 = parsePriority0();
            Double d = (Double) actions.translate(param1, Double.class);
            if (d == null) {
                incorrectParameters(s);
                return null;
            }
            Integer num = 0;
            String tok = nextToken();
            if (!tok.equals(")")) {
                if (!tok.equals(",")) {
                    incorrectParameters(s);
                    return null;
                }
                num = (Integer) actions.translate(parsePriority0(), Integer.class);
                if (num == null) {
                    incorrectParameters(s);
                    return null;
                }
                tok = nextToken();
            }
            if (!tok.equals(")")) {
                incorrectParameters(s);
                return null;
            }
            if (BigDecimal.class.isInstance(param1)) {
                return ((BigDecimal) param1).setScale(num, RoundingMode.HALF_UP);
            } else {
                if (num <= 0) {
                    return actions.roundFunction(d);
                }
                return actions.roundFunction(d, num);
            }
        }
        if (s.equals("number_to_string_ru")) {
            Object param1 = parsePriority0();
            String tok = nextToken();
            if (param1 == null) {
                incorrectParameters(s);
                return null;
            }
            if (tok.equals(")")) {
                Long number = (Long) actions.translate(param1, Long.class);
                if (number == null) {
                    incorrectParameters(s);
                    return null;
                }
                return NumberToStringRu.numberToString(number);
            }
            if (!tok.equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param3 = parsePriority0();
            if (param3 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param4 = parsePriority0();
            if (param4 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param5 = parsePriority0();
            if (param5 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            Long number = (Long) actions.translate(param1, Long.class);
            int p = -1;
            if (param2.toString().equals("M")) {
                p = 0;
            }
            if (param2.toString().equals("F")) {
                p = 1;
            }
            if (p == -1 || number == null) {
                incorrectParameters(s);
                return null;
            }
            String s1 = param3.toString();
            String s2 = param4.toString();
            String s3 = param5.toString();
            return NumberToStringRu.numberToString(number, new NumberToStringRu.Word(p, new String[] { s1, s2, s3 }));
        }
        if (s.equals("FIO_case_ru")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param3 = parsePriority0();
            if (param3 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            String fio = param1.toString();
            Integer caseNumber = (Integer) actions.translate(param2, Integer.class);
            if (caseNumber == null || caseNumber < 1 || caseNumber > 6) {
                incorrectParameters(s);
                return null;
            }
            String mode = param3.toString();
            return actions.nameCaseRussian(fio, caseNumber, mode);
        }
        if (s.equalsIgnoreCase("BigDecimal")) {
            Object param = parsePriority0();
            if (param == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return new BigDecimal(param.toString());
        }
        if (s.equalsIgnoreCase("float")) {
            Object param = parsePriority0();
            if (param == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            return new Double(param.toString());
        }
        if (s.equals("mapping")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            String input = param1.toString();
            String rule = param2.toString();
            return actions.mapping(input, rule);
        }
        if (s.equals("number_to_short_string_ru")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param3 = parsePriority0();
            if (param3 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param4 = parsePriority0();
            if (param4 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Object param5 = parsePriority0();
            if (param5 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            Long number = (Long) actions.translate(param1, Long.class);
            int p = -1;
            if (param2.toString().equals("M")) {
                p = 0;
            }
            if (param2.toString().equals("F")) {
                p = 1;
            }
            if (p == -1 || number == null) {
                incorrectParameters(s);
                return null;
            }
            String s1 = param3.toString();
            String s2 = param4.toString();
            String s3 = param5.toString();
            return NumberToStringRu.numberToShortString(number, new NumberToStringRu.Word(p, new String[] { s1, s2, s3 }));
        }
        if (s.equals("isExecutorInGroup")) {
            Object param1 = parsePriority0();
            if (param1 == null || !nextToken().equals(",")) {
                incorrectParameters(s);
                return null;
            }
            Group group;
            try {
                group = TypeConversionUtil.convertTo(Group.class, param1);
            } catch (Exception e) {
                error("param1 cannot is not group: " + e);
                return null;
            }
            Object param2 = parsePriority0();
            if (param2 == null || !nextToken().equals(")")) {
                incorrectParameters(s);
                return null;
            }
            Executor executor;
            try {
                executor = TypeConversionUtil.convertTo(Executor.class, param2);
            } catch (Exception e) {
                error("param2 cannot is not executor: " + e);
                return null;
            }
            return ApplicationContextFactory.getExecutorDAO().isExecutorInGroup(executor, group);
        }
        Function<? extends Object> function = FormulaActionHandlerOperations.getFunction(s);
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
