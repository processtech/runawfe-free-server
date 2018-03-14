package ru.runa.wfe.extension.function;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class NumberToStringRu extends Function<String> {

    public NumberToStringRu() {
        super(Param.required(Long.class), Param.optional(Integer.class, -1), Param.multiple(String.class));
    }

    private static Word[] words = { new Word(1, new String[] { "тысяча", "тысячи", "тысяч" }),
            new Word(0, new String[] { "миллион", "миллиона", "миллионов" }), new Word(0, new String[] { "миллиард", "миллиарда", "миллиардов" }),
            new Word(0, new String[] { "триллион", "триллиона", "триллионов" }) };
    private static String[] hundreds = { "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот" };
    private static String[] decades = { "десять", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят",
            "девяносто" };
    private static String[] teens = { "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать",
            "восемнадцать", "девятнадцать" };
    private static Num[] digits = { new Num(new String[] { "ноль", "ноль" }, 2), new Num(new String[] { "один", "одна" }, 0),
            new Num(new String[] { "два", "две" }, 1), new Num(new String[] { "три", "три" }, 1), new Num(new String[] { "четыре", "четыре" }, 1),
            new Num(new String[] { "пять", "пять" }, 2), new Num(new String[] { "шесть", "шесть" }, 2), new Num(new String[] { "семь", "семь" }, 2),
            new Num(new String[] { "восемь", "восемь" }, 2), new Num(new String[] { "девять", "девять" }, 0) };

    @Override
    protected String doExecute(Object... parameters) {
        int param = (int)parameters[1];
        if (param == -1) {
            return numberToString((long) parameters[0]);
        } else if (parameters.length > 2) {
            long number = (long) parameters[0];
            List<String> symbols = new ArrayList<>(0);
            for (int i = 2; i < parameters.length; i++) {
                symbols.add((String) parameters[i]);
            }
            String[] wordSymbols = symbols.toArray(new String[symbols.size()]);
            return numberToString(number, new Word(param, wordSymbols));
        }
        return null;
    }

    private static class Word {
        public int p;
        public String[] s;

        public Word(int param, String[] str) {
            p = param;
            s = str;
        }
    }

    private static class Num {
        public String[] s;
        int p;

        public Num(String[] str, int param) {
            p = param;
            s = str;
        }
    }

    private static String numberToString(long number) {
        return numberToString(number, new Word(0, new String[] { "", "", "" }));
    }

    private static String numberToString(long number, Word word) {
        StringBuilder answer = new StringBuilder();
        if (number < 0) {
            answer.append("минус");
            number *= -1;
        }
        for (int a = words.length - 1; a >= 0; a--) {
            long st = 1;
            for (int b = 0; b <= a; b++) {
                st *= 1000;
            }
            long val = number / st;
            if (val / 1000 > 0) {
                return "Слишком большое число " + word.s[2];
            }
            if (val > 0) {
                if (answer.length() > 0) {
                    answer.append(" ");
                }
                answer.append(shortNumberToString((int) val, words[a]));
                number = number % st;
            }
        }
        if (number > 0 || answer.length() == 0) {
            if (answer.length() > 0) {
                answer.append(" ");
            }
            answer.append(shortNumberToString((int) number, word));
        } else {
            answer.append(" ").append(word.s[2]);
        }
        char first = (char) (answer.charAt(0) + 'А' - 'а');
        answer.append(first).append(answer.substring(1));
        return answer.toString();
    }

    private static String shortNumberToString(int number, Word word) {
        String answer = "";
        if (number / 100 > 0) {
            answer = hundreds[number / 100 - 1];
        }
        number %= 100;
        if (number / 10 == 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += teens[number - 10] + (word.s[2].length() > 0 ? (" " + word.s[2]) : "");
            return answer;
        }
        if (number / 10 > 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += decades[number / 10 - 1];
        }
        number %= 10;
        if (number > 0 || answer.length() == 0) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += digits[number].s[word.p];
        }
        String ws = word.s[digits[number].p];
        if (ws.length() > 0) {
            answer += " " + ws;
        }
        return answer;
    }

    @Override
    public String getName() {
        return "number_to_string_ru";
    }

}
